package pingrpc.form.values

import com.google.protobuf.Descriptors.FieldDescriptor
import javafx.beans.property.{Property, SimpleBooleanProperty}
import javafx.scene.Node
import javafx.scene.control.{CheckBox, Label}
import pingrpc.form.FormValue

import java.lang
import scala.util.chaining.scalaUtilChainingOps

case class UnknownValue(fieldDescriptor: FieldDescriptor) extends FormValue[Any] {
  override def default: Any = ???

  override def property: Property[Any] = ???

  override def toNode: Node =
    new Label(s"${fieldDescriptor.getName} (not supported yet)")
}
