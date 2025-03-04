package pingrpc.grpc

import org.scalatest.flatspec._
import org.scalatest.matchers._

class FullMethodNameSpec extends AnyFlatSpec with should.Matchers {

  "parse" should "return package and message name" in {
    FullMessageName.parse("grpc.health.v1.Health.HealthCheckRequest") shouldBe Some(FullMessageName("grpc.health.v1.Health", "HealthCheckRequest"))
    FullMessageName.parse(".grpc.health.v1.Health.HealthCheckRequest") shouldBe Some(FullMessageName("grpc.health.v1.Health", "HealthCheckRequest"))
  }
}
