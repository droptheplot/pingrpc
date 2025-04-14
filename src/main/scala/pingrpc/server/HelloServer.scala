package pingrpc.server

import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import io.grpc.{Server, ServerInterceptors}
import protobuf.hello.HelloServiceGrpc

import scala.concurrent.ExecutionContext

object HelloServer {
  def start()(implicit ec: ExecutionContext): Server = {
    NettyServerBuilder
      .forPort(8080)
      .addService(ServerInterceptors.intercept(HelloServiceGrpc.bindService(new HelloServiceImpl, ec), new HeaderServerInterceptor()))
      .addService(ProtoReflectionService.newInstance)
      .build
      .start
  }
}
