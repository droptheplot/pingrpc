package pingrpc.proto

import com.google.protobuf.DescriptorProtos.{FileDescriptorProto, ServiceDescriptorProto}
import com.google.protobuf.Descriptors.{Descriptor, FileDescriptor}
import com.google.protobuf.util.JsonFormat
import com.google.protobuf.{DynamicMessage, Message}
import pingrpc.grpc.FullMessageName
import protobuf.MethodOuterClass.Method
import protobuf.ServiceOuterClass.Service

import scala.annotation.tailrec
import scala.jdk.CollectionConverters._
import scala.util.Try

object ProtoUtils {
  def findServiceDescriptor(
      fileDescriptorProtos: List[FileDescriptorProto],
      fullMessageName: FullMessageName
  ): Option[ServiceDescriptorProto] =
    fileDescriptorProtos.find(_.getPackage == fullMessageName.packageName).flatMap { fileDescriptorProto =>
      fileDescriptorProto.getServiceList.asScala.toList.find(_.getName == fullMessageName.messageName)
    }

  def findMessageDescriptor(
      fileDescriptors: List[FileDescriptor],
      fullMessageName: FullMessageName
  ): Option[Descriptor] =
    fileDescriptors.flatMap { fileDescriptor =>
      Option.apply(fileDescriptor.findMessageTypeByName(fullMessageName.messageName))
    }.headOption

  @tailrec
  def toFileDescriptors(fileDescriptorProtos: List[FileDescriptorProto], resolved: List[FileDescriptor] = List.empty): List[FileDescriptor] =
    fileDescriptorProtos match {
      case head :: rest =>
        if (head.getDependencyList.asScala.toList.forall(resolved.map(_.getName).contains)) {
          val dependencies = head.getDependencyList.asScala.toList.flatMap(name => resolved.find(_.getName == name))
          toFileDescriptors(rest, resolved :+ FileDescriptor.buildFrom(head, dependencies.toArray))
        } else {
          toFileDescriptors(rest :+ head, resolved)
        }
      case Nil => resolved
    }

  def messageFromJson(json: String, descriptor: Descriptor): Either[Throwable, Message] =
    Option.when(json.nonEmpty)(json) match {
      case Some(value) =>
        val builder = DynamicMessage.getDefaultInstance(descriptor).toBuilder
        Try(JsonFormat.parser.ignoringUnknownFields.merge(value, builder)).map(_ => builder.build).toEither
      case None => Right(DynamicMessage.getDefaultInstance(descriptor))
    }

  def messageToJson(message: Message): String =
    JsonFormat.printer.preservingProtoFieldNames.print(message)

  def buildMethodName(service: Service, method: Method) =
    s"${service.getName}/${method.getName}"
}
