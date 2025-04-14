package pingrpc.grpc

import cats.effect.{IO, Resource}
import com.google.protobuf.{Message, Parser}
import com.typesafe.scalalogging.StrictLogging
import io.grpc.ClientCall.Listener
import io.grpc.internal.DnsNameResolverProvider
import io.grpc.netty.shaded.io.grpc.netty._
import io.grpc.{CallOptions, ClientCall, Metadata, MethodDescriptor}
import pingrpc.proto.ByteMarshaller

import java.util.concurrent.{Executors, TimeUnit}
import scala.concurrent.Promise

class GrpcClient extends StrictLogging {
  private val marshaller = new ByteMarshaller

  def send(target: String, method: String, request: Message, headers: Map[String, String]): IO[Array[Byte]] = {
    val methodDescriptor = MethodDescriptor
      .newBuilder(marshaller, marshaller)
      .setFullMethodName(method)
      .setType(MethodDescriptor.MethodType.UNARY)
      .build()

    val channelBuilder: NettyChannelBuilder =
      NettyChannelBuilder
        .forTarget(target)
        .nameResolverFactory(new DnsNameResolverProvider)
        .usePlaintext

    val promise = Promise.apply[Array[Byte]]()
    val listener = new PromiseListener[Array[Byte]](promise)

    val metadata = new Metadata()
    headers.foreach { case (key, value) =>
      metadata.put(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER), value)
    }

    (for {
      executor <- Resource.make(IO(Executors.newSingleThreadExecutor))(executor => IO(executor.shutdown()))
      channel <- Resource.make(IO(channelBuilder.build))(channel => IO(channel.shutdown()))
      options = CallOptions.DEFAULT.withDeadlineAfter(3L, TimeUnit.SECONDS).withExecutor(executor)
      call <- Resource.make(IO(channel.newCall(methodDescriptor, options)))(call => IO(call.cancel("Call is cancelled", null)))
    } yield call).use { call =>
       processCall(call, listener, request.toByteArray, metadata) *> IO.fromFuture(IO(promise.future))
    }
  }

  def sendAndParse[T <: Message](target: String, method: String, request: Message, headers: Map[String, String])(implicit parser: Parser[T]): IO[T] =
    for {
      responseBytes <- send(target, method, request, headers)
      response <- IO(parser.parseFrom(responseBytes))
    } yield response

  private def processCall[T](call: ClientCall[T, T], listener: Listener[T], request: T, metadata: Metadata): IO[Unit] =
    IO.blocking {
      call.start(listener, metadata)
      call.request(1)
      call.sendMessage(request)
      call.halfClose()
    }
}
