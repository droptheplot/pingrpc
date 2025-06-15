package pingrpc.form

import com.google.protobuf.Descriptors.{Descriptor, FieldDescriptor}
import com.google.protobuf.{DynamicMessage, Message}

import scala.jdk.CollectionConverters._

trait FormMessageBuilder {
  def descriptor: Descriptor

  def children: Seq[Form]

  def toMessage: Message = {
    val messageBuilder = DynamicMessage.getDefaultInstance(descriptor).toBuilder

    children.foreach {
      case formField: FormField[_] =>
        formField.toValue.foreach { value =>
          val v = wrapRepeated(formField.fieldDescriptor, value)

          messageBuilder.setField(formField.fieldDescriptor, v)
        }
      case formMessage: FormMessage =>
        val message = formMessage.toMessage

        if (message != message.getDefaultInstanceForType)
          messageBuilder.setField(formMessage.fieldDescriptor, wrapRepeated(formMessage.fieldDescriptor, message))
      case _ =>
    }

    messageBuilder.build
  }

  private def wrapRepeated(fieldDescriptor: FieldDescriptor, value: Any): Any =
    if (fieldDescriptor.isRepeated) List(value).asJava else value
}
