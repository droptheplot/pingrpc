package pingrpc

import java.text.NumberFormat
import scala.util.chaining.scalaUtilChainingOps

package object form {
  private val numberFormat = NumberFormat.getNumberInstance.tap(_.setGroupingUsed(false))

  val numberStringConverter = new HiddenZeroNumberStringConverter(numberFormat)
}
