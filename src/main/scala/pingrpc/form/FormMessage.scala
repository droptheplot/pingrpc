package pingrpc.form

import com.google.protobuf.Descriptors.FieldDescriptor
import io.circe.Json
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout._
import pingrpc.ui.{boldFont, lightGrayColor}

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

case class FormMessage(fieldDescriptor: FieldDescriptor, children: Seq[Form]) extends Form {

  override def toNode: Node = {
    val message = new VBox()
      .tap(_.setSpacing(10))
      .tap(_.setPadding(new Insets(0, 0, 0, 10)))
      .tap(_.setBorder(new Border(new BorderStroke(lightGrayColor, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 0, 2)))))
      .tap(_.getChildren.add(new Label(fieldDescriptor.getMessageType.getFullName).tap(_.setFont(boldFont))))
      .tap(_.getChildren.addAll(children.map(_.toNode).asJava))

    new VBox()
      .tap(_.setSpacing(10))
      .tap(_.getChildren.add(new Label(fieldDescriptor.getName)))
      .tap(_.getChildren.add(message))
  }

  override def toJson: Json =
    Json.obj {
      fieldDescriptor.getName ->
        children
          .foldLeft(Json.obj()) { case (acc, form) => form.toJson.deepMerge(acc) }
          .asObject
          .filter(_.nonEmpty)
          .map(jsonObject => wrapJson(fieldDescriptor, jsonObject.toJson))
          .getOrElse(Json.obj())
          .dropEmptyValues
    }
}
