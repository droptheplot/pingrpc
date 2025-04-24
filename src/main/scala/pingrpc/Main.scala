package pingrpc

import atlantafx.base.theme.PrimerLight
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.{Screen, Stage}
import pingrpc.grpc.{GrpcClient, ReflectionManager, Sender}
import pingrpc.server.HelloServer
import pingrpc.ui.controllers.ActionController
import pingrpc.ui.views.LayoutView

import scala.concurrent.ExecutionContext
import scala.util.Try

class AppFx extends Application {
  override def start(primaryStage: Stage): Unit = {
    val grpcClient = new GrpcClient
    val sender = new Sender(grpcClient)
    val reflectionManager = new ReflectionManager(grpcClient)
    val actionController = new ActionController(reflectionManager, sender)
    val layout = new LayoutView(actionController)

    val screenBounds = Screen.getPrimary.getBounds

    primaryStage.setTitle("PingRPC")
    primaryStage.setScene(new Scene(layout, screenBounds.getWidth * 0.83, screenBounds.getHeight * 0.74))

    Try(getClass.getResourceAsStream("/icon.png"))
      .map(new Image(_))
      .foreach(primaryStage.getIcons.add(_))

    Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet)

    primaryStage.show()
  }
}

object Main extends App {
  args.find(_ == "--server").foreach { _ =>
    HelloServer.start()(ExecutionContext.global)
  }

  Application.launch(classOf[AppFx], args: _*)
}
