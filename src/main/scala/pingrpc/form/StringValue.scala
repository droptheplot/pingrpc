package pingrpc.form

import com.google.protobuf.Descriptors.FieldDescriptor
import javafx.beans.property.SimpleStringProperty
import javafx.scene.Node
import javafx.scene.control.TextField

import scala.util.chaining.scalaUtilChainingOps

case class StringValue(fieldDescriptor: FieldDescriptor, property: SimpleStringProperty) extends FormValue[String] {
  override def toNode: Node = {
    val textField = new TextField()
      .tap(_.setPromptText(fieldDescriptor.getType.toString.toLowerCase))
      .tap(_.textProperty.bindBidirectional(property))
      .tap(_.disableProperty.bindBidirectional(isDisabled))

    fieldContainer(textField)
  }

  override def toValue: Option[String] = super.toValue.filter(_.nonEmpty)
}
