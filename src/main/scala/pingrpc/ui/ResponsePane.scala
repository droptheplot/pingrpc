package pingrpc.ui

import javafx.geometry.Insets
import javafx.scene.control.{Label, ScrollPane, Tab, TabPane, TextArea}
import javafx.scene.layout.{Pane, Priority, VBox}

import scala.util.chaining.scalaUtilChainingOps

class ResponsePane(jsonArea: TextArea, curlArea: TextArea, responseMessageLabel: Label, responseHeadersPane: ScrollPane) {
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
    .tap(_.getChildren.add(responseHeadersPane))

  private val jsonTab = new Tab("JSON", jsonContainer)
  private val headersTab = new Tab("Metadata", responseHeadersContainer)

  tabPane.getTabs.add(jsonTab)
  tabPane.getTabs.add(headersTab)

  def build: Pane = new VBox()
    .tap(_.setSpacing(10))
    .tap(_.setPadding(new Insets(10, 10, 10, 5)))
    .tap(_.getChildren.add(responseLabel))
    .tap(_.getChildren.add(responseMessageLabel))
    .tap(_.getChildren.add(tabPane))
    .tap(_.getChildren.add(statusLabel))
    .tap(_.getChildren.add(curlArea))
}
