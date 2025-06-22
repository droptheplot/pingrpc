package pingrpc.form

import com.google.protobuf.Descriptors.FieldDescriptor.JavaType
import com.google.protobuf.Descriptors.{Descriptor, FieldDescriptor}
import com.google.protobuf.{Descriptors, Message}
import javafx.beans.property._
import javafx.scene.Node

import scala.jdk.CollectionConverters._
import scala.util.Try

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

          messageOpt.foreach { message =>
            Try(message.getField(fieldDescriptor).asInstanceOf[String]).foreach(property.setValue)
          }

          FormField.StringField(fieldDescriptor, property)
        case JavaType.BOOLEAN =>
          val property = new SimpleBooleanProperty()

          messageOpt.foreach { message =>
            Try(message.getField(fieldDescriptor).asInstanceOf[java.lang.Boolean]).foreach(property.setValue)
          }

          FormField.BooleanField(fieldDescriptor, property)
        case JavaType.ENUM =>
          val property = new SimpleObjectProperty[Descriptors.EnumValueDescriptor](fieldDescriptor.getEnumType.getValues.getFirst)

          messageOpt.foreach { message =>
            Try(message.getField(fieldDescriptor).asInstanceOf[Descriptors.EnumValueDescriptor]).foreach(property.setValue)
          }

          FormField.EnumField(fieldDescriptor, property)
        case JavaType.INT =>
          val property = new SimpleIntegerProperty()

          messageOpt.foreach { message =>
            Try(message.getField(fieldDescriptor).asInstanceOf[java.lang.Integer]).foreach(property.setValue)
          }

          FormField.IntField(fieldDescriptor, property)
        case JavaType.LONG =>
          val property = new SimpleLongProperty()

          messageOpt.foreach { message =>
            Try(message.getField(fieldDescriptor).asInstanceOf[java.lang.Long]).foreach(property.setValue)
          }

          FormField.LongField(fieldDescriptor, property)
        case JavaType.FLOAT =>
          val property = new SimpleFloatProperty()

          messageOpt.foreach { message =>
            Try(message.getField(fieldDescriptor).asInstanceOf[java.lang.Float]).foreach(property.setValue)
          }

          FormField.FloatField(fieldDescriptor, property)
        case JavaType.DOUBLE =>
          val property = new SimpleDoubleProperty()

          messageOpt.foreach { message =>
            Try(message.getField(fieldDescriptor).asInstanceOf[java.lang.Double]).foreach(property.setValue)
          }

          FormField.DoubleField(fieldDescriptor, property)
      }
  }
}
