package pingrpc.ui.controllers

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.google.protobuf.DescriptorProtos.{FileDescriptorProto, MethodDescriptorProto}
import com.google.protobuf.Descriptors.FileDescriptor
import com.google.protobuf.util.JsonFormat
import com.google.protobuf.{DescriptorProtos, Descriptors, DynamicMessage, Message}
import com.typesafe.scalalogging.StrictLogging
import io.grpc.reflection.v1.ServiceResponse
import io.grpc.{Status, StatusException}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.scene.control._
import org.fxmisc.richtext.CodeArea
import pingrpc.form.{Form, FormRoot}
import pingrpc.grpc.{CurlPrinter, FullMessageName, ReflectionManager, Sender}
import pingrpc.proto.{ProtoUtils, serviceOrdering}
import pingrpc.storage.StateManager
import pingrpc.ui.tasks.SendTask
import pingrpc.ui.views.AlertView
import pingrpc.ui.{Header, RequestTimer, headersFromMap, headersToMap}
import protobuf.MethodOuterClass.Method
import protobuf.ServiceOuterClass.Service
import protobuf.StateOuterClass.State

import scala.jdk.CollectionConverters._
import scala.util.Try

class AppController(reflectionManager: ReflectionManager, sender: Sender, stateManager: StateManager) extends StrictLogging {
  def serviceAction[T <: ComboBox[Service]](
      urlField: TextField,
      methodsBox: ComboBox[Method]
  )(e: ActionEvent): Unit =
    Option(e.getSource.asInstanceOf[T].getSelectionModel.getSelectedItem).foreach { service =>
      (for {
        _ <- IO(logger.info(s"Service `${service.getName}` is selected"))
        fileDescriptorProtos <- reflectionManager.getFileDescriptors(urlField.getText, service.getName)
        serviceDescriptorProto <- IO.fromEither(findServiceDescriptor(fileDescriptorProtos, service.getName))
        methods = serviceDescriptorProto.getMethodList.asScala.map(buildProtoMethod).toList
        _ <- stateManager.update(
          _.clearFileDescriptorProtos
            .addAllFileDescriptorProtos(fileDescriptorProtos.asJava)
            .clearMethods
            .addAllMethods(methods.asJava)
            .setSelectedService(service)
        )
      } yield methods).attempt.unsafeRunSync match {
        case Right(methods) if methods.nonEmpty => fillMethods(methods, methods.head, methodsBox)
        case Right(_) => new AlertView("No methods found", s"Service `${service.getName}` returned no methods").showAndWait
        case Left(error) =>
          logger.error(error.getMessage, error)
          new AlertView("Unknown error", error.getMessage).showAndWait
      }
    }

  def methodAction[T <: ComboBox[Method]](
      responseMessageLabel: Label,
      sendButton: Button,
      formPane: ScrollPane
  )(e: ActionEvent): Unit =
    Option(e.getSource.asInstanceOf[T].getSelectionModel.getSelectedItem).foreach { method =>
      (for {
        _ <- IO(logger.info(s"Method `${method.getName}` is selected"))
        fileDescriptors = ProtoUtils.toFileDescriptors(stateManager.currentState.getFileDescriptorProtosList.asScala.toList)
        requestDescriptor <- IO.fromEither(findMessageDescriptor(fileDescriptors, method.getInputType))
        responseDescriptor <- IO.fromEither(findMessageDescriptor(fileDescriptors, method.getOutputType))
        _ <- stateManager.update(_.setSelectedMethod(method))
        _ <- IO.whenA(!isSameMessageName(stateManager.currentState.getRequest.getTypeUrl, method.getInputType)) {
          stateManager.update(_.clearRequest.clearResponse)
        }
        form = Form.build(requestDescriptor, None)
      } yield (form, responseDescriptor.getFullName)).attempt.unsafeRunSync match {
        case Right((form, responseMessageName)) =>
          responseMessageLabel.setText(responseMessageName)
          formPane.setContent(form.toNode)
          formPane.setUserData(form)
          sendButton.setDisable(false)
        case Left(error) =>
          sendButton.setDisable(true)
          logger.error(error.getMessage, error)
          new AlertView("Unknown error", error.getMessage).showAndWait
      }
    }

