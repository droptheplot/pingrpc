package pingrpc.form

import com.google.protobuf.Descriptors.FieldDescriptor
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.Node
import javafx.scene.control.CheckBox

import scala.util.chaining.scalaUtilChainingOps

case class BooleanValue(fieldDescriptor: FieldDescriptor, property: SimpleBooleanProperty) extends FormValue[java.lang.Boolean] {
  override def toNode: Node =
    new CheckBox(fieldDescriptor.getName)
      .tap(_.setMnemonicParsing(false))
      .tap(_.selectedProperty.bindBidirectional(property))
}
