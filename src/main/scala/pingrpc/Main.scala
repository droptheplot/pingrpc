package pingrpc

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import pingrpc.grpc.{GrpcClient, ReflectionManager, Sender}
import pingrpc.hello.HelloServer
import pingrpc.ui.Layout

import scala.util.Try

class AppFx extends Application {
  override def start(primaryStage: Stage): Unit = {
    val grpcClient = new GrpcClient
    val sender = new Sender(grpcClient)
    val reflectionManager = new ReflectionManager(grpcClient)
    val layout = new Layout(reflectionManager, sender)

    primaryStage.setTitle("PingRPC")
    primaryStage.setScene(new Scene(layout.build, 1200, 800))

    Try(getClass.getResourceAsStream("/icon.png"))
      .map(new Image(_))
      .foreach(primaryStage.getIcons.add(_))

    primaryStage.show()
  }
}

object Main extends App {
  HelloServer.start

  Application.launch(classOf[AppFx], args: _*)
}
