package pingrpc.grpc

import io.grpc.{ClientCall, Metadata, Status}

import scala.concurrent.Promise

class PromiseListener[T](promise: Promise[T]) extends ClientCall.Listener[T] {
  override def onMessage(message: T): Unit =
    promise.success(message)

  override def onClose(status: Status, trailers: Metadata): Unit =
    if (!status.isOk) promise.failure(status.asException)
}
