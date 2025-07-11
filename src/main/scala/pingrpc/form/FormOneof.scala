package pingrpc.form

import com.google.protobuf.Descriptors.OneofDescriptor
import com.google.protobuf.Message
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.control.{ComboBox, Label}
import javafx.scene.layout._
import pingrpc.proto.FormFieldConverter

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

case class FormOneof(descriptor: OneofDescriptor, children: Seq[FormField], selected: Option[FormField]) extends Form {
  private val current = new SimpleObjectProperty[FormField](selected.orNull)

  override def toNode: Node = {
    val message = new HBox()
      .tap(_.setSpacing(10))
      .tap(_.getStyleClass.add("form-message"))
      .tap(_.getChildren.add(current.getValue.toNode))

    val label = new Label(descriptor.getName)

    val comboBox = new ComboBox[FormField]
      .tap(_.setConverter(new FormFieldConverter))
      .tap(_.getItems.addAll(children.asJava))
      .tap(_.getSelectionModel.select(current.getValue))
      .tap(_.setOnAction { e =>
        val selected = e.getSource.asInstanceOf[ComboBox[FormField]].getSelectionModel.getSelectedItem
        current.setValue(selected)
        message.getChildren.clear()
        message.getChildren.add(selected.toNode)
      })

    new VBox()
      .tap(_.setSpacing(10))
      .tap(_.getChildren.add(label))
      .tap(_.getChildren.add(comboBox))
      .tap(_.getChildren.add(message))
  }

  def toForm: FormField = current.getValue
}

object FormOneof {
  def build(descriptor: OneofDescriptor, fields: Seq[FormField], messageOpt: Option[Message]): FormOneof = {
    val selected = for {
      fieldDescriptor <- messageOpt.map(_.getOneofFieldDescriptor(descriptor))
      formField <- fields.find(_.fieldDescriptor == fieldDescriptor)
    } yield formField

    FormOneof(descriptor, fields, selected)
  }
}
