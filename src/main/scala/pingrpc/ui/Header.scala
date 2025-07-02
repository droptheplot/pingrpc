package pingrpc.ui

import javafx.beans.property.{SimpleStringProperty, StringProperty}

case class Header(key: StringProperty, value: StringProperty)

object Header {
  def empty: Header = Header(new SimpleStringProperty(), new SimpleStringProperty())
}
