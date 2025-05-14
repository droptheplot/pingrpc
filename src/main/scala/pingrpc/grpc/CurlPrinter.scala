package pingrpc.grpc

import pingrpc.proto.ProtoUtils
import protobuf.MethodOuterClass.Method
import protobuf.ServiceOuterClass.Service

object CurlPrinter {
  def print(
             service: Service,
             method: Method,
             target: String,
             json: String
  ): String = {
    val methodName = ProtoUtils.buildMethodName(service, method)
    val dataOpt = Option.when(json.nonEmpty)(s"-d '$json'")

    List(Some("grpcurl"), Some("-plaintext"), dataOpt, Some(target), Some(methodName)).flatten.mkString(" ")
  }
}
