package pingrpc.ui.views

import javafx.beans.binding.Bindings
import javafx.collections.{FXCollections, MapChangeListener, ObservableList, ObservableMap}
import javafx.scene.Node
import javafx.scene.control.TextField
import javafx.scene.layout.{HBox, Priority, VBox}

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

class MetadataView extends VBox {
  val headers: ObservableMap[String, String] = FXCollections.observableHashMap()
  private val nodes: ObservableList[Node] = FXCollections.observableArrayList()

  headers.addListener(new MapChangeListener[String, String] {
    override def onChanged(change: MapChangeListener.Change[_ <: String, _ <: String]): Unit =
      nodes.setAll(change.getMap.asScala.map { case (k, v) => toRow(k, v) }.toList.asJava)
  })

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

  Bindings.bindContentBidirectional(nodes, getChildren)
}
