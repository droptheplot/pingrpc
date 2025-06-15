package pingrpc.form

import com.google.protobuf.Descriptors
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType
import javafx.beans.property.{Property, SimpleBooleanProperty}
import javafx.geometry.Pos
import javafx.scene.control._
import javafx.scene.layout.HBox
import javafx.scene.{Cursor, Node}
import pingrpc.proto.EnumValueDescriptorConverter

import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

case class FormField[T](fieldDescriptor: FieldDescriptor, property: Property[T]) extends Form {
  private val isDisabled: SimpleBooleanProperty = new SimpleBooleanProperty(false)

  override def toNode: Node = {
    val node = fieldDescriptor.getJavaType match {
      case JavaType.STRING | JavaType.BYTE_STRING | JavaType.INT | JavaType.LONG | JavaType.DOUBLE | JavaType.FLOAT =>
        new TextField()
          .tap(_.setPromptText(fieldDescriptor.getType.toString.toLowerCase))
          .tap(_.textProperty.bindBidirectional(property.asInstanceOf[Property[String]]))
      case JavaType.BOOLEAN =>
        new CheckBox(fieldDescriptor.getName)
          .tap(_.setMnemonicParsing(false))
          .tap(_.selectedProperty.bindBidirectional(property.asInstanceOf[Property[java.lang.Boolean]]))
      case JavaType.ENUM =>
        new ComboBox[Descriptors.EnumValueDescriptor]
          .tap(_.setConverter(new EnumValueDescriptorConverter))
          .tap(_.getItems.addAll(fieldDescriptor.getEnumType.getValues))
          .tap(_.getSelectionModel.select(0))
          .tap(_.valueProperty.bindBidirectional(property.asInstanceOf[Property[Descriptors.EnumValueDescriptor]]))
    }

    fieldDescriptor.getJavaType match {
      case JavaType.BOOLEAN =>
        new HBox().tap(_.getChildren.add(node))
      case _ =>
        node.disableProperty.bindBidirectional(isDisabled)

        val label = new Label(fieldDescriptor.getName)
          .tap(_.setCursor(Cursor.HAND))
          .tap(_.setOnMouseClicked { _ => isDisabled.set(!isDisabled.get) })

        new HBox()
          .tap(_.setSpacing(10))
          .tap(_.setAlignment(Pos.CENTER_LEFT))
          .tap(_.getChildren.add(label))
          .tap(_.getChildren.add(node))
    }
  }

  def toValue: Option[Any] = fieldDescriptor.getJavaType match {
    case JavaType.INT | JavaType.LONG | JavaType.FLOAT | JavaType.DOUBLE =>
      Try(property.asInstanceOf[Property[String]]).toOption
        .filterNot(_ => isDisabled.get)
        .map(_.getValue)
        .filter(_ != null)
        .filter(_.nonEmpty)
        .flatMap(parseNumber(fieldDescriptor.getJavaType, _))
    case JavaType.STRING =>
      Try(property.asInstanceOf[Property[String]]).toOption
        .filterNot(_ => isDisabled.get)
        .map(_.getValue)
        .filter(_ != null)
        .filter(_.nonEmpty)
    case JavaType.BOOLEAN =>
      Try(property.asInstanceOf[Property[java.lang.Boolean]]).toOption
        .filterNot(_ => isDisabled.get)
        .map(_.getValue)
        .filter(_ != null)
    case JavaType.ENUM =>
      Try(property.asInstanceOf[Property[Descriptors.EnumValueDescriptor]]).toOption
        .filterNot(_ => isDisabled.get)
        .map(_.getValue)
        .filter(_ != null)
    case _ => None
  }

  private def parseNumber(javaType: JavaType, text: String): Option[Any] = javaType match {
    case JavaType.INT => text.toIntOption.filter(_ != 0)
    case JavaType.LONG => text.toLongOption.filter(_ != 0)
    case JavaType.FLOAT => text.toFloatOption.filter(_ != 0)
    case JavaType.DOUBLE => text.toDoubleOption.filter(_ != 0)
    case _ => None
  }
}
