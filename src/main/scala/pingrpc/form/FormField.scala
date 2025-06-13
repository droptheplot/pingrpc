package pingrpc.form

import com.google.protobuf.Descriptors
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.scene.control.{CheckBox, ComboBox, Label, TextField}
import javafx.scene.layout.HBox
import javafx.scene.{Cursor, Node}

import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

case class FormField(fieldDescriptor: FieldDescriptor, node: Node) extends Form {
  private val isDisabled: SimpleBooleanProperty = new SimpleBooleanProperty(false)

  override def toNode: Node = fieldDescriptor.getJavaType match {
    case JavaType.BOOLEAN =>
      new HBox().tap(_.getChildren.add(node))
    case _ =>
      node.disableProperty.bindBidirectional(isDisabled)

      val label = new Label(fieldDescriptor.getName)
        .tap(_.setCursor(Cursor.HAND))
        .tap(_.setOnMouseClicked { _ => isDisabled.set(!isDisabled.get) })

      new HBox()
        .tap(_.setSpacing(10))
        .tap(_.setAlignment(Pos.CENTER_LEFT))
        .tap(_.getChildren.add(label))
        .tap(_.getChildren.add(node))
  }

  def toValue: Option[Any] = fieldDescriptor.getJavaType match {
    case JavaType.INT | JavaType.LONG | JavaType.FLOAT | JavaType.DOUBLE =>
      Try(node.asInstanceOf[TextField]).toOption
        .filterNot(_.isDisabled)
        .map(_.getText)
        .flatMap(parseNumber(fieldDescriptor.getJavaType, _))
    case JavaType.STRING =>
      Try(node.asInstanceOf[TextField]).toOption
        .filterNot(_.isDisabled)
        .map(_.getText)
        .filter(_.nonEmpty)
    case JavaType.BOOLEAN =>
      Try(node.asInstanceOf[CheckBox]).toOption
        .filterNot(_.isDisabled)
        .map(_.isSelected)
        .filter(_ == true)
    case JavaType.ENUM =>
      Try(node.asInstanceOf[ComboBox[Descriptors.EnumValueDescriptor]]).toOption
        .filterNot(_.isDisabled)
        .map(_.getSelectionModel.getSelectedItem)
        .filterNot(_.getIndex == 0)
    case _ => None
  }
}
