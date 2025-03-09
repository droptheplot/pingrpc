package pingrpc.ui

import javafx.geometry.Insets
import javafx.scene.control.{TextArea, ToggleButton, ToggleGroup}
import javafx.scene.layout.{HBox, Pane, Priority, VBox}

import scala.util.chaining.scalaUtilChainingOps

class ResponsePane(jsonArea: TextArea, curlArea: TextArea, statusArea: TextArea) {
  private val toggleGroup = new ToggleGroup()

  private val jsonButton = new ToggleButton("JSON")
    .tap(_.setToggleGroup(toggleGroup))
    .tap(_.setSelected(true))
    .tap(_.setOnAction { _ =>
      contentBox.getChildren.setAll(jsonArea)
    })

  private val grpcurlButton = new ToggleButton("gRPCurl")
    .tap(_.setToggleGroup(toggleGroup))
    .tap(_.setOnAction { _ =>
      contentBox.getChildren.setAll(curlArea)
    })

  private val menuBox = new HBox()
    .tap(_.setSpacing(10))
    .tap(_.getChildren.add(jsonButton))
    .tap(_.getChildren.add(grpcurlButton))

  private val contentBox = new VBox()
    .tap(_.getChildren.add(jsonArea))
  VBox.setVgrow(contentBox, Priority.ALWAYS)

  def build: Pane = new VBox()
    .tap(_.setSpacing(10))
    .tap(_.setPadding(new Insets(10, 10, 10, 5)))
    .tap(_.getChildren.add(menuBox))
    .tap(_.getChildren.add(contentBox))
    .tap(_.getChildren.add(statusArea))
}
