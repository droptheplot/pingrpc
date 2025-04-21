package pingrpc.ui.views

import javafx.geometry.Insets
import javafx.scene.control._
import javafx.scene.layout.{Priority, VBox}
import pingrpc.ui.{boldFont, grayColor}

import scala.util.chaining.scalaUtilChainingOps

class ResponseView(jsonArea: TextArea, curlArea: TextArea, responseMessageLabel: Label, responseHeadersPane: ScrollPane) extends VBox {
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

  setSpacing(10)
  setPadding(new Insets(10, 10, 10, 5))
  getChildren.add(responseLabel)
  getChildren.add(responseMessageLabel)
  getChildren.add(tabPane)
  getChildren.add(statusLabel)
  getChildren.add(curlArea)
}
