package pingrpc.ui

import javafx.geometry.Insets
import javafx.scene.control.{Label, TextArea}
import javafx.scene.layout.{Pane, Priority, VBox}

import scala.util.chaining.scalaUtilChainingOps

class ResponsePane(jsonArea: TextArea, curlArea: TextArea, responseMessageLabel: Label) {
  private val responseLabel = new Label("RESPONSE")
    .tap(_.setFont(boldFont))
    .tap(_.setTextFill(grayColor))

  private val statusLabel = new Label("GRPCURL")
    .tap(_.setFont(boldFont))
    .tap(_.setTextFill(grayColor))

  private val contentBox = new VBox()
    .tap(_.getChildren.add(jsonArea))
  VBox.setVgrow(contentBox, Priority.ALWAYS)

  def build: Pane = new VBox()
    .tap(_.setSpacing(10))
    .tap(_.setPadding(new Insets(10, 10, 10, 5)))
    .tap(_.getChildren.add(responseLabel))
    .tap(_.getChildren.add(responseMessageLabel))
    .tap(_.getChildren.add(contentBox))
    .tap(_.getChildren.add(statusLabel))
    .tap(_.getChildren.add(curlArea))
}
