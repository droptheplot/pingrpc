package pingrpc

import javafx.scene.paint.Color
import javafx.scene.text.{Font, FontWeight}

package object ui {
  val monospacedFont: Font = Font.font("monospaced")
  val boldFont: Font = Font.font("Helvetica", FontWeight.EXTRA_BOLD, 13)

  val grayColor: Color = Color.valueOf("8c959f")
  val lightGrayColor: Color = Color.valueOf("d0d7de")
}
