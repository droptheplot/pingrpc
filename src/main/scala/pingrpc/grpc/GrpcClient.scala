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
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.chaining.scalaUtilChainingOps

class GrpcClient extends StrictLogging {
  private val marshaller = new ByteMarshaller

  def send(request: Request): IO[Response[Array[Byte]]] = {
    val methodDescriptor = MethodDescriptor
      .newBuilder(marshaller, marshaller)
      .setFullMethodName(request.method)
      .setType(MethodDescriptor.MethodType.UNARY)
      .build()

    val channelBuilder: NettyChannelBuilder =
      NettyChannelBuilder
        .forTarget(request.target)
        .nameResolverFactory(new DnsNameResolverProvider)
        .usePlaintext

    val responsePromise = Promise.apply[Array[Byte]]()
    val headersPromise = Promise.apply[Metadata]()

    val listener = new PromiseListener[Array[Byte]](responsePromise, headersPromise)

    (for {
      executor <- Resource.make(IO(Executors.newSingleThreadExecutor))(executor => IO(executor.shutdown()))
      channel <- Resource.make(IO(channelBuilder.build))(channel => IO(channel.shutdown()))
      options = CallOptions.DEFAULT.withDeadlineAfter(3L, TimeUnit.SECONDS).withExecutor(executor)
      call <- Resource.make(IO(channel.newCall(methodDescriptor, options)))(call => IO(call.cancel("Call is cancelled", null)))
    } yield call).use { call =>
      for {
        _ <- processCall(call, listener, request.message.toByteArray, toMetadata(request.headers))
        response <- IO.fromFuture(IO(responsePromise.future))
        metadata <- IO.fromFuture(IO(headersPromise.future))
      } yield Response(response, fromMetadata(metadata))
    }
  }

  private def toMetadata(headers: Map[String, String]) =
     new Metadata().tap { metadata =>
       headers.foreach { case (key, value) =>
         metadata.put(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER), value)
       }
     }

  private def fromMetadata(metadata: Metadata): Map[String, String] =
    metadata.keys.asScala.map { key =>
      (key, metadata.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER)))
    }.toMap

  def sendAndParse[T <: Message](request: Request)(implicit parser: Parser[T]): IO[Response[T]] =
    for {
      response <- send(request)
      message <- IO(parser.parseFrom(response.message))
    } yield response.copy(message = message)

  private def processCall[T](call: ClientCall[T, T], listener: Listener[T], request: T, metadata: Metadata): IO[Unit] =
    IO.blocking {
      call.start(listener, metadata)
      call.request(1)
      call.sendMessage(request)
      call.halfClose()
    }
}