  def syncAction(urlField: TextField, servicesBox: ComboBox[Service], methodsBox: ComboBox[Method])(e: ActionEvent): Unit = {
    val syncButton = e.getSource.asInstanceOf[Button]

    syncButton.setDisable(true)

    reflectionManager
      .getServices(urlField.getText)
      .map(_.map(buildProtoService))
      .flatTap(services => stateManager.update(_.setUrl(urlField.getText).clearServices.addAllServices(services.asJava)))
      .attempt
      .unsafeRunSync
      .map(_.sorted[Service](serviceOrdering))
      .left
      .map { error =>
        methodsBox.setDisable(true)
        servicesBox.setDisable(true)

        error
      } match {
      case Right(services @ service :: _) =>
        servicesBox.getItems.clear()
        methodsBox.getItems.clear()

        fillServices(services, service, servicesBox)
      case Right(Nil) =>
        new AlertView(s"${urlField.getText} returned no services", "No services found").showAndWait
      case Left(error: StatusException) if error.getStatus.getCode == Status.UNIMPLEMENTED.getCode =>
        new AlertView(s"${urlField.getText} does not support reflection", error.getMessage).showAndWait
      case Left(error: StatusException) =>
        new AlertView(s"${urlField.getText} is not available", error.getMessage).showAndWait
      case Left(error: Throwable) =>
        new AlertView("Unknown error", error.getMessage).showAndWait
    }

    syncButton.setDisable(false)
  }

  def sendAction(
      urlField: TextField,
      servicesBox: ComboBox[Service],
      methodsBox: ComboBox[Method],
      formPane: ScrollPane,
      curlArea: CodeArea,
      jsonArea: CodeArea,
      requestHeaders: ObservableList[Header],
      responseHeaders: ObservableList[Header],
      responseStatusLabel: Label
  )(e: ActionEvent): Unit = {
    val service = servicesBox.getSelectionModel.getSelectedItem
    val method = methodsBox.getSelectionModel.getSelectedItem
    val methodName = ProtoUtils.buildMethodName(service, method)
    val fileDescriptors = ProtoUtils.toFileDescriptors(stateManager.currentState.getFileDescriptorProtosList.asScala.toList)
    val requestTimer = new RequestTimer(responseStatusLabel)
    val sendButton: Button = e.getSource.asInstanceOf[Button]

    curlArea.clear()
    responseHeaders.clear()
    jsonArea.clear()

    val message: Message = formPane.getUserData.asInstanceOf[FormRoot].toMessage
    val json: String = ProtoUtils.messageToJson(message)

    curlArea.replaceText(CurlPrinter.print(service, method, urlField.getText, json, headersToMap(requestHeaders)))

    val sendTask = new SendTask(
      message,
      methodName,
      urlField.getText,
      requestTimer,
      jsonArea,
      requestHeaders,
      responseHeaders,
      sender,
      fileDescriptors,
      method,
      sendButton
    )

    new Thread(sendTask).start()
  }

  def applyState(
      urlField: TextField,
      servicesBox: ComboBox[Service],
      methodsBox: ComboBox[Method],
      requestHeaders: ObservableList[Header],
      responseHeaders: ObservableList[Header],
      formPane: ScrollPane,
      responseArea: CodeArea,
      sendButton: Button,
      responseMessageLabel: Label
  ): Unit =
    stateManager.load().attempt.unsafeRunSync.toOption.filter(_ != State.getDefaultInstance).foreach { state =>
      fillServices(state.getServicesList.asScala.toList, state.getSelectedService, servicesBox)
      fillMethods(state.getMethodsList.asScala.toList, state.getSelectedMethod, methodsBox)

      val fileDescriptors = ProtoUtils.toFileDescriptors(state.getFileDescriptorProtosList.asScala.toList)

      for {
        requestDescriptor <- findMessageDescriptor(fileDescriptors, state.getSelectedMethod.getInputType)
        responseDescriptor <- findMessageDescriptor(fileDescriptors, state.getSelectedMethod.getOutputType)
        requestBuilder = DynamicMessage.getDefaultInstance(requestDescriptor).toBuilder
        requestOpt = Try(requestBuilder.mergeFrom(state.getRequest.getValue.toByteArray))
          .map(_.build)
          .toOption
        responseBuilder = DynamicMessage.getDefaultInstance(responseDescriptor).toBuilder
        responseOpt = Try(responseBuilder.mergeFrom(state.getResponse.getValue.toByteArray))
          .map(_.build)
          .map(JsonFormat.printer.preservingProtoFieldNames.print)
      } yield {
        val form = Form.build(requestDescriptor, requestOpt)

        formPane.setContent(form.toNode)
        formPane.setUserData(form)

        urlField.setText(state.getUrl)
        sendButton.setDisable(false)
        responseMessageLabel.setText(responseDescriptor.getFullName)

        requestHeaders.clear()
        requestHeaders.addAll(headersFromMap(state.getRequestHeadersMap.asScala.toMap))

        responseHeaders.clear()
        responseHeaders.addAll(headersFromMap(state.getResponseHeadersMap.asScala.toMap))

        responseOpt.foreach(responseArea.replaceText)
      }
    }

