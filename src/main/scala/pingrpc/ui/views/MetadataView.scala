package pingrpc.ui.views

import javafx.scene.control.{ScrollPane, TextField}
import javafx.scene.layout.{HBox, Priority, VBox}

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

class MetadataView(headers: Map[String, String], container: ScrollPane) extends VBox {
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

  setSpacing(10)
  getChildren.addAll(headers.map((toRow _).tupled(_)).toList.asJava)

  prefWidthProperty.bind(container.widthProperty)
}
