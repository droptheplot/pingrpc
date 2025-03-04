package pingrpc.grpc

import cats.effect.IO
import com.google.protobuf.{Message, Parser}
import com.typesafe.scalalogging.StrictLogging
import pingrpc.proto.ByteMarshaller
import io.grpc.internal.DnsNameResolverProvider
import io.grpc.netty.shaded.io.grpc.netty._
import io.grpc.stub.ClientCalls
import io.grpc.{CallOptions, ManagedChannel, MethodDescriptor}

import java.time.Duration

class GrpcClient extends StrictLogging {
  private val marshaller = new ByteMarshaller

  def send(target: String, method: String, request: Message): IO[Array[Byte]] = {
    val methodDescriptor = MethodDescriptor
      .newBuilder(marshaller, marshaller)
      .setFullMethodName(method)
      .setType(MethodDescriptor.MethodType.UNARY)
      .build()

    val callOptions = CallOptions.DEFAULT.withDeadlineAfter(Duration.ofSeconds(2))

    IO.blocking {
      val managedChannel: ManagedChannel = NettyChannelBuilder
        .forTarget(target)
        .nameResolverFactory(new DnsNameResolverProvider)
        .usePlaintext
        .build()

      ClientCalls.blockingUnaryCall(managedChannel, methodDescriptor, callOptions, request.toByteArray)
    }
  }

  def sendAndParse[T <: Message](target: String, method: String, request: Message)(implicit parser: Parser[T]): IO[T] =
    for {
      responseBytes <- send(target, method, request)
      response <- IO(parser.parseFrom(responseBytes))
    } yield response
}
