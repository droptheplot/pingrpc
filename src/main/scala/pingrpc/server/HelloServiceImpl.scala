package pingrpc.server

import com.typesafe.scalalogging.StrictLogging
import protobuf.hello._

import scala.concurrent.{ExecutionContext, Future}

class HelloServiceImpl()(implicit ec: ExecutionContext) extends HelloServiceGrpc.HelloService with StrictLogging {
  override def createUser(request: CreateUserRequest): Future[CreateUserResponse] = {
    logger.info(request.toProtoString)
    Future.successful(CreateUserResponse.defaultInstance.withUser(request.getUser))
  }
}
