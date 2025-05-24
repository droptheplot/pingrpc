package pingrpc.ui.controllers

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.google.protobuf.DescriptorProtos.{FileDescriptorProto, MethodDescriptorProto}
import com.google.protobuf.Descriptors.FileDescriptor
import com.google.protobuf.util.JsonFormat
import com.google.protobuf.{DescriptorProtos, Descriptors, DynamicMessage, InvalidProtocolBufferException}
import com.typesafe.scalalogging.StrictLogging
import io.grpc.reflection.v1.ServiceResponse
import io.grpc.{Status, StatusException}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.event.ActionEvent
import javafx.scene.control._
import pingrpc.form.Form
import pingrpc.grpc.{CurlPrinter, FullMessageName, ReflectionManager, Sender}
import pingrpc.proto.ProtoUtils
import pingrpc.storage.StateManager
import pingrpc.ui.views.{AlertView, MetadataView}
import protobuf.MethodOuterClass.Method
import protobuf.ServiceOuterClass.Service
import protobuf.StateOuterClass.State

import scala.jdk.CollectionConverters._
import scala.util.Try

class ActionController(reflectionManager: ReflectionManager, sender: Sender, stateManager: StateManager) extends StrictLogging {
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
      submitButton: Button,
      formPane: ScrollPane,
      jsonArea: TextArea
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
        requestOpt = Try(DynamicMessage.getDefaultInstance(requestDescriptor).toBuilder.mergeFrom(stateManager.currentState.getRequest.getValue.toByteArray).build)
        responseOpt = Try(DynamicMessage.getDefaultInstance(responseDescriptor).toBuilder.mergeFrom(stateManager.currentState.getResponse.getValue.toByteArray).build)
        responseJson = responseOpt.map(JsonFormat.printer.preservingProtoFieldNames.print).getOrElse("{}")
        form = Form.build(requestDescriptor, requestOpt.toOption)
      } yield (form, responseDescriptor.getFullName, responseJson)).attempt.unsafeRunSync match {
        case Right((form, responseMessageName, responseJson)) =>
          responseMessageLabel.setText(responseMessageName)
          formPane.setContent(form.toNode)
          formPane.setUserData(form)
          submitButton.setDisable(false)
          jsonArea.setText(responseJson)
        case Left(error) =>
          submitButton.setDisable(true)
          logger.error(error.getMessage, error)
          new AlertView("Unknown error", error.getMessage).showAndWait
      }
    }

  def syncAction(urlField: TextField, servicesBox: ComboBox[Service], methodsBox: ComboBox[Method])(e: ActionEvent): Unit =
    reflectionManager
      .getServices(urlField.getText)
      .map(_.map(buildProtoService))
      .flatTap(services => stateManager.update(_.setUrl(urlField.getText).clearServices.addAllServices(services.asJava)))
      .attempt
      .unsafeRunSync
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
      case Left(error: StatusException) if error.getStatus.getCode == Status.UNIMPLEMENTED.getCode =>
        new AlertView(s"${urlField.getText} does not support reflection", error.getMessage).showAndWait
      case Left(error: StatusException) =>
        new AlertView(s"${urlField.getText} is not available", error.getMessage).showAndWait
      case Left(error: Throwable) =>
        new AlertView("Unknown error", error.getMessage).showAndWait
    }

  def submitAction(
      urlField: TextField,
      servicesBox: ComboBox[Service],
      methodsBox: ComboBox[Method],
      formPane: ScrollPane,
      tabPane: TabPane,
      requestArea: TextArea,
      curlArea: TextArea,
      responseMetadataContainer: ScrollPane,
      jsonArea: TextArea
  )(e: ActionEvent): Unit = {
    val service = servicesBox.getSelectionModel.getSelectedItem
    val method = methodsBox.getSelectionModel.getSelectedItem
    val fileDescriptors = ProtoUtils.toFileDescriptors(stateManager.currentState.getFileDescriptorProtosList.asScala.toList)

    (for {
      requestDescriptor <- findMessageDescriptor(fileDescriptors, method.getInputType)
      responseDescriptor <- findMessageDescriptor(fileDescriptors, method.getOutputType)
      json = tabPane.getSelectionModel.getSelectedItem.getId match {
        case "form" => formPane.getUserData.asInstanceOf[Form].toJson.asObject.filter(_.nonEmpty).map(_.toJson.toString).getOrElse("")
        case "json" => requestArea.getText
        case _ => ""
      }
      curlText = CurlPrinter.print(service, method, urlField.getText, json)
      _ = curlArea.setText(curlText)
      response <- sender.send(requestDescriptor, responseDescriptor, ProtoUtils.buildMethodName(service, method), urlField.getText, json).attempt.unsafeRunSync
      metadataView = new MetadataView(response.headers, responseMetadataContainer)
      _ = responseMetadataContainer.setContent(metadataView)
    } yield response) match {
      case Right(response) =>
        jsonArea.setText(response.message)
      case Left(error: StatusException) =>
        new AlertView("Server responded with an error", error.getMessage).showAndWait
      case Left(error: InvalidProtocolBufferException) =>
        new AlertView("Cannot build request", error.getMessage).showAndWait
      case Left(error) =>
        new AlertView("Unknown error", error.toString).showAndWait
    }
  }

  def applyState(urlField: TextField, servicesBox: ComboBox[Service], methodsBox: ComboBox[Method]): Unit =
    stateManager.load().attempt.unsafeRunSync.toOption.filter(_ != State.getDefaultInstance).foreach { state =>
      urlField.setText(state.getUrl)

      fillServices(state.getServicesList.asScala.toList, state.getSelectedService, servicesBox)
      fillMethods(state.getMethodsList.asScala.toList, state.getSelectedMethod, methodsBox)

      methodsBox.fireEvent(new ActionEvent())
    }

  def requestTabsListener(requestArea: TextArea, formPane: ScrollPane, methodsBox: ComboBox[Method]): ChangeListener[Tab] =
    (_: ObservableValue[_ <: Tab], _: Tab, tab: Tab) =>
      tab.getId match {
        case "form" if stateManager.currentState.getRequestDescriptor.hasName =>
          val fileDescriptors = ProtoUtils.toFileDescriptors(stateManager.currentState.getFileDescriptorProtosList.asScala.toList)

          (for {
            requestDescriptor <- findMessageDescriptor(fileDescriptors, methodsBox.getSelectionModel.getSelectedItem.getInputType)
            message <- ProtoUtils.messageFromJson(requestArea.getText, requestDescriptor)
            form = Form.build(requestDescriptor, Option.when(message.toByteArray.nonEmpty)(message))
          } yield form) match {
            case Right(form) =>
              formPane.setContent(form.toNode)
              formPane.setUserData(form)
            case Left(error) =>
              logger.error("Cannot convert json to form", error)
          }
        case "json" =>
          requestArea.setText(formPane.getUserData.asInstanceOf[Form].toJson.asObject.filter(_.nonEmpty).map(_.toJson.toString).getOrElse(""))
        case _ =>
      }

  private def fillServices(services: List[Service], selectedService: Service, servicesBox: ComboBox[Service]): Unit =
    if (services.nonEmpty) {
      servicesBox.getItems.clear()
      services
        .sorted[Service] {
          case (a, _) if a.getName.startsWith("grpc.reflection") => 1
          case (_, b) if b.getName.startsWith("grpc.reflection") => -1
          case (a, b) => a.getName.compareTo(b.getName)
        }
        .foreach(servicesBox.getItems.add)
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
