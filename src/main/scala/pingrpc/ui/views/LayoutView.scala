package pingrpc.ui.views

import com.google.protobuf.DescriptorProtos.MethodDescriptorProto
import com.typesafe.scalalogging.StrictLogging
import io.grpc.reflection.v1.ServiceResponse
import javafx.scene.control._
import javafx.scene.layout._
import pingrpc.proto.{MethodDescriptorProtoConverter, ServiceResponseConverter}
import pingrpc.ui.controllers.ActionController
import pingrpc.ui.{grayColor, monospacedFont}

import scala.util.chaining.scalaUtilChainingOps

class LayoutView(controller: ActionController) extends FlowPane with StrictLogging {
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

  private lazy val responseMetadataContainer = new ScrollPane()
  VBox.setVgrow(responseMetadataContainer, Priority.ALWAYS)

  private val tabPane = new TabPane()
    .tap(_.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE))
  VBox.setVgrow(tabPane, Priority.ALWAYS)

  private lazy val curlArea: TextArea = new TextArea()
    .tap(_.setEditable(false))
    .tap(_.setWrapText(true))
    .tap(_.setFont(monospacedFont))
    .tap(_.setPrefHeight(80))

  private val syncButton: Button = new Button("Sync")
    .tap(_.setOnAction(controller.syncAction(urlField, servicesBox, methodsBox)(_)))

  private val submitButton: Button = new Button("Send")
    .tap(_.setDisable(true))
    .tap(_.getStyleClass.add("accent"))
    .tap(_.setOnAction(controller.submitAction(urlField,servicesBox, methodsBox, formPane, tabPane, requestArea, curlArea, responseMetadataContainer, jsonArea)(_)))

  private val responseMessageLabel = new Label("...").tap(_.setTextFill(grayColor))

  private lazy val servicesBox: ComboBox[ServiceResponse] = new ComboBox[ServiceResponse]()
    .tap(_.setConverter(new ServiceResponseConverter))
    .tap(_.setPrefWidth(Double.MaxValue))
    .tap(_.setPromptText("..."))
    .tap(_.setDisable(true))
    .tap(_.setOnAction(controller.serviceAction(urlField, methodsBox)(_)))

  private lazy val methodsBox: ComboBox[MethodDescriptorProto] = new ComboBox[MethodDescriptorProto]()
    .tap(_.setConverter(new MethodDescriptorProtoConverter))
    .tap(_.setPrefWidth(Double.MaxValue))
    .tap(_.setPromptText("..."))
    .tap(_.setDisable(true))
    .tap(_.setOnAction(controller.methodAction(responseMessageLabel, submitButton, formPane)(_)))

  private val requestView = new RequestView(urlField, requestArea, syncButton, submitButton, servicesBox, methodsBox, formPane, tabPane)
  private val responseView = new ResponseView(jsonArea, curlArea, responseMessageLabel, responseMetadataContainer)

  private val gridPane = new GridPane()
    .tap(_.getColumnConstraints.add(new ColumnConstraints().tap(_.setPercentWidth(50))))
    .tap(_.getColumnConstraints.add(new ColumnConstraints().tap(_.setPercentWidth(50))))
    .tap { pane =>
      requestView.prefHeightProperty.bind(pane.heightProperty)
      responseView.prefHeightProperty.bind(pane.heightProperty)
    }
    .tap(_.add(requestView, 0, 0))
    .tap(_.add(responseView, 1, 0))

  setHgap(10)
  setVgap(10)
  setPrefHeight(Double.MaxValue)
  setMinHeight(Double.MaxValue)
  getChildren.add(gridPane)

  gridPane.prefWidthProperty.bind(widthProperty)
  gridPane.prefHeightProperty.bind(heightProperty)
}
