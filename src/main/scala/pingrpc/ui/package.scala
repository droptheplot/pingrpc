package pingrpc

import javafx.scene.paint.Color
import javafx.scene.text.{Font, FontWeight}

package object ui {
  val monospacedFont: Font = Font.font("monospaced")

  val titleFont: Font = Font.font("System", FontWeight.EXTRA_BOLD, 13)

  val grayColor = new Color(0.6, 0.6, 0.6, 1)
}
