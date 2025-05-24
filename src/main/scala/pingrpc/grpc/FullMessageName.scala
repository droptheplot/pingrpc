package pingrpc.grpc

case class FullMessageName(packageName: String, messageName: String) {
  override def toString: String = s"$packageName.$messageName"
}

object FullMessageName {
  def parse(value: String): Option[FullMessageName] =
    value.split("/").lastOption.map(_.split('.').filter(_.nonEmpty)).toList.flatten match {
      case packageName :+ messageName if packageName.nonEmpty => Some(FullMessageName(packageName.mkString("."), messageName))
      case _ => None
    }
}
