package pingrpc.grpc

import cats.effect.IO
import com.google.protobuf.util.JsonFormat
import com.google.protobuf.{Descriptors, DynamicMessage}
import com.typesafe.scalalogging.StrictLogging
import pingrpc.proto.ProtoUtils

class Sender(grpcClient: GrpcClient) extends StrictLogging {
  def send(
      requestDescriptor: Descriptors.Descriptor,
      responseDescriptor: Descriptors.Descriptor,
      method: String,
      target: String,
      json: String
  ): IO[Response[String]] = for {
    message <- ProtoUtils.messageFromJson(json, requestDescriptor)
    request = Request(target, method, message, Map.empty)
    parser = DynamicMessage.getDefaultInstance(responseDescriptor).getParserForType
    response <- grpcClient.send(request)(parser)
    json <- IO(JsonFormat.printer.preservingProtoFieldNames.print(response.message))
  } yield response.copy(message = json)
}
