package pingrpc.grpc

import cats.effect.IO
import cats.implicits.catsSyntaxMonadError
import com.google.protobuf.Descriptors
import com.typesafe.scalalogging.StrictLogging
import pingrpc.proto.ProtoUtils

class Sender(grpcClient: GrpcClient) extends StrictLogging {
  def send(
      requestDescriptor: Descriptors.Descriptor,
      responseDescriptor: Descriptors.Descriptor,
      method: String,
      target: String,
      json: String
  ): IO[String] = for {
    message <- ProtoUtils
      .messageFromJson(json, requestDescriptor)
      .adaptError(_ => new Throwable("Invalid request json"))
    request = Request(target, method, message, Map.empty)
    response <- grpcClient.send(request)
    json <- ProtoUtils
      .messageToJson(response.message, responseDescriptor)
      .adaptError(_ => new Throwable("Invalid response json"))
  } yield json
}
