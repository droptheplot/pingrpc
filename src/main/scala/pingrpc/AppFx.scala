package pingrpc

import atlantafx.base.theme.PrimerLight
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.{Screen, Stage}
import pingrpc.grpc.{GrpcClient, ReflectionManager, Sender}
import pingrpc.storage.StateManager
import pingrpc.ui.controllers.AppController
import pingrpc.ui.views.AppView

import scala.util.Try

class AppFx extends Application {
  override def start(primaryStage: Stage): Unit = {
    val grpcClient = new GrpcClient
    val stateManager = new StateManager
    val sender = new Sender(grpcClient, stateManager)
    val reflectionManager = new ReflectionManager(grpcClient)
    val appController = new AppController(reflectionManager, sender, stateManager)
    val appView = new AppView(appController)

    val screenBounds = Screen.getPrimary.getBounds

    primaryStage.setTitle("PingRPC")
    primaryStage.setScene(new Scene(appView, screenBounds.getWidth * 0.83, screenBounds.getHeight * 0.74))

    Try(getClass.getResourceAsStream("/icon.png"))
      .map(new Image(_))
      .foreach(primaryStage.getIcons.add(_))

    Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet)

    primaryStage.show()
  }
}
