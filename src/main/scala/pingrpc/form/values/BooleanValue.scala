package pingrpc.form.values

import com.google.protobuf.Descriptors.FieldDescriptor
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.Node
import javafx.scene.control.CheckBox
import pingrpc.form.FormValue

import java.lang
import scala.util.chaining.scalaUtilChainingOps

case class BooleanValue(fieldDescriptor: FieldDescriptor, property: SimpleBooleanProperty) extends FormValue[java.lang.Boolean] {
  override def default: lang.Boolean = false

  override def toNode: Node =
    new CheckBox(fieldDescriptor.getName)
      .tap(_.setMnemonicParsing(false))
      .tap(_.selectedProperty.bindBidirectional(property))
}
