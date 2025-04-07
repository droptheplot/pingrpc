package pingrpc.server

import io.grpc.Server
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import protobuf.hello.HelloServiceGrpc

import scala.concurrent.ExecutionContext

object HelloServer {
  def start()(implicit ec: ExecutionContext): Server =
    NettyServerBuilder
      .forPort(8080)
      .addService(HelloServiceGrpc.bindService(new HelloServiceImpl, ec))
      .addService(ProtoReflectionService.newInstance)
      .build
      .start
}
