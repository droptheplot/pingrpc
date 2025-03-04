package pingrpc.ui

import cats.effect.unsafe.implicits.global
import com.google.protobuf.DescriptorProtos.{FileDescriptorProto, MethodDescriptorProto}
import com.typesafe.scalalogging.StrictLogging
import pingrpc.grpc.{FullMessageName, ReflectionManager, Sender}
import pingrpc.proto.{MethodDescriptorProtoConverter, ProtoUtils, ServiceResponseConverter}
import io.grpc.StatusRuntimeException
import io.grpc.reflection.v1.ServiceResponse
import javafx.geometry.Insets
import javafx.scene.control.{Button, ComboBox, TextArea, TextField}
import javafx.scene.layout._
import javafx.scene.text.Font

import scala.util.chaining.scalaUtilChainingOps

class Layout(reflectionManager: ReflectionManager, sender: Sender) extends StrictLogging {
  private var fileDescriptorProtos = List.empty[FileDescriptorProto]

  private val monospacedFont = Font.font("monospaced")

  private val urlField: TextField = new TextField()
    .tap(_.setText("localhost:8080"))
  HBox.setHgrow(urlField, Priority.ALWAYS)

  private lazy val requestArea: TextArea = new TextArea()
    .tap(_.setWrapText(true))
    .tap(_.setWrapText(true))
    .tap(_.setFont(monospacedFont))
  VBox.setVgrow(requestArea, Priority.ALWAYS)

  private lazy val responseArea: TextArea = new TextArea()
    .tap(_.setEditable(false))
    .tap(_.setWrapText(true))
    .tap(_.setFont(monospacedFont))
  VBox.setVgrow(responseArea, Priority.ALWAYS)

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

  servicesBox.setOnAction { _ =>
    val serviceResponse: ServiceResponse = servicesBox.getSelectionModel.getSelectedItem

    logger.info(s"Select service=${serviceResponse.getName}")

    fileDescriptorProtos = reflectionManager.getFileDescriptors(urlField.getText, serviceResponse.getName).unsafeRunSync

    ProtoUtils
      .findServiceDescriptor(fileDescriptorProtos, FullMessageName.parse(serviceResponse.getName).get)
      .foreach { serviceDescriptorProto =>
        methodsBox.setDisable(false)
        methodsBox.getItems.setAll(serviceDescriptorProto.getMethodList)
        methodsBox.getSelectionModel.select(0)
      }
  }

  methodsBox.setOnAction { _ =>
    logger.info(s"Select method=${methodsBox.getSelectionModel.getSelectedItem.getName}")
    submitButton.setDisable(false)
  }

  syncButton.setOnAction { _ =>
    servicesBox.getItems.clear()
    methodsBox.getItems.clear()

    reflectionManager.getServices(urlField.getText).attempt.unsafeRunSync match {
      case Right(serviceResponses) =>
        serviceResponses.foreach(servicesBox.getItems.add)
        servicesBox.getSelectionModel.select(0)
        servicesBox.setDisable(false)
      case Left(error) =>
        responseArea.setText(error.toString)
        methodsBox.setDisable(true)
        servicesBox.setDisable(true)
    }
  }

  submitButton.setOnAction { _ =>
    val serviceResponse = servicesBox.getSelectionModel.getSelectedItem
    val methodDescriptorProto = methodsBox.getSelectionModel.getSelectedItem

    sender.send(fileDescriptorProtos, serviceResponse, methodDescriptorProto, urlField.getText, requestArea.getText).attempt.unsafeRunSync match {
      case Right(responseText) => responseArea.setText(responseText)
      case Left(error: StatusRuntimeException) => responseArea.setText(error.getStatus.getCode.toString + "\n\n" + error.getStatus.getDescription)
      case Left(error) => responseArea.setText(error.getMessage)
    }
  }

  def build: Pane = {
    val leftColumn = new ColumnConstraints()
      .tap(_.setPercentWidth(50))

    val rightColumn = new ColumnConstraints()
      .tap(_.setPercentWidth(50))

    val gridPane = new GridPane()
      .tap(_.getColumnConstraints.add(leftColumn))
      .tap(_.getColumnConstraints.add(rightColumn))
      .tap { pane =>
        leftPane.prefHeightProperty.bind(pane.heightProperty)
        rightPane.prefHeightProperty.bind(pane.heightProperty)
      }
      .tap(_.add(leftPane, 0, 0))
      .tap(_.add(rightPane, 1, 0))

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

  private lazy val rightPane: Pane = new VBox()
    .tap(_.setPadding(new Insets(10, 10, 10, 5)))
    .tap(_.getChildren.add(responseArea))

  private lazy val submitPane: Pane = new HBox()
    .tap(_.setSpacing(10))
    .tap(_.getChildren.add(urlField))
    .tap(_.getChildren.add(syncButton))
    .tap(_.getChildren.add(submitButton))
}
