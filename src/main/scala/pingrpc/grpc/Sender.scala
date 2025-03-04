package pingrpc.grpc

import cats.effect.IO
import com.google.protobuf.DescriptorProtos.{FileDescriptorProto, MethodDescriptorProto}
import com.typesafe.scalalogging.StrictLogging
import pingrpc.proto.ProtoUtils
import io.grpc.reflection.v1.ServiceResponse

class Sender(grpcClient: GrpcClient) extends StrictLogging {
  def send(
      fileDescriptorProtos: List[FileDescriptorProto],
      serviceResponse: ServiceResponse,
      methodDescriptorProto: MethodDescriptorProto,
      target: String,
      json: String
  ): IO[String] =
    for {
      requestMessageName <- IO.fromOption(FullMessageName.parse(methodDescriptorProto.getInputType)) {
        new Throwable(s"Cannot get request name from `${methodDescriptorProto.getInputType}`")
      }
      responseMessageName <- IO.fromOption(FullMessageName.parse(methodDescriptorProto.getOutputType)) {
        new Throwable(s"Cannot get response name from `${methodDescriptorProto.getOutputType}`")
      }
      fileDescriptors = ProtoUtils.toFileDescriptors(fileDescriptorProtos)
      requestDescriptor <- IO.fromOption(ProtoUtils.findMessageDescriptor(fileDescriptors, requestMessageName)) {
        new Throwable(s"Cannot find request descriptor for `$requestMessageName`")
      }
      responseDescriptor <- IO.fromOption(ProtoUtils.findMessageDescriptor(fileDescriptors, responseMessageName)) {
        new Throwable(s"Cannot find response descriptor for `$responseMessageName`")
      }
      requestMessage <- ProtoUtils.messageFromJson(json, requestDescriptor)
      method = ProtoUtils.buildMethodName(serviceResponse, methodDescriptorProto)
      responseBytes <- grpcClient.send(target, method, requestMessage)
      responseText <- ProtoUtils.messageToJson(responseBytes, responseDescriptor)
    } yield responseText
}
