package pingrpc

import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType
import io.circe.Json

package object form {
  def wrapJson(fieldDescriptor: FieldDescriptor, json: Json): Json =
    if (fieldDescriptor.isRepeated) Json.arr(json)
    else json

  def parseNumberToJson(javaType: JavaType, text: String): Option[Json] = javaType match {
    case JavaType.INT => text.toIntOption.map(Json.fromInt)
    case JavaType.LONG => text.toLongOption.map(Json.fromLong)
    case JavaType.FLOAT => text.toFloatOption.flatMap(Json.fromFloat)
    case JavaType.DOUBLE => text.toDoubleOption.flatMap(Json.fromDouble)
    case _ => None
  }
}
