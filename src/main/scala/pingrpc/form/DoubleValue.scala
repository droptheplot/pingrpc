package pingrpc.form

import com.google.protobuf.Descriptors.FieldDescriptor
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.Node
import javafx.scene.control.TextField

import scala.util.chaining.scalaUtilChainingOps

case class DoubleValue(fieldDescriptor: FieldDescriptor, property: SimpleDoubleProperty) extends FormValue[Number] {
  override def toNode: Node = {
    val textField = new TextField()
      .tap(_.setPromptText(fieldDescriptor.getType.toString.toLowerCase))
      .tap(_.textProperty.bindBidirectional(property, numberStringConverter))
      .tap(_.disableProperty.bindBidirectional(isDisabled))

    fieldContainer(textField)
  }
}
