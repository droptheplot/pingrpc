package pingrpc.server

import com.typesafe.scalalogging.StrictLogging
import io.grpc.ForwardingServerCall.SimpleForwardingServerCall
import io.grpc.{Metadata, ServerCall, ServerCallHandler, ServerInterceptor}

class HeaderServerInterceptor extends ServerInterceptor with StrictLogging {
  private val customHeaderKey: Metadata.Key[String] = Metadata.Key.of("custom_header_key", Metadata.ASCII_STRING_MARSHALLER)

  override def interceptCall[ReqT, RespT](call: ServerCall[ReqT, RespT], requestHeaders: Metadata, next: ServerCallHandler[ReqT, RespT]): ServerCall.Listener[ReqT] = {
    logger.info(requestHeaders.toString)

    val simpleForwardingServerCall = new SimpleForwardingServerCall[ReqT, RespT](call) {
      override def sendHeaders(responseHeaders: Metadata): Unit = {
        responseHeaders.put(customHeaderKey, "custom_header_value")

        super.sendHeaders(responseHeaders)
      }
    }

    next.startCall(simpleForwardingServerCall, requestHeaders)
  }
}
