package pingrpc

import protobuf.ServiceOuterClass.Service

package object proto {
  implicit val serviceOrdering: Ordering[Service] =
    (a: Service, b: Service) =>
      (a, b) match {
        case (a, _) if a.getName.startsWith("grpc") => 1
        case (_, b) if b.getName.startsWith("grpc") => -1
        case (a, b) => a.getName.compareTo(b.getName)
      }
}
