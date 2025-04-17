package pingrpc.grpc

import cats.effect.IO
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.google.protobuf.Parser
import com.typesafe.scalalogging.StrictLogging
import io.grpc.reflection.v1.{ServerReflectionRequest, ServerReflectionResponse, ServiceResponse}

import scala.jdk.CollectionConverters._

class ReflectionManager(grpcClient: GrpcClient) extends StrictLogging {
  private val v1MethodName = "grpc.reflection.v1.ServerReflection/ServerReflectionInfo"
  private val alphaMethodName = "grpc.reflection.v1alpha.ServerReflection/ServerReflectionInfo"

  implicit val reflectionParser: Parser[ServerReflectionResponse] = ServerReflectionResponse.parser

  def getServices(target: String): IO[List[ServiceResponse]] = {
    val message = ServerReflectionRequest
      .newBuilder()
      .setListServices(new String)
      .build()

    val request = Request(target, v1MethodName, message, Map.empty)

    grpcClient
      .send[ServerReflectionResponse](request)
      .recoverWith(_ => grpcClient.send[ServerReflectionResponse](request.copy(method = alphaMethodName)))
      .map(_.message.getListServicesResponse.getServiceList.asScala.toList)
  }

  def getFileDescriptors(target: String, symbol: String): IO[List[FileDescriptorProto]] = {
    val message = ServerReflectionRequest
      .newBuilder()
      .setFileContainingSymbol(symbol)
      .build()

    val request = Request(target, v1MethodName, message, Map.empty)

    grpcClient
      .send[ServerReflectionResponse](request)
      .recoverWith(_ => grpcClient.send[ServerReflectionResponse](request.copy(method = alphaMethodName)))
      .map(_.message.getFileDescriptorResponse.getFileDescriptorProtoList.asScala.toList.map(FileDescriptorProto.parseFrom))
  }
}
