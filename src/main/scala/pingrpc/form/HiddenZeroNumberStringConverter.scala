package pingrpc.form

import javafx.util.converter.NumberStringConverter

import java.text.NumberFormat

class HiddenZeroNumberStringConverter(numberFormat: NumberFormat) extends NumberStringConverter(numberFormat) {
  override def toString(number: Number): String =
    if (number.shortValue == 0) ""
    else super.toString(number)
}
