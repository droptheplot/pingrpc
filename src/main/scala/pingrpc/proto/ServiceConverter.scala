package pingrpc.proto

import javafx.util.StringConverter
import protobuf.ServiceOuterClass.Service

class ServiceConverter extends StringConverter[Service] {
  override def toString(t: Service): String = Option(t).map(_.getName).getOrElse("Unknown")
  override def fromString(s: String): Service = Service.newBuilder.setName(s).build
}
