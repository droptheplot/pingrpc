package pingrpc.headers

import javafx.scene.control.TextField
import javafx.scene.layout.{HBox, Pane, Priority, VBox}

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

class Headers(headers: Map[String, String]) {
  def toPane: Pane = new VBox()
    .tap(_.setSpacing(10))
    .tap(_.getChildren.addAll(headers.map((toRow _).tupled(_)).toList.asJava))

  private def toRow(key: String, value: String): HBox = {
    val keyField = new TextField()
      .tap(_.setText(key))
      .tap(_.setEditable(false))
    HBox.setHgrow(keyField, Priority.ALWAYS)

    val valueField = new TextField()
      .tap(_.setText(value))
      .tap(_.setEditable(false))
    HBox.setHgrow(valueField, Priority.ALWAYS)

    new HBox()
      .tap(_.getChildren.addAll(keyField, valueField))
      .tap(_.setSpacing(10))
  }
}
