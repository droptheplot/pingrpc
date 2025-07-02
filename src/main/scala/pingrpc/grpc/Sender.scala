package pingrpc.grpc

import cats.effect.IO
import com.google.protobuf.util.JsonFormat
import com.google.protobuf.{Any, Descriptors, DynamicMessage, Message}
import com.typesafe.scalalogging.StrictLogging
import pingrpc.storage.StateManager

import scala.jdk.CollectionConverters._

class Sender(grpcClient: GrpcClient, stateManager: StateManager) extends StrictLogging {
  def send(
      requestDescriptor: Descriptors.Descriptor,
      responseDescriptor: Descriptors.Descriptor,
      method: String,
      target: String,
      message: Message,
      requestHeaders: Map[String, String]
  ): IO[Response[String]] = for {
    _ <- IO(logger.info(s"Request: $message"))
    _ <- stateManager.update(_.setRequestDescriptor(requestDescriptor.toProto).setRequest(Any.pack(message)))
    request = Request(target, method, message, requestHeaders)
    parser = DynamicMessage.getDefaultInstance(responseDescriptor).getParserForType
    response <- grpcClient.send(request)(parser)
    _ <- stateManager.update(
      _.setResponseDescriptor(responseDescriptor.toProto)
        .setResponse(Any.pack(response.message))
        .putAllRequestHeaders(requestHeaders.asJava)
        .putAllResponseHeaders(response.headers.asJava))
    _ = IO(logger.info(s"Response: ${response.message}"))
    responseJson <- IO(JsonFormat.printer.preservingProtoFieldNames.print(response.message))
  } yield response.copy(message = responseJson)
}
