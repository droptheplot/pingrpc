package pingrpc.hello

import protobuf.hello.{HelloGrpc, Simple}

import scala.concurrent.Future

class HelloImpl extends HelloGrpc.Hello {
  override def callSimple(request: Simple): Future[Simple] =
    Future.successful {
      Simple
        .defaultInstance
        .withInt64(123)
        .withDouble(1.0)
        .withString("string")
        .withEnum(Simple.Enum.FIRST)
        .withNested(Simple.Nested.defaultInstance.withString("nested"))
    }
}
