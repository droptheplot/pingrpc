package pingrpc.form

import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.{DynamicMessage, Message}

trait MessageBuilder {
  def descriptor: Descriptor

  def children: Seq[Form]

  def toMessage: Message = {
    val messageBuilder = DynamicMessage.getDefaultInstance(descriptor).toBuilder

    children
      .map {
        case formOneOf: FormOneof => formOneOf.toForm
        case form => form
      }
      .foreach {
        case formField: FormValue[_] =>
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
      }

    messageBuilder.build
  }
}
