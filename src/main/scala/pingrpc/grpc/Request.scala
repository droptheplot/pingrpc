package pingrpc.grpc

import com.google.protobuf.Message

case class Request(target: String, method: String, message: Message, headers: Map[String, String])
