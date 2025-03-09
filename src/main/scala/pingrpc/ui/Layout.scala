package pingrpc.ui

import cats.effect.unsafe.implicits.global
import com.google.protobuf.DescriptorProtos.{FileDescriptorProto, MethodDescriptorProto}
import com.typesafe.scalalogging.StrictLogging
import io.grpc.StatusRuntimeException
import io.grpc.reflection.v1.ServiceResponse
import javafx.geometry.Insets
import javafx.scene.control.{Button, ComboBox, TextArea, TextField}
import javafx.scene.layout._
import javafx.scene.text.Font
import pingrpc.grpc.{CurlPrinter, FullMessageName, ReflectionManager, Sender}
import pingrpc.proto.{MethodDescriptorProtoConverter, ProtoUtils, ServiceResponseConverter}

import scala.collection.mutable
import scala.util.chaining.scalaUtilChainingOps

class Layout(reflectionManager: ReflectionManager, sender: Sender) extends StrictLogging {
  private val fileDescriptorProtos = mutable.ListBuffer.empty[FileDescriptorProto]

  private val monospacedFont = Font.font("monospaced")

  private val urlField: TextField = new TextField()
    .tap(_.setText("localhost:8080"))
  HBox.setHgrow(urlField, Priority.ALWAYS)

  private lazy val requestArea: TextArea = new TextArea()
    .tap(_.setWrapText(true))
    .tap(_.setWrapText(true))
    .tap(_.setFont(monospacedFont))
  VBox.setVgrow(requestArea, Priority.ALWAYS)

  private lazy val jsonArea: TextArea = new TextArea()
    .tap(_.setEditable(false))
    .tap(_.setWrapText(true))
    .tap(_.setFont(monospacedFont))
  VBox.setVgrow(jsonArea, Priority.ALWAYS)

  private lazy val curlArea: TextArea = new TextArea()
    .tap(_.setEditable(false))
    .tap(_.setWrapText(true))
    .tap(_.setFont(monospacedFont))
  VBox.setVgrow(curlArea, Priority.ALWAYS)

  private val syncButton: Button = new Button("Sync")

  private val submitButton: Button = new Button("Send")
    .tap(_.setDisable(true))

  private lazy val servicesBox: ComboBox[ServiceResponse] = new ComboBox[ServiceResponse]()
    .tap(_.setConverter(new ServiceResponseConverter))
    .tap(_.setPrefWidth(Double.MaxValue))
    .tap(_.setPromptText("..."))
    .tap(_.setDisable(true))

  private lazy val methodsBox: ComboBox[MethodDescriptorProto] = new ComboBox[MethodDescriptorProto]()
    .tap(_.setConverter(new MethodDescriptorProtoConverter))
    .tap(_.setPrefWidth(Double.MaxValue))
    .tap(_.setPromptText("..."))
    .tap(_.setDisable(true))

  private lazy val statusArea = new TextArea()
    .tap(_.setEditable(false))
    .tap(_.setWrapText(true))

  servicesBox.setOnAction { _ =>
    Option(servicesBox.getSelectionModel.getSelectedItem).foreach { serviceResponse =>
      logger.info(s"Select service=${serviceResponse.getName}")

      fileDescriptorProtos.clear()
      fileDescriptorProtos.addAll(reflectionManager.getFileDescriptors(urlField.getText, serviceResponse.getName).unsafeRunSync)

      ProtoUtils
        .findServiceDescriptor(fileDescriptorProtos.toList, FullMessageName.parse(serviceResponse.getName).get)
        .foreach { serviceDescriptorProto =>
          methodsBox.setDisable(false)
          methodsBox.getItems.setAll(serviceDescriptorProto.getMethodList)
          methodsBox.getSelectionModel.select(0)
        }
    }
  }

  methodsBox.setOnAction { _ =>
    Option(methodsBox.getSelectionModel.getSelectedItem) match {
      case Some(methodDescriptorProto) =>
        logger.info(s"Select method=${methodDescriptorProto.getName}")
        submitButton.setDisable(false)
      case _ => ()
    }
  }

