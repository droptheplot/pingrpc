package pingrpc

import javafx.application.Application
import pingrpc.server.HelloServer

import scala.concurrent.ExecutionContext

object Main extends App {
  args.find(_ == "--server").foreach { _ =>
    HelloServer.start()(ExecutionContext.global)
  }

  Application.launch(classOf[AppFx], args: _*)
}
