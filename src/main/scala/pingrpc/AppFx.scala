package pingrpc

import atlantafx.base.theme.PrimerLight
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.{Screen, Stage}
import pingrpc.grpc.{GrpcClient, ReflectionManager, Sender}
import pingrpc.storage.StateManager
import pingrpc.ui.controllers.AppController
import pingrpc.ui.views.AppView

class AppFx extends Application {
  override def start(primaryStage: Stage): Unit = {
    val grpcClient = new GrpcClient
    val stateManager = new StateManager
    val sender = new Sender(grpcClient, stateManager)
    val reflectionManager = new ReflectionManager(grpcClient)
    val appController = new AppController(reflectionManager, sender, stateManager)
    val appView = new AppView(appController)

    val screenBounds = Screen.getPrimary.getBounds

    val scene = new Scene(appView, screenBounds.getWidth * 0.83, screenBounds.getHeight * 0.74)

    scene.getStylesheets.add(getClass.getResource("/styles.css").toExternalForm)
    scene.getStylesheets.add(new PrimerLight().getUserAgentStylesheet)

    primaryStage.setTitle("PingRPC")
    primaryStage.setScene(scene)
    primaryStage.show()
  }
}
