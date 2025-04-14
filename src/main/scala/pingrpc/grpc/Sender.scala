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
    requestMessage <- ProtoUtils
      .messageFromJson(json, requestDescriptor)
      .adaptError(_ => new Throwable("Invalid request json"))
    responseBytes <- grpcClient.send(target, method, requestMessage, Map.empty)
    responseText <- ProtoUtils
      .messageToJson(responseBytes, responseDescriptor)
      .adaptError(_ => new Throwable("Invalid response json"))
  } yield responseText
}
