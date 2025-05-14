package pingrpc.storage

import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import net.harawata.appdirs.AppDirsFactory
import protobuf.StateOuterClass.State

import java.nio.file.{FileAlreadyExistsException, Files, Path}

class StateManager extends StrictLogging {
  var currentState: State = State.newBuilder().build()

  def save(state: State): IO[Unit] =
    for {
      file <- getStoreFile
      _ <- IO(Files.createFile(file)).recoverWith { case _: FileAlreadyExistsException => IO.unit }
      _ <- IO(Files.write(file, state.toByteArray))
      _ = { currentState = state }
    } yield ()

  def load(): IO[State] =
    for {
      file <- getStoreFile
      state <- IO(Files.readAllBytes(file))
        .map(State.parseFrom)
        .recoverWith(e => IO(logger.error("Cannot parse state", e)) *> IO(State.getDefaultInstance))
      _ = { currentState = state }
    } yield state

  def update(f: State.Builder => State.Builder): IO[Unit] =
    save(f(currentState.toBuilder).build)

  private def getStoreFile: IO[Path] =
    for {
      userDataDir <- IO(AppDirsFactory.getInstance.getUserDataDir("PingRPC", null, "$USER"))
      userDataPath <- IO(Path.of(userDataDir))
      file <- IO(userDataPath.resolve("store.txt"))
    } yield file
}
