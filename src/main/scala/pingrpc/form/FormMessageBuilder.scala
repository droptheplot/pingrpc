package pingrpc.form

import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.{DynamicMessage, Message}

trait FormMessageBuilder {
  def descriptor: Descriptor

  def children: Seq[Form]

  def toMessage: Message = {
    val messageBuilder = DynamicMessage.getDefaultInstance(descriptor).toBuilder

    children.foreach {
      case formField: FormField =>
        formField.toValue.foreach(value => messageBuilder.setField(formField.fieldDescriptor, wrapRepeated(formField.fieldDescriptor, value)))
      case formMessage: FormMessage =>
        messageBuilder.setField(formMessage.fieldDescriptor, wrapRepeated(formMessage.fieldDescriptor, formMessage.toMessage))
      case _ =>
    }

    messageBuilder.build
  }
}