  def requestTabsListener(requestArea: CodeArea, formPane: ScrollPane): ChangeListener[Tab] = (_: ObservableValue[_ <: Tab], _: Tab, tab: Tab) =>
    tab.getId match {
      case "json" =>
        val message = formPane.getUserData.asInstanceOf[FormRoot].toMessage
        val json = ProtoUtils.messageToJson(message)

        if (requestArea.getText != json) requestArea.replaceText(json)
      case _ =>
    }

  def jsonToMessage(requestArea: CodeArea, formPane: ScrollPane, methodsBox: ComboBox[Method]): Unit = {
    val fileDescriptors = ProtoUtils.toFileDescriptors(stateManager.currentState.getFileDescriptorProtosList.asScala.toList)

    (for {
      requestDescriptor <- findMessageDescriptor(fileDescriptors, methodsBox.getSelectionModel.getSelectedItem.getInputType)
      message <- ProtoUtils.messageFromJson(requestArea.getText, requestDescriptor)
      form = Form.build(requestDescriptor, Option.when(message.toByteArray.nonEmpty)(message))
    } yield form) match {
      case Right(form) =>
        requestArea.getStyleClass.removeAll("error")
        formPane.setContent(form.toNode)
        formPane.setUserData(form)
      case Left(error) =>
        requestArea.getStyleClass.add("error")
        logger.error("Cannot convert json to form", error)
    }
  }

  private def fillServices(services: List[Service], selectedService: Service, servicesBox: ComboBox[Service]): Unit =
    if (services.nonEmpty) {
      servicesBox.getItems.clear()
      services.foreach(servicesBox.getItems.add)
      servicesBox.setDisable(false)
      servicesBox.getSelectionModel.select(selectedService)
    }

  private def fillMethods(methods: List[Method], selectedMethod: Method, methodsBox: ComboBox[Method]): Unit =
    if (methods.nonEmpty) {
      methodsBox.getItems.clear()
      methods.foreach(methodsBox.getItems.add)
      methodsBox.setDisable(false)
      methodsBox.getSelectionModel.select(selectedMethod)
    }

  private def buildProtoMethod(methodDescriptorProto: MethodDescriptorProto): Method =
    Method.newBuilder
      .setName(methodDescriptorProto.getName)
      .setInputType(methodDescriptorProto.getInputType)
      .setOutputType(methodDescriptorProto.getOutputType)
      .build

  private def buildProtoService(serviceResponse: ServiceResponse): Service =
    Service.newBuilder
      .setName(serviceResponse.getName)
      .build

  private def findMessageDescriptor(fileDescriptors: List[FileDescriptor], name: String): Either[Throwable, Descriptors.Descriptor] =
    for {
      fullMessageName <- FullMessageName
        .parse(name)
        .toRight(new Throwable(s"Cannot get full message name from `$name`"))
      descriptor <- ProtoUtils
        .findMessageDescriptor(fileDescriptors, fullMessageName)
        .toRight(new Throwable(s"Cannot find response descriptor for `$fullMessageName`"))
    } yield descriptor

  private def findServiceDescriptor(fileDescriptorProtos: List[FileDescriptorProto], name: String): Either[Throwable, DescriptorProtos.ServiceDescriptorProto] =
    for {
      fullMessageName <- FullMessageName
        .parse(name)
        .toRight(new Throwable(s"Cannot parse full message name from `$name`"))
      serviceDescriptorProto <- ProtoUtils
        .findServiceDescriptor(fileDescriptorProtos, fullMessageName)
        .toRight(new Throwable(s"Cannot find service descriptor for `$fullMessageName`"))
    } yield serviceDescriptorProto

  private def isSameMessageName(left: String, right: String): Boolean =
    (for {
      leftFullMessageName <- FullMessageName.parse(left)
      rightFullMessageName <- FullMessageName.parse(right)
    } yield leftFullMessageName == rightFullMessageName).getOrElse(false)
}
