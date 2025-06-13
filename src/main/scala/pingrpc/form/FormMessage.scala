package pingrpc.form

import com.google.protobuf.Descriptors.{Descriptor, FieldDescriptor}
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.layout._
import javafx.scene.{Cursor, Node}
import pingrpc.ui.{boldFont, lightGrayColor}

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

case class FormMessage(fieldDescriptor: FieldDescriptor, children: Seq[Form]) extends Form with FormMessageBuilder {
  def descriptor: Descriptor = fieldDescriptor.getMessageType

  private val isDisabled: SimpleBooleanProperty = new SimpleBooleanProperty(false)

  override def toNode: Node = {
    val message = new VBox()
      .tap(_.setSpacing(10))
      .tap(_.setPadding(new Insets(0, 0, 0, 10)))
      .tap(_.setBorder(new Border(new BorderStroke(lightGrayColor, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 0, 2)))))
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
