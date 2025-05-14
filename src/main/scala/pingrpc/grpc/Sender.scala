package pingrpc.grpc

import cats.effect.IO
import com.google.protobuf.util.JsonFormat
import com.google.protobuf.{Any, Descriptors, DynamicMessage}
import com.typesafe.scalalogging.StrictLogging
import pingrpc.proto.ProtoUtils
import pingrpc.storage.StateManager

class Sender(grpcClient: GrpcClient, stateManager: StateManager) extends StrictLogging {
  def send(
      requestDescriptor: Descriptors.Descriptor,
      responseDescriptor: Descriptors.Descriptor,
      method: String,
      target: String,
      requestJson: String
  ): IO[Response[String]] = for {
    _ <- IO(logger.info(s"Request: $requestJson"))
    message <- ProtoUtils.messageFromJson(requestJson, requestDescriptor)
    _ <- stateManager.update(_.setRequestDescriptor(requestDescriptor.toProto).setRequest(Any.pack(message)))
    request = Request(target, method, message, Map.empty)
    parser = DynamicMessage.getDefaultInstance(responseDescriptor).getParserForType
    response <- grpcClient.send(request)(parser)
    _ <- stateManager.update(_.setResponseDescriptor(responseDescriptor.toProto).setRequest(Any.pack(response.message)))
    responseJson <- IO(JsonFormat.printer.preservingProtoFieldNames.print(response.message))
    _ = IO(logger.info(s"Response: $responseJson"))
  } yield response.copy(message = responseJson)
}
