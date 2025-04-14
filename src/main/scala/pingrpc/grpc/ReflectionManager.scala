package pingrpc.grpc

import cats.effect.IO
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.google.protobuf.Parser
import io.grpc.reflection.v1.{ServerReflectionRequest, ServerReflectionResponse, ServiceResponse}

import scala.jdk.CollectionConverters._

class ReflectionManager(grpcClient: GrpcClient) {
  private val reflectionServerName = "grpc.reflection.v1alpha.ServerReflection/ServerReflectionInfo"

  implicit val reflectionParser: Parser[ServerReflectionResponse] = ServerReflectionResponse.parser

  def getServices(target: String): IO[List[ServiceResponse]] = {
    val request = ServerReflectionRequest
      .newBuilder()
      .setListServices(new String)
      .build()

    grpcClient
      .sendAndParse[ServerReflectionResponse](target, reflectionServerName, request, Map.empty)
      .map(_.getListServicesResponse.getServiceList.asScala.toList)
  }

  def getFileDescriptors(target: String, symbol: String): IO[List[FileDescriptorProto]] = {
    val request = ServerReflectionRequest
      .newBuilder()
      .setFileContainingSymbol(symbol)
      .build()

    grpcClient
      .sendAndParse[ServerReflectionResponse](target, reflectionServerName, request, Map.empty)
      .map(_.getFileDescriptorResponse.getFileDescriptorProtoList.asScala.toList.map(FileDescriptorProto.parseFrom))
  }
}
