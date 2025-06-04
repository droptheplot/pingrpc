package pingrpc.ui.views

import javafx.geometry.Insets
import javafx.scene.control._
import javafx.scene.layout.{Priority, VBox}
import pingrpc.ui.{boldFont, grayColor, monospacedFont}

import scala.util.chaining.scalaUtilChainingOps

class ResponseView extends VBox {
  val responseMessageLabel: Label = new Label("...")
    .tap(_.setTextFill(grayColor))

  val jsonArea: TextArea = new TextArea()
    .tap(_.setEditable(false))
    .tap(_.setWrapText(true))
    .tap(_.setFont(monospacedFont))
  VBox.setVgrow(jsonArea, Priority.ALWAYS)

  val metadataView: MetadataView = new MetadataView()

  private val responseMetadataPane: ScrollPane = new ScrollPane()
    .tap(_.setContent(metadataView))
  VBox.setVgrow(responseMetadataPane, Priority.ALWAYS)

  metadataView.prefWidthProperty.bind(responseMetadataPane.widthProperty)

  val curlArea: TextArea = new TextArea()
    .tap(_.setEditable(false))
    .tap(_.setWrapText(true))
    .tap(_.setFont(monospacedFont))
    .tap(_.setPrefHeight(80))

  private val responseLabel = new Label("RESPONSE")
    .tap(_.setFont(boldFont))
    .tap(_.setTextFill(grayColor))

  private val statusLabel = new Label("GRPCURL")
    .tap(_.setFont(boldFont))
    .tap(_.setTextFill(grayColor))

  private val contentBox = new VBox()
    .tap(_.getChildren.add(jsonArea))
  VBox.setVgrow(contentBox, Priority.ALWAYS)

  private val tabPane = new TabPane()
    .tap(_.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE))

  VBox.setVgrow(tabPane, Priority.ALWAYS)

  private val jsonContainer = new VBox()
    .tap(_.setPadding(new Insets(10, 0, 0, 0)))
    .tap(_.getChildren.add(jsonArea))

  private val responseHeadersContainer = new VBox()
    .tap(_.setPadding(new Insets(10, 0, 0, 0)))
    .tap(_.getChildren.add(responseMetadataPane))

  private val jsonTab = new Tab("JSON", jsonContainer)
  private val headersTab = new Tab("Metadata", responseHeadersContainer)

  tabPane.getTabs.add(jsonTab)
  tabPane.getTabs.add(headersTab)

  setSpacing(10)
  setPadding(new Insets(10, 10, 10, 5))
  getChildren.add(responseLabel)
  getChildren.add(responseMessageLabel)
  getChildren.add(tabPane)
  getChildren.add(statusLabel)
  getChildren.add(curlArea)
}
