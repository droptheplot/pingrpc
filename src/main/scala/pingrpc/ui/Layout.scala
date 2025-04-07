package pingrpc.ui

import cats.effect.unsafe.implicits.global
import com.google.protobuf.DescriptorProtos.{FileDescriptorProto, MethodDescriptorProto}
import com.typesafe.scalalogging.StrictLogging
import io.grpc.StatusRuntimeException
import io.grpc.reflection.v1.ServiceResponse
import javafx.scene.control._
import javafx.scene.layout._
import pingrpc.form.Form
import pingrpc.grpc.{CurlPrinter, FullMessageName, ReflectionManager, Sender}
import pingrpc.proto.{MethodDescriptorProtoConverter, ProtoUtils, ServiceResponseConverter}

import scala.collection.mutable
import scala.util.chaining.scalaUtilChainingOps

class Layout(reflectionManager: ReflectionManager, sender: Sender) extends StrictLogging {
  private val fileDescriptorProtos = mutable.ListBuffer.empty[FileDescriptorProto]

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

  private lazy val formPane = new ScrollPane()
  VBox.setVgrow(formPane, Priority.ALWAYS)

  private val tabPane = new TabPane()
    .tap(_.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE))
  VBox.setVgrow(tabPane, Priority.ALWAYS)

  private lazy val curlArea: TextArea = new TextArea()
    .tap(_.setEditable(false))
    .tap(_.setWrapText(true))
    .tap(_.setFont(monospacedFont))
    .tap(_.setPrefHeight(80))

  private val syncButton: Button = new Button("Sync")

  private val submitButton: Button = new Button("Send")
    .tap(_.setDisable(true))
    .tap(_.getStyleClass.add("accent"))

  private val requestMessageLabel = new Label("...").tap(_.setTextFill(grayColor))

  private val responseMessageLabel = new Label("...").tap(_.setTextFill(grayColor))

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
        FullMessageName.parse(methodDescriptorProto.getInputType).foreach { fullMessageName =>
          requestMessageLabel.setText(fullMessageName.toString)
        }
        FullMessageName.parse(methodDescriptorProto.getOutputType).foreach { fullMessageName =>
          responseMessageLabel.setText(fullMessageName.toString)
        }
        submitButton.setDisable(false)

        val fileDescriptors = ProtoUtils.toFileDescriptors(fileDescriptorProtos.toList)

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
      case _ => ()
    }
  }

  syncButton.setOnAction { _ =>
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
      method = ProtoUtils.buildMethodName(serviceResponse, methodDescriptorProto)
      json = tabPane.getSelectionModel.getSelectedItem.getId match {
        case "form" => formPane.getUserData.asInstanceOf[Form].toJson.asObject.filter(_.nonEmpty).map(_.toJson.toString).getOrElse("{}")
        case "json" => requestArea.getText
        case _ => "{}"
      }
      _ = logger.info(json)
      curlText = CurlPrinter.print(serviceResponse, methodDescriptorProto, urlField.getText, json)
      _ = curlArea.setText(curlText)
      responseText <- sender.send(requestDescriptor, responseDescriptor, method, urlField.getText, json).attempt.unsafeRunSync
    } yield responseText) match {
      case Right(responseText) =>
        jsonArea.setText(responseText)
      case Left(_: StatusRuntimeException) =>
        jsonArea.clear()
      case Left(_) =>
        jsonArea.clear()
    }
  }

  def build: Pane = {
    val requestPane = new RequestPane(urlField, requestArea, syncButton, submitButton, servicesBox, methodsBox, formPane, tabPane).build
    val responsePane = new ResponsePane(jsonArea, curlArea, responseMessageLabel).build

    val requestColumn = new ColumnConstraints().tap(_.setPercentWidth(50))
    val responseColumn = new ColumnConstraints().tap(_.setPercentWidth(50))

    val gridPane = new GridPane()
      .tap(_.getColumnConstraints.add(requestColumn))
      .tap(_.getColumnConstraints.add(responseColumn))
      .tap { pane =>
        requestPane.prefHeightProperty.bind(pane.heightProperty)
        responsePane.prefHeightProperty.bind(pane.heightProperty)
      }
      .tap(_.add(requestPane, 0, 0))
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
}
