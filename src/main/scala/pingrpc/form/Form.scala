package pingrpc.form

import com.google.protobuf.Descriptors.FieldDescriptor.JavaType
import com.google.protobuf.Descriptors.{Descriptor, FieldDescriptor}
import com.google.protobuf.{Descriptors, Message}
import javafx.beans.property._
import javafx.scene.Node
import pingrpc.form.values.{BooleanValue, DoubleValue, EnumValue, FloatValue, IntValue, LongValue, StringValue, UnknownValue}

import scala.jdk.CollectionConverters._

trait Form {
  def toNode: Node
}

object Form {

  def build(descriptor: Descriptor, messageOpt: Option[Message]): Form =
    FormRoot(descriptor, descriptor.getFields.asScala.map(Form.build(_, messageOpt)).toList)

  private def build(fieldDescriptor: FieldDescriptor, messageOpt: Option[Message]): FormField =
    fieldDescriptor.getJavaType match {
      case JavaType.MESSAGE =>
        val nestedMessageOpt: Option[Message] = messageOpt.flatMap { message =>
          Option(message.getField(fieldDescriptor))
            .collect {
              case message: Message => message
              case v: java.util.List[Message] if v.size > 0 => v.getFirst
            }
        }

        val oneofs = fieldDescriptor.getMessageType.getOneofs.asScala.toList.map { oneofDescriptor =>
          val fields = oneofDescriptor.getFields.asScala.map(Form.build(_, nestedMessageOpt)).toList
          FormOneof.build(oneofDescriptor, fields, nestedMessageOpt)
        }

        val fields = fieldDescriptor.getMessageType.getFields.asScala.toList
          .filter(fieldDescriptor => Option(fieldDescriptor.getContainingOneof).isEmpty)
          .map(Form.build(_, nestedMessageOpt))

        FormMessage(fieldDescriptor, fields ++ oneofs)
      case JavaType.STRING =>
        val property = new SimpleStringProperty()
        messageOpt.flatMap(getValue[String](_, fieldDescriptor)).foreach(property.setValue)

        StringValue(fieldDescriptor, property)
      case JavaType.BOOLEAN =>
        val property = new SimpleBooleanProperty()
        messageOpt.flatMap(getValue[java.lang.Boolean](_, fieldDescriptor)).foreach(property.setValue)

        BooleanValue(fieldDescriptor, property)
      case JavaType.ENUM =>
        val property = new SimpleObjectProperty[Descriptors.EnumValueDescriptor]()
        val value = messageOpt
          .flatMap(getValue[Descriptors.EnumValueDescriptor](_, fieldDescriptor))
          .getOrElse(fieldDescriptor.getEnumType.getValues.getFirst)
        property.setValue(value)

        EnumValue(fieldDescriptor, property)
      case JavaType.INT =>
        val property = new SimpleIntegerProperty()
        messageOpt.flatMap(getValue[java.lang.Integer](_, fieldDescriptor)).foreach(property.setValue)

        IntValue(fieldDescriptor, property)
      case JavaType.LONG =>
        val property = new SimpleLongProperty()
        messageOpt.flatMap(getValue[java.lang.Long](_, fieldDescriptor)).foreach(property.setValue)

        LongValue(fieldDescriptor, property)
      case JavaType.FLOAT =>
        val property = new SimpleFloatProperty()

        messageOpt.flatMap(getValue[java.lang.Float](_, fieldDescriptor)).foreach(property.setValue)

        FloatValue(fieldDescriptor, property)
      case JavaType.DOUBLE =>
        val property = new SimpleDoubleProperty()
        messageOpt.flatMap(getValue[java.lang.Double](_, fieldDescriptor)).foreach(property.setValue)

        DoubleValue(fieldDescriptor, property)
      case _ =>
        UnknownValue(fieldDescriptor)
    }

  private def getValue[T](message: Message, fieldDescriptor: FieldDescriptor): Option[T] =
    message.getField(fieldDescriptor) match {
      case values: java.util.List[T] if values.size > 0 => Option(values.getFirst)
      case values: java.util.List[T] if values.isEmpty => None
      case value: T if value.isInstanceOf[T] => Option(value)
      case _ => None
    }
}
