package pingrpc.ui.tasks

import cats.effect.unsafe.implicits.global
import com.google.protobuf.Descriptors.FileDescriptor
import com.google.protobuf.{Descriptors, InvalidProtocolBufferException, Message}
import io.grpc.StatusException
import javafx.animation.AnimationTimer
import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.scene.control.Button
import org.fxmisc.richtext.CodeArea
import pingrpc.grpc.{FullMessageName, Sender}
import pingrpc.proto.ProtoUtils
import pingrpc.ui.views.AlertView
import pingrpc.ui.{Header, headersFromMap, headersToMap}
import protobuf.MethodOuterClass.Method

class SendTask(
    message: Message,
    methodName: String,
    url: String,
    timer: AnimationTimer,
    jsonArea: CodeArea,
    requestHeaders: ObservableList[Header],
    responseHeaders: ObservableList[Header],
    sender: Sender,
    fileDescriptors: List[FileDescriptor],
    method: Method,
    sendButton: Button
) extends Task[Unit] {
  override def call(): Unit = {
    timer.start()
    sendButton.setDisable(true)

    val responseOpt = for {
      requestDescriptor <- findMessageDescriptor(fileDescriptors, method.getInputType)
      responseDescriptor <- findMessageDescriptor(fileDescriptors, method.getOutputType)
      metadata = headersToMap(requestHeaders)
      response <- sender.send(requestDescriptor, responseDescriptor, methodName, url, message, metadata).attempt.unsafeRunSync
    } yield response

    sendButton.setDisable(false)
    timer.stop()

    responseOpt match {
      case Right(response) =>
        Platform.runLater(() => {
          responseHeaders.clear()
          responseHeaders.addAll(headersFromMap(response.headers))
          jsonArea.replaceText(response.message)
        })
      case Left(error: StatusException) =>
        Platform.runLater(() => new AlertView("Server responded with an error", error.getMessage).showAndWait)
      case Left(error: InvalidProtocolBufferException) =>
        Platform.runLater(() => new AlertView("Cannot build request", error.getMessage).showAndWait)
      case Left(error) =>
        Platform.runLater(() => new AlertView("Unknown error", error.toString).showAndWait)
    }
  }

  private def findMessageDescriptor(fileDescriptors: List[FileDescriptor], name: String): Either[Throwable, Descriptors.Descriptor] =
    for {
      fullMessageName <- FullMessageName
        .parse(name)
        .toRight(new Throwable(s"Cannot get full message name from `$name`"))
      descriptor <- ProtoUtils
        .findMessageDescriptor(fileDescriptors, fullMessageName)
        .toRight(new Throwable(s"Cannot find response descriptor for `$fullMessageName`"))
    } yield descriptor
}
