package pingrpc.hello

import io.grpc.Server
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import protobuf.hello.HelloServiceGrpc

import scala.concurrent.ExecutionContext

object HelloServer {
  def start: Server =
    NettyServerBuilder
      .forPort(8080)
      .addService(HelloServiceGrpc.bindService(new HelloImpl, ExecutionContext.global))
      .addService(ProtoReflectionService.newInstance)
      .build
      .start
}

