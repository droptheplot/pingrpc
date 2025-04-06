package pingrpc.form

import com.google.protobuf.Descriptors
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType
import com.google.protobuf.Descriptors.{Descriptor, FieldDescriptor}
import io.circe.Json
import javafx.scene.Node
import javafx.scene.control.{CheckBox, ComboBox, TextField}
import pingrpc.proto.EnumValueDescriptorConverter

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

trait Form {
  def toNode: Node

  def toJson: Json
}

object Form {

  def build(descriptor: Descriptor): Form =
    FormRoot(descriptor.getFullName, descriptor.getFields.asScala.map(Form.build).toList)

  def build(fieldDescriptor: FieldDescriptor): Form = fieldDescriptor.getJavaType match {
    case JavaType.MESSAGE => FormMessage(fieldDescriptor, fieldDescriptor.getMessageType.getFields.asScala.map(Form.build).toList)
    case _ =>
      val node = fieldDescriptor.getJavaType match {
        case JavaType.STRING | JavaType.INT | JavaType.LONG | JavaType.DOUBLE | JavaType.FLOAT | JavaType.BYTE_STRING =>
          new TextField().tap(_.setPromptText(fieldDescriptor.getType.toString.toLowerCase))
        case JavaType.BOOLEAN =>
          new CheckBox(fieldDescriptor.getName).tap(_.setMnemonicParsing(false))
        case JavaType.ENUM =>
          new ComboBox[Descriptors.EnumValueDescriptor]
            .tap(_.setConverter(new EnumValueDescriptorConverter))
            .tap(_.getItems.addAll(fieldDescriptor.getEnumType.getValues))
            .tap(_.getSelectionModel.select(0))
      }

      FormField(fieldDescriptor, node)
  }
}
