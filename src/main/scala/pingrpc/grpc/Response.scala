package pingrpc.grpc

case class Response[T](message: T, headers: Map[String, String])