  syncButton.setOnAction { _ =>
    servicesBox.getItems.clear()
    methodsBox.getItems.clear()
    statusArea.clear()

    reflectionManager.getServices(urlField.getText).attempt.unsafeRunSync match {
      case Right(serviceResponses) =>
        serviceResponses.foreach(servicesBox.getItems.add)
        servicesBox.getSelectionModel.select(0)
        servicesBox.setDisable(false)
      case Left(error) =>
        statusArea.setText(error.toString)
        methodsBox.setDisable(true)
        servicesBox.setDisable(true)
    }
  }

  submitButton.setOnAction { _ =>
    val serviceResponse = servicesBox.getSelectionModel.getSelectedItem
    val methodDescriptorProto = methodsBox.getSelectionModel.getSelectedItem

    (for {
      requestMessageName <- FullMessageName
        .parse(methodDescriptorProto.getInputType)
        .toRight(new Throwable(s"Cannot get request name from `${methodDescriptorProto.getInputType}`"))
      responseMessageName <- FullMessageName
        .parse(methodDescriptorProto.getOutputType)
        .toRight(new Throwable(s"Cannot get response name from `${methodDescriptorProto.getOutputType}`"))
      fileDescriptors = ProtoUtils.toFileDescriptors(fileDescriptorProtos.toList)
      requestDescriptor <- ProtoUtils
        .findMessageDescriptor(fileDescriptors, requestMessageName)
        .toRight(new Throwable(s"Cannot find request descriptor for `$requestMessageName`"))
      responseDescriptor <- ProtoUtils
        .findMessageDescriptor(fileDescriptors, responseMessageName)
        .toRight(new Throwable(s"Cannot find response descriptor for `$responseMessageName`"))
      curlText = CurlPrinter.print(serviceResponse, methodDescriptorProto, urlField.getText, requestArea.getText)
      _ = curlArea.setText(curlText)
      method = ProtoUtils.buildMethodName(serviceResponse, methodDescriptorProto)
      responseText <- sender.send(requestDescriptor, responseDescriptor, method, urlField.getText, requestArea.getText).attempt.unsafeRunSync
    } yield (responseText, requestDescriptor)) match {
      case Right(responseText -> requestDescriptor) =>
        jsonArea.setText(responseText)
        statusArea.setText(s"OK\n${requestDescriptor.getFullName}\n${requestDescriptor.getFile.getFullName}")
      case Left(error: StatusRuntimeException) =>
        jsonArea.clear()
        statusArea.setText(error.getMessage)
      case Left(error) =>
        jsonArea.clear()
        statusArea.setText(error.getMessage)
    }
  }

  def build: Pane = {
    val leftColumn = new ColumnConstraints()
      .tap(_.setPercentWidth(50))

    val responsePane = new ResponsePane(jsonArea, curlArea, statusArea).build

    val rightColumn = new ColumnConstraints()
      .tap(_.setPercentWidth(50))

    val gridPane = new GridPane()
      .tap(_.getColumnConstraints.add(leftColumn))
      .tap(_.getColumnConstraints.add(rightColumn))
      .tap { pane =>
        leftPane.prefHeightProperty.bind(pane.heightProperty)
        responsePane.prefHeightProperty.bind(pane.heightProperty)
      }
      .tap(_.add(leftPane, 0, 0))
      .tap(_.add(responsePane, 1, 0))

    new FlowPane()
      .tap(_.setHgap(10))
      .tap(_.setVgap(10))
      .tap(_.setPrefHeight(Double.MaxValue))
      .tap(_.setMinHeight(Double.MaxValue))
      .tap(_.getChildren.add(gridPane))
      .tap { root =>
        gridPane.prefWidthProperty.bind(root.widthProperty)
        gridPane.prefHeightProperty.bind(root.heightProperty)
      }
  }

  private lazy val leftPane: Pane = new VBox()
    .tap(_.setSpacing(10))
    .tap(_.setPadding(new Insets(10, 5, 10, 10)))
    .tap(_.getChildren.add(submitPane))
    .tap(_.getChildren.add(servicesBox))
    .tap(_.getChildren.add(methodsBox))
    .tap(_.getChildren.add(requestArea))

  private lazy val submitPane: Pane = new HBox()
    .tap(_.setSpacing(10))
    .tap(_.getChildren.add(urlField))
    .tap(_.getChildren.add(syncButton))
    .tap(_.getChildren.add(submitButton))
}
