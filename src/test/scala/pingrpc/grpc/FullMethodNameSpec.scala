package pingrpc.grpc

import org.scalatest.flatspec._
import org.scalatest.matchers._

class FullMethodNameSpec extends AnyFlatSpec with should.Matchers {

  "parse" should "return package and message name" in {
    val fullMessageName = FullMessageName("grpc.health.v1.Health", "HealthCheckRequest")

    FullMessageName.parse("") shouldBe None
    FullMessageName.parse("HealthCheckRequest") shouldBe None
    FullMessageName.parse("grpc.health.v1.Health.HealthCheckRequest") shouldBe Some(fullMessageName)
    FullMessageName.parse(".grpc.health.v1.Health.HealthCheckRequest") shouldBe Some(fullMessageName)
    FullMessageName.parse("type.googleapis.com/grpc.health.v1.Health.HealthCheckRequest") shouldBe Some(fullMessageName)
  }
}
