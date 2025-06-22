package pingrpc.form

import com.google.protobuf.Descriptors.{Descriptor, FieldDescriptor}
import com.google.protobuf.{DynamicMessage, Message}
import javafx.beans.property.SimpleBooleanProperty

import scala.jdk.CollectionConverters._

trait FormMessageBuilder {
  def descriptor: Descriptor

  def children: Seq[Form]

  def isDisabled: SimpleBooleanProperty

  def toMessage: Message = {
    val messageBuilder = DynamicMessage.getDefaultInstance(descriptor).toBuilder

    children.foreach {
      case formField: FormField[_] =>
        formField.toValue.foreach { value =>
          if (formField.fieldDescriptor.isRepeated) messageBuilder.addRepeatedField(formField.fieldDescriptor, value)
          else messageBuilder.setField(formField.fieldDescriptor, value)
        }
      case formMessage: FormMessage =>
        val message = formMessage.toMessage

        if (message != message.getDefaultInstanceForType && !formMessage.isDisabled.getValue)
          messageBuilder.setField(formMessage.fieldDescriptor, wrapRepeated(formMessage.fieldDescriptor, message))
      case _ =>
    }

    messageBuilder.build
  }

  private def wrapRepeated(fieldDescriptor: FieldDescriptor, value: Any): Any =
    if (fieldDescriptor.isRepeated) List(value).asJava else value
}
