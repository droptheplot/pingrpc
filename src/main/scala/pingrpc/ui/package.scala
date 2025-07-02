package pingrpc

import javafx.beans.property.SimpleStringProperty
import javafx.collections.{FXCollections, ObservableList}
import javafx.scene.text.{Font, FontWeight}

import scala.jdk.CollectionConverters._

package object ui {
  val boldFont: Font = Font.font("Helvetica", FontWeight.EXTRA_BOLD, 13)

  def headersToMap(headers: ObservableList[Header]): Map[String, String] =
    headers.asScala
      .map { case Header(key, value) => key.getValueSafe -> value.getValueSafe }
      .filter { case (key, value) => key.nonEmpty && value.nonEmpty }
      .toMap

  def headersFromMap(headers: Map[String, String]): ObservableList[Header] =
    FXCollections.observableArrayList {
      headers.map { case (key, value) => Header(new SimpleStringProperty(key), new SimpleStringProperty(value)) }.toList.asJava
    }
}
