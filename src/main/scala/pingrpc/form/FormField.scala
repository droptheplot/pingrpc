package pingrpc.form

import com.google.protobuf.Descriptors
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType
import io.circe.Json
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.{CheckBox, ComboBox, Label, TextField}
import javafx.scene.layout.HBox

import scala.util.chaining.scalaUtilChainingOps

case class FormField(fieldDescriptor: FieldDescriptor, node: Node) extends Form {

  override def toNode: Node = fieldDescriptor.getJavaType match {
    case JavaType.BOOLEAN =>
      new HBox().tap(_.getChildren.add(node))
    case _ =>
      new HBox()
        .tap(_.setSpacing(10))
        .tap(_.setAlignment(Pos.CENTER_LEFT))
        .tap(_.getChildren.add(new Label(fieldDescriptor.getName)))
        .tap(_.getChildren.add(node))
  }

  override def toJson: Json = fieldDescriptor.getJavaType match {
    case JavaType.INT | JavaType.LONG | JavaType.FLOAT | JavaType.DOUBLE =>
      parseNumberToJson(fieldDescriptor.getJavaType, node.asInstanceOf[TextField].getText)
        .map(value => Json.obj(fieldDescriptor.getName -> wrapJson(fieldDescriptor, value)))
        .getOrElse(Json.obj())
    case JavaType.BOOLEAN =>
      Option(node.asInstanceOf[CheckBox].isSelected)
        .filter(_ == true)
        .map(_ => Json.obj(fieldDescriptor.getName -> wrapJson(fieldDescriptor, Json.fromBoolean(true))))
        .getOrElse(Json.obj())
    case JavaType.STRING | JavaType.BYTE_STRING | JavaType.LONG | JavaType.DOUBLE =>
      Option(node.asInstanceOf[TextField].getText)
        .filter(_.nonEmpty)
        .map(value => Json.obj(fieldDescriptor.getName -> wrapJson(fieldDescriptor, Json.fromString(value))))
        .getOrElse(Json.obj())
    case JavaType.ENUM =>
      val enumValueDescriptor = node.asInstanceOf[ComboBox[Descriptors.EnumValueDescriptor]].getSelectionModel.getSelectedItem

      if (enumValueDescriptor.getIndex == 0) Json.obj()
      else Json.obj(fieldDescriptor.getName -> wrapJson(fieldDescriptor, Json.fromString(enumValueDescriptor.getName)))
  }
}
