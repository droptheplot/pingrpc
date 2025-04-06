package pingrpc.proto

import com.google.protobuf.Descriptors.EnumValueDescriptor
import javafx.util.StringConverter

class EnumValueDescriptorConverter extends StringConverter[EnumValueDescriptor] {
  override def toString(t: EnumValueDescriptor): String = Option(t).map(_.getName).getOrElse("")
  override def fromString(s: String): EnumValueDescriptor = ???
}
