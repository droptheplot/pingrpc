package pingrpc.form

import com.google.protobuf.Descriptors
import com.google.protobuf.Descriptors.FieldDescriptor
import javafx.beans.property._
import javafx.geometry.Pos
import javafx.scene.control._
import javafx.scene.layout.HBox
import javafx.scene.{Cursor, Node}
import pingrpc.proto.EnumValueDescriptorConverter

import java.text.NumberFormat
import scala.util.chaining.scalaUtilChainingOps

trait FormField[T] extends Form {
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

object FormField {
  private val numberFormat = NumberFormat.getNumberInstance.tap(_.setGroupingUsed(false))

  private val numberStringConverter = new HiddenZeroNumberStringConverter(numberFormat)

  case class StringField(fieldDescriptor: FieldDescriptor, property: SimpleStringProperty) extends FormField[String] {
    override def toNode: Node = {
      val textField = new TextField()
        .tap(_.setPromptText(fieldDescriptor.getType.toString.toLowerCase))
        .tap(_.textProperty.bindBidirectional(property))
        .tap(_.disableProperty.bindBidirectional(isDisabled))

      fieldContainer(textField)
    }

    override def toValue: Option[String] = super.toValue.filter(_.nonEmpty)
  }

  case class LongField(fieldDescriptor: FieldDescriptor, property: SimpleLongProperty) extends FormField[Number] {
    override def toNode: Node = {
      val textField = new TextField()
        .tap(_.setPromptText(fieldDescriptor.getType.toString.toLowerCase))
        .tap(_.textProperty.bindBidirectional(property, numberStringConverter))
        .tap(_.disableProperty.bindBidirectional(isDisabled))

      fieldContainer(textField)
    }
  }

  case class IntField(fieldDescriptor: FieldDescriptor, property: SimpleIntegerProperty) extends FormField[Number] {
    override def toNode: Node = {
      val textField = new TextField()
        .tap(_.setPromptText(fieldDescriptor.getType.toString.toLowerCase))
        .tap(_.textProperty.bindBidirectional(property, numberStringConverter))
        .tap(_.disableProperty.bindBidirectional(isDisabled))

      fieldContainer(textField)
    }
  }

  case class FloatField(fieldDescriptor: FieldDescriptor, property: SimpleFloatProperty) extends FormField[Number] {
    override def toNode: Node = {
      val textField = new TextField()
        .tap(_.setPromptText(fieldDescriptor.getType.toString.toLowerCase))
        .tap(_.textProperty.bindBidirectional(property, numberStringConverter))
        .tap(_.disableProperty.bindBidirectional(isDisabled))

      fieldContainer(textField)
    }
  }

  case class DoubleField(fieldDescriptor: FieldDescriptor, property: SimpleDoubleProperty) extends FormField[Number] {
    override def toNode: Node = {
      val textField = new TextField()
        .tap(_.setPromptText(fieldDescriptor.getType.toString.toLowerCase))
        .tap(_.textProperty.bindBidirectional(property, numberStringConverter))
        .tap(_.disableProperty.bindBidirectional(isDisabled))

      fieldContainer(textField)
    }
  }

  case class BooleanField(fieldDescriptor: FieldDescriptor, property: SimpleBooleanProperty) extends FormField[java.lang.Boolean] {
    override def toNode: Node =
      new CheckBox(fieldDescriptor.getName)
        .tap(_.setMnemonicParsing(false))
        .tap(_.selectedProperty.bindBidirectional(property))
  }

  case class EnumField(fieldDescriptor: FieldDescriptor, property: Property[Descriptors.EnumValueDescriptor]) extends FormField[Descriptors.EnumValueDescriptor] {
    override def toNode: Node = {
      val comboBox = new ComboBox[Descriptors.EnumValueDescriptor]
        .tap(_.setConverter(new EnumValueDescriptorConverter))
        .tap(_.getItems.addAll(fieldDescriptor.getEnumType.getValues))
        .tap(_.valueProperty.bindBidirectional(property))
        .tap(_.disableProperty.bindBidirectional(isDisabled))

      fieldContainer(comboBox)
    }
  }
}
