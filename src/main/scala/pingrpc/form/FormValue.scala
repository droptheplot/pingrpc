package pingrpc.form

import com.google.protobuf.Descriptors.FieldDescriptor
import javafx.beans.property._
import javafx.geometry.Pos
import javafx.scene.control._
import javafx.scene.layout.HBox
import javafx.scene.{Cursor, Node}

import scala.util.chaining.scalaUtilChainingOps

trait FormValue[T] extends Form with FormField {
  protected val isDisabled: SimpleBooleanProperty = new SimpleBooleanProperty(false)

  def fieldDescriptor: FieldDescriptor

  def property: Property[T]

  def toValue: Option[T] = Option(property.getValue).filterNot(_ => isDisabled.get)

  def fieldContainer(node: Node): HBox = new HBox()
    .tap(_.setSpacing(10))
    .tap(_.setAlignment(Pos.CENTER_LEFT))
    .tap(_.getChildren.add(label))
    .tap(_.getChildren.add(node))

  private val label: Label = new Label(fieldDescriptor.getName)
    .tap(_.setCursor(Cursor.HAND))
    .tap(_.setOnMouseClicked { _ => isDisabled.set(!isDisabled.get) })
}
