package pingrpc.ui

import javafx.animation.AnimationTimer
import javafx.scene.control.Label

import java.time.Duration

class RequestTimer(label: Label) extends AnimationTimer {
  private val startedAt: Long = System.nanoTime()

  def handle(now: Long): Unit = {
    val duration = Duration.ofNanos(System.nanoTime() - startedAt)
    label.setText(s"${duration.toSecondsPart}.${duration.toMillisPart} seconds")
  }
}

