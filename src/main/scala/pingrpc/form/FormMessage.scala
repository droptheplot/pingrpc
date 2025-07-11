package pingrpc.form

import com.google.protobuf.Descriptors.{Descriptor, FieldDescriptor}
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.Label
import javafx.scene.layout._
import javafx.scene.{Cursor, Node}
import pingrpc.ui.boldFont

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

case class FormMessage(fieldDescriptor: FieldDescriptor, children: Seq[Form]) extends Form with MessageBuilder with FormField {
  def descriptor: Descriptor = fieldDescriptor.getMessageType

  val isDisabled: SimpleBooleanProperty = new SimpleBooleanProperty(false)

  override def toNode: Node = {
    val message = new VBox()
      .tap(_.setSpacing(10))
      .tap(_.getStyleClass.add("form-message"))
      .tap(_.getChildren.add(new Label(fieldDescriptor.getMessageType.getFullName).tap(_.setFont(boldFont))))
      .tap(_.getChildren.addAll(children.map(_.toNode).asJava))
      .tap(_.disableProperty.bindBidirectional(isDisabled))

    val label = new Label(fieldDescriptor.getName)
      .tap(_.setCursor(Cursor.HAND))
      .tap(_.setOnMouseClicked { _ => isDisabled.set(!isDisabled.get) })

    new VBox()
      .tap(_.setSpacing(10))
      .tap(_.getChildren.add(label))
      .tap(_.getChildren.add(message))
  }
}
