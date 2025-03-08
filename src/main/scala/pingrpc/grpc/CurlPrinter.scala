package pingrpc.grpc

import com.google.protobuf.DescriptorProtos.MethodDescriptorProto
import io.grpc.reflection.v1.ServiceResponse
import pingrpc.proto.ProtoUtils

object CurlPrinter {
  def print(
      serviceResponse: ServiceResponse,
      methodDescriptorProto: MethodDescriptorProto,
      target: String,
      json: String
  ): String = {
    val method = ProtoUtils.buildMethodName(serviceResponse, methodDescriptorProto)
    val dataOpt = Option.when(json.nonEmpty)(s"-d '$json'")

    List(Some("grpcurl"), Some("-plaintext"), dataOpt, Some(target), Some(method)).flatten.mkString(" ")
  }
}
