package pingrpc.proto

import com.google.common.io.ByteStreams
import io.grpc.MethodDescriptor

import java.io.{ByteArrayInputStream, InputStream}

class ByteMarshaller extends MethodDescriptor.Marshaller[Array[Byte]] {
  def parse(stream: InputStream): Array[Byte] = ByteStreams.toByteArray(stream)

  def stream(value: Array[Byte]) = new ByteArrayInputStream(value)
}
