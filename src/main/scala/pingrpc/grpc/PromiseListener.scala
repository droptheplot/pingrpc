package pingrpc.grpc

import io.grpc.{ClientCall, Metadata, Status}

import scala.concurrent.Promise

class PromiseListener[T](responsePromise: Promise[T], headersPromise: Promise[Metadata]) extends ClientCall.Listener[T] {
  override def onHeaders(headers: Metadata): Unit =
    headersPromise.success(headers)

  override def onMessage(message: T): Unit =
    responsePromise.success(message)

  override def onClose(status: Status, trailers: Metadata): Unit =
    if (!status.isOk) responsePromise.failure(status.asException)
}
