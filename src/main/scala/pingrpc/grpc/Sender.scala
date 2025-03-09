package pingrpc.grpc

import cats.effect.IO
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
    requestMessage <- ProtoUtils.messageFromJson(json, requestDescriptor)
    responseBytes <- grpcClient.send(target, method, requestMessage)
    responseText <- ProtoUtils.messageToJson(responseBytes, responseDescriptor)
  } yield responseText
}
