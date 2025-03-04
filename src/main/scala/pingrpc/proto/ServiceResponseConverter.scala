package pingrpc.proto

import io.grpc.reflection.v1.ServiceResponse
import javafx.util.StringConverter

class ServiceResponseConverter extends StringConverter[ServiceResponse] {
  override def toString(t: ServiceResponse): String = Option(t).map(_.getName).getOrElse("")
  override def fromString(s: String): ServiceResponse = ServiceResponse.getDefaultInstance
}
