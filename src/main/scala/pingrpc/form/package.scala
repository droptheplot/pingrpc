package pingrpc

import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType

import scala.jdk.CollectionConverters._

package object form {
  def parseNumber(javaType: JavaType, text: String): Option[Any] = javaType match {
    case JavaType.INT => text.toIntOption
    case JavaType.LONG => text.toLongOption
    case JavaType.FLOAT => text.toFloatOption
    case JavaType.DOUBLE => text.toDoubleOption
    case _ => None
  }

  def wrapRepeated(fieldDescriptor: FieldDescriptor, value: Any): Any =
    if (fieldDescriptor.isRepeated) List(value).asJava else value
}
