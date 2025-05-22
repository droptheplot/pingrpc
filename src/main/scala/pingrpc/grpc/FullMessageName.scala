package pingrpc.grpc

case class FullMessageName(packageName: String, messageName: String) {
  override def toString: String = s"$packageName.$messageName"
}

object FullMessageName {
  def parse(value: String): Option[FullMessageName] = {
    val parts = value.split("/").lastOption.map(_.split('.').filter(_.nonEmpty)).toList.flatten

    parts.lastOption.map { messageName =>
      FullMessageName(parts.dropRight(1).mkString("."), messageName)
    }
  }
}
