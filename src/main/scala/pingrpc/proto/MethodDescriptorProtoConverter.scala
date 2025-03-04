package pingrpc.proto

import com.google.protobuf.DescriptorProtos.MethodDescriptorProto
import javafx.util.StringConverter

class MethodDescriptorProtoConverter extends StringConverter[MethodDescriptorProto] {
  override def toString(t: MethodDescriptorProto): String = Option(t).map(_.getName).getOrElse("")
  override def fromString(s: String): MethodDescriptorProto = MethodDescriptorProto.getDefaultInstance
}
