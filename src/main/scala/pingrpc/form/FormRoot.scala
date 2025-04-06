package pingrpc.form

import io.circe.Json
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import pingrpc.ui.boldFont

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

case class FormRoot(name: String, children: Seq[Form]) extends Form {

  override def toNode: Node =
    new VBox()
      .tap(_.setSpacing(10))
      .tap(_.setPadding(new Insets(7, 0, 10, 15)))
      .tap(_.getChildren.add(new Label(name).tap(_.setFont(boldFont))))
      .tap(_.getChildren.addAll(children.map(_.toNode).asJava))

  override def toJson: Json = children
    .foldLeft(Json.obj()) { case (acc, form) => form.toJson.deepMerge(acc) }
    .dropEmptyValues
}
