package pingrpc

import javafx.geometry.Insets
import javafx.scene.layout.{Background, BackgroundFill, Border, BorderStroke, BorderStrokeStyle, BorderWidths, CornerRadii}
import javafx.scene.paint.Color
import javafx.scene.text.{Font, FontWeight}

package object ui {
  val monospacedFont: Font = Font.font("monospaced")
  val boldFont: Font = Font.font("Helvetica", FontWeight.EXTRA_BOLD, 13)

  val grayColor: Color = Color.valueOf("8c959f")
  val lightGrayColor: Color = Color.valueOf("d0d7de")
  val veryLightGrayColor: Color = Color.valueOf("f6f8fa")

  val areaBackground: Background = new Background(new BackgroundFill(veryLightGrayColor, CornerRadii.EMPTY, Insets.EMPTY))
  val areaBorder = new Border(new BorderStroke(lightGrayColor, BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(1)))
  val areaInsets = new Insets(5, 7, 5, 7)
}
