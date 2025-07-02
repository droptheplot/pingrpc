package pingrpc.ui.views

import javafx.collections.{FXCollections, ListChangeListener, ObservableList}
import javafx.scene.control.{Button, TextField}
import javafx.scene.layout.{HBox, Priority, VBox}
import pingrpc.ui.Header

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

class MetadataView(editable: Boolean) extends VBox {
  val headers: ObservableList[Header] = FXCollections.observableArrayList()

  private val container: VBox = new VBox()
    .tap(_.setSpacing(10))

  private val addButton = new Button("Add")
    .tap(_.setOnAction { _ => headers.add(Header.empty) })

  private def createRemoveButton(id: Int) = new Button("Remove")
    .tap(_.setOnAction { _ => headers.remove(id) })

  headers.addListener(new ListChangeListener[Header] {
    override def onChanged(change: ListChangeListener.Change[_ <: Header]): Unit = {
      val nodes = change.getList.asScala.zipWithIndex.map((toRow _).tupled).toList.asJava

      container.getChildren.clear()
      container.getChildren.addAll(nodes)
    }
  })

  private def toRow(header: Header, id: Int): HBox = {
    val keyField = new TextField()
      .tap(_.setEditable(editable))
      .tap(_.textProperty.bindBidirectional(header.key))
    HBox.setHgrow(keyField, Priority.ALWAYS)

    val valueField = new TextField()
      .tap(_.setEditable(editable))
      .tap(_.textProperty.bindBidirectional(header.value))
    HBox.setHgrow(valueField, Priority.ALWAYS)

    new HBox()
      .tap(_.getChildren.addAll(keyField, valueField))
      .tap(hbox => if (editable) hbox.getChildren.add(createRemoveButton(id)))
      .tap(_.setSpacing(10))
  }

  setSpacing(10)

  getChildren.add(container)

  if (editable) getChildren.add(addButton)
}
