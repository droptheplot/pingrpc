package pingrpc.proto

import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import org.scalatest.flatspec._
import org.scalatest.matchers._

import scala.jdk.CollectionConverters._

class ProtoUtilsSpec extends AnyFlatSpec with should.Matchers {

  "toFileDescriptors" should "resolve dependencies for file descriptors" in {
    val fileDescriptorProtos = List(
      FileDescriptorProto.newBuilder.setName("a").addAllDependency(List("b").asJava).build,
      FileDescriptorProto.newBuilder.setName("b").addAllDependency(List("c").asJava).build,
      FileDescriptorProto.newBuilder.setName("c").build
    )

    ProtoUtils.toFileDescriptors(fileDescriptorProtos) match {
      case c :: b :: a :: Nil =>
        a.getName shouldBe "a"
        a.getDependencies.asScala.toList.map(_.getName) shouldBe List("b")

        b.getName shouldBe "b"
        b.getDependencies.asScala.toList.map(_.getName) shouldBe List("c")

        c.getName shouldBe "c"
        c.getDependencies.asScala.length shouldBe 0
      case fileDescriptors => fail(s"Got ${fileDescriptors.length} fileDescriptors")
    }
  }
}
