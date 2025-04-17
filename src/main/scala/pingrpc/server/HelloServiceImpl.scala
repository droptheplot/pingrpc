package pingrpc.server

import com.typesafe.scalalogging.StrictLogging
import protobuf.hello._

import scala.concurrent.{ExecutionContext, Future}

class HelloServiceImpl()(implicit ec: ExecutionContext) extends HelloServiceGrpc.HelloService with StrictLogging {
  override def createUser(request: CreateUserRequest): Future[CreateUserResponse] =
    Future.successful(CreateUserResponse.defaultInstance.withUser(request.getUser))

  override def updateUser(request: UpdateUserRequest): Future[UpdateUserResponse] =
    Future.failed(new Throwable("Something went wrong"))
}
