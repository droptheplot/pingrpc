package pingrpc.proto

import javafx.util.StringConverter
import protobuf.MethodOuterClass.Method

class MethodConverter extends StringConverter[Method] {
  override def toString(t: Method): String = Option(t).map(_.getName).getOrElse("Unknown")
  override def fromString(s: String): Method = Method.newBuilder.setName(s).build
}
