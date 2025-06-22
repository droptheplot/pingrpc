package pingrpc.form

import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.{DynamicMessage, Message}
import javafx.beans.property.SimpleBooleanProperty

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

        if (message != message.getDefaultInstanceForType && !formMessage.isDisabled.getValue) {
          if (formMessage.fieldDescriptor.isRepeated) messageBuilder.addRepeatedField(formMessage.fieldDescriptor, message)
          else messageBuilder.setField(formMessage.fieldDescriptor, message)
        }
      case _ =>
    }

    messageBuilder.build
  }
}
