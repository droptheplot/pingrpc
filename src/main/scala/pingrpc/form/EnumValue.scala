package pingrpc.form

import com.google.protobuf.Descriptors
import com.google.protobuf.Descriptors.FieldDescriptor
import javafx.beans.property.Property
import javafx.scene.Node
import javafx.scene.control.ComboBox
import pingrpc.proto.EnumValueDescriptorConverter

import scala.util.chaining.scalaUtilChainingOps

case class EnumValue(fieldDescriptor: FieldDescriptor, property: Property[Descriptors.EnumValueDescriptor]) extends FormValue[Descriptors.EnumValueDescriptor] {
  override def toNode: Node = {
    val comboBox = new ComboBox[Descriptors.EnumValueDescriptor]
      .tap(_.setConverter(new EnumValueDescriptorConverter))
      .tap(_.getItems.addAll(fieldDescriptor.getEnumType.getValues))
      .tap(_.valueProperty.bindBidirectional(property))
      .tap(_.disableProperty.bindBidirectional(isDisabled))

    fieldContainer(comboBox)
  }
}
