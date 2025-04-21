package pingrpc.ui.controllers

import cats.effect.unsafe.implicits.global
import com.google.protobuf.DescriptorProtos.MethodDescriptorProto
import com.typesafe.scalalogging.StrictLogging
import io.grpc.StatusRuntimeException
import io.grpc.reflection.v1.ServiceResponse
import javafx.event.ActionEvent
import javafx.scene.control._
import pingrpc.form.Form
import pingrpc.grpc.{CurlPrinter, FullMessageName, ReflectionManager, Sender}
import pingrpc.proto.ProtoUtils
import pingrpc.ui.State
import pingrpc.ui.views.MetadataView

class ActionController(reflectionManager: ReflectionManager, sender: Sender) extends StrictLogging {
  def serviceAction[T <: ComboBox[ServiceResponse]](
      urlField: TextField,
      methodsBox: ComboBox[MethodDescriptorProto]
  )(e: ActionEvent): Unit = {
    Option(e.getSource.asInstanceOf[T].getSelectionModel.getSelectedItem).foreach { serviceResponse =>
      logger.info(s"Service `${serviceResponse.getName}` is selected")

      State.fileDescriptorProtos.clear()
      State.fileDescriptorProtos.addAll(reflectionManager.getFileDescriptors(urlField.getText, serviceResponse.getName).unsafeRunSync)

      ProtoUtils
        .findServiceDescriptor(State.fileDescriptorProtos.toList, FullMessageName.parse(serviceResponse.getName).get)
        .foreach { serviceDescriptorProto =>
          methodsBox.setDisable(false)
          methodsBox.getItems.setAll(serviceDescriptorProto.getMethodList)
          methodsBox.getSelectionModel.select(0)
        }
    }
  }

  def methodAction[T <: ComboBox[MethodDescriptorProto]](
      responseMessageLabel: Label,
      submitButton: Button,
      formPane: ScrollPane
  )(e: ActionEvent): Unit = {
    Option(e.getSource.asInstanceOf[T].getSelectionModel.getSelectedItem).foreach { methodDescriptorProto =>
      logger.info(s"Method `${methodDescriptorProto.getName}` is selected")

      FullMessageName.parse(methodDescriptorProto.getOutputType).foreach { fullMessageName =>
        responseMessageLabel.setText(fullMessageName.toString)
      }
      submitButton.setDisable(false)

      val fileDescriptors = ProtoUtils.toFileDescriptors(State.fileDescriptorProtos.toList)

      for {
        requestMessageName <- FullMessageName
          .parse(methodDescriptorProto.getInputType)
          .toRight(new Throwable(s"Cannot get request name from `${methodDescriptorProto.getInputType}`"))
        descriptor <- ProtoUtils
          .findMessageDescriptor(fileDescriptors, requestMessageName)
          .toRight(new Throwable(s"Cannot find request descriptor for `$requestMessageName`"))
        form = Form.build(descriptor)
      } yield {
        formPane.setContent(form.toNode)
        formPane.setUserData(form)
      }
    }
  }

  def syncAction(urlField: TextField, servicesBox: ComboBox[ServiceResponse], methodsBox: ComboBox[MethodDescriptorProto])(e: ActionEvent): Unit = {
    servicesBox.getItems.clear()
    methodsBox.getItems.clear()

    reflectionManager.getServices(urlField.getText).attempt.unsafeRunSync match {
      case Right(serviceResponses) =>
        serviceResponses
          .sorted[ServiceResponse] {
            case (a, _) if a.getName == "grpc.reflection.v1alpha.ServerReflection" => 1
            case (_, b) if b.getName == "grpc.reflection.v1alpha.ServerReflection" => -1
            case (a, b) => a.getName.compareTo(b.getName)
          }
          .foreach(servicesBox.getItems.add)
        servicesBox.getSelectionModel.select(0)
        servicesBox.setDisable(false)
      case Left(_) =>
        methodsBox.setDisable(true)
        servicesBox.setDisable(true)
    }
  }

  def submitAction(
      urlField: TextField,
      servicesBox: ComboBox[ServiceResponse],
      methodsBox: ComboBox[MethodDescriptorProto],
      formPane: ScrollPane,
      tabPane: TabPane,
      requestArea: TextArea,
      curlArea: TextArea,
      responseMetadataContainer: ScrollPane,
      jsonArea: TextArea
  )(e: ActionEvent): Unit = {
    val serviceResponse = servicesBox.getSelectionModel.getSelectedItem
    val methodDescriptorProto = methodsBox.getSelectionModel.getSelectedItem

    (for {
      requestMessageName <- FullMessageName
        .parse(methodDescriptorProto.getInputType)
        .toRight(new Throwable(s"Cannot get request name from `${methodDescriptorProto.getInputType}`"))
      responseMessageName <- FullMessageName
        .parse(methodDescriptorProto.getOutputType)
        .toRight(new Throwable(s"Cannot get response name from `${methodDescriptorProto.getOutputType}`"))
      fileDescriptors = ProtoUtils.toFileDescriptors(State.fileDescriptorProtos.toList)
      requestDescriptor <- ProtoUtils
        .findMessageDescriptor(fileDescriptors, requestMessageName)
        .toRight(new Throwable(s"Cannot find request descriptor for `$requestMessageName`"))
      responseDescriptor <- ProtoUtils
        .findMessageDescriptor(fileDescriptors, responseMessageName)
        .toRight(new Throwable(s"Cannot find response descriptor for `$responseMessageName`"))
      method = ProtoUtils.buildMethodName(serviceResponse, methodDescriptorProto)
      json = tabPane.getSelectionModel.getSelectedItem.getId match {
        case "form" => formPane.getUserData.asInstanceOf[Form].toJson.asObject.filter(_.nonEmpty).map(_.toJson.toString).getOrElse("{}")
        case "json" => requestArea.getText
        case _ => "{}"
      }
      curlText = CurlPrinter.print(serviceResponse, methodDescriptorProto, urlField.getText, json)
      _ = curlArea.setText(curlText)
      response <- sender.send(requestDescriptor, responseDescriptor, method, urlField.getText, json).attempt.unsafeRunSync
      metadataView = new MetadataView(response.headers, responseMetadataContainer)
      _ = responseMetadataContainer.setContent(metadataView)
    } yield response) match {
      case Right(response) =>
        jsonArea.setText(response.message)
      case Left(e: StatusRuntimeException) =>
        jsonArea.setText(e.toString)
      case Left(e) =>
        jsonArea.setText(e.toString)
    }
  }
}
