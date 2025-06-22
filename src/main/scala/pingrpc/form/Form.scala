package pingrpc.form

import com.google.protobuf.Descriptors.FieldDescriptor.JavaType
import com.google.protobuf.Descriptors.{Descriptor, FieldDescriptor}
import com.google.protobuf.{Descriptors, Message}
import javafx.beans.property._
import javafx.scene.Node

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

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
      fieldDescriptor.getJavaType match {
        case JavaType.STRING =>
          val property = new SimpleStringProperty()
          messageOpt.flatMap(getValue[String](_, fieldDescriptor)).foreach(property.setValue)

          FormField.StringField(fieldDescriptor, property)
        case JavaType.BOOLEAN =>
          val property = new SimpleBooleanProperty()
          messageOpt.flatMap(getValue[java.lang.Boolean](_, fieldDescriptor)).foreach(property.setValue)

          FormField.BooleanField(fieldDescriptor, property)
        case JavaType.ENUM =>
          val property = new SimpleObjectProperty[Descriptors.EnumValueDescriptor](fieldDescriptor.getEnumType.getValues.getFirst)

          FormField.EnumField(fieldDescriptor, property)
        case JavaType.INT =>
          val property = new SimpleIntegerProperty()
          messageOpt.flatMap(getValue[java.lang.Integer](_, fieldDescriptor)).foreach(property.setValue)

          FormField.IntField(fieldDescriptor, property)
        case JavaType.LONG =>
          val property = new SimpleLongProperty()
          messageOpt.flatMap(getValue[java.lang.Long](_, fieldDescriptor)).foreach(property.setValue)

          FormField.LongField(fieldDescriptor, property)
        case JavaType.FLOAT =>
          val property = new SimpleFloatProperty()

          messageOpt.flatMap(getValue[java.lang.Float](_, fieldDescriptor)).foreach(property.setValue)

          FormField.FloatField(fieldDescriptor, property)
        case JavaType.DOUBLE =>
          val property = new SimpleDoubleProperty()
          messageOpt.flatMap(getValue[java.lang.Double](_, fieldDescriptor)).foreach(property.setValue)

          FormField.DoubleField(fieldDescriptor, property)
      }
  }

  private def getValue[T](message: Message, fieldDescriptor: FieldDescriptor): Option[T] =
    message.getField(fieldDescriptor) match {
      case values: java.util.List[T] if values.size > 0 => Option(values.getFirst)
      case values: java.util.List[T] if values.isEmpty => None
      case value: T if value.isInstanceOf[T] => Option(value)
      case _ => None
    }
}
