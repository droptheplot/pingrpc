package pingrpc.ui

import com.google.protobuf.DescriptorProtos.FileDescriptorProto

import scala.collection.mutable

object State {
  val fileDescriptorProtos: mutable.ListBuffer[FileDescriptorProto] = mutable.ListBuffer.empty[FileDescriptorProto]
}
