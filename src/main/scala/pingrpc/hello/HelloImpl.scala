package pingrpc.hello

import protobuf.hello._

import scala.concurrent.Future

class HelloImpl extends HelloServiceGrpc.HelloService {
  override def create(request: CreateUserRequest): Future[CreateUserResponse] =
    Future.successful(CreateUserResponse.defaultInstance.withUser(request.getUser))
}
