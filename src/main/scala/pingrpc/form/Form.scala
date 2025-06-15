package pingrpc.form

import com.google.protobuf.Descriptors.FieldDescriptor.JavaType
import com.google.protobuf.Descriptors.{Descriptor, FieldDescriptor}
import com.google.protobuf.{Descriptors, Message}
import javafx.beans.property.{SimpleBooleanProperty, SimpleObjectProperty, SimpleStringProperty}
import javafx.scene.Node

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

trait Form {
  def toNode: Node
}

object Form {

  def build(descriptor: Descriptor, messageOpt: Option[Message]): Form =
    FormRoot(descriptor, descriptor.getFields.asScala.map(Form.build(_, messageOpt)).toList)

  private def build(fieldDescriptor: FieldDescriptor, messageOpt: Option[Message]): Form = fieldDescriptor.getJavaType match {
    case JavaType.MESSAGE =>
      val nestedMessageOpt: Option[Message] = messageOpt.flatMap { message =>
        Option(message.getField(fieldDescriptor))
          .collect {
            case message: Message => message
            case v: java.util.List[Message] if v.size > 0 => v.getFirst
          }
      }

      FormMessage(fieldDescriptor, fieldDescriptor.getMessageType.getFields.asScala.map(Form.build(_, nestedMessageOpt)).toList)
    case _ =>
      val valueOpt: Option[Any] = messageOpt
        .map(message => message.getField(fieldDescriptor))
        .map {
          case v: java.util.List[_] if v.size > 0 => v.getFirst
          case v => v
        }

      val property = fieldDescriptor.getJavaType match {
        case JavaType.STRING | JavaType.BYTE_STRING | JavaType.INT | JavaType.LONG | JavaType.FLOAT | JavaType.DOUBLE =>
          new SimpleStringProperty()
            .tap(property => valueOpt.flatMap(value => Option(value).collect(anyToString(_))).foreach(property.set))
        case JavaType.BOOLEAN =>
          new SimpleBooleanProperty()
            .tap(property => valueOpt.flatMap(value => Option(value).collect { case v: Boolean if v => v }).foreach(property.set))
        case JavaType.ENUM =>
          new SimpleObjectProperty[Descriptors.EnumValueDescriptor](fieldDescriptor.getEnumType.getValues.getFirst)
            .tap(property => valueOpt.flatMap(value => Option(value).collect { case v: Descriptors.EnumValueDescriptor => v }).foreach(property.set))
      }

      FormField(fieldDescriptor, property)
  }

  private def anyToString(any: Any): String = any match {
    case s: String => s
    case n: java.lang.Number => n.toString
    case _ => ""
  }
}
