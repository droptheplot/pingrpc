package pingrpc.form

import com.google.protobuf.Descriptors.FieldDescriptor.JavaType
import com.google.protobuf.Descriptors.{Descriptor, FieldDescriptor}
import com.google.protobuf.{Descriptors, Message}
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

  def build(descriptor: Descriptor, messageOpt: Option[Message]): Form =
    FormRoot(descriptor.getFullName, descriptor.getFields.asScala.map(Form.build(_, messageOpt)).toList)

  private def build(fieldDescriptor: FieldDescriptor, messageOpt: Option[Message]): Form = fieldDescriptor.getJavaType match {
    case JavaType.MESSAGE =>
      val nestedMessageOpt: Option[Message] = messageOpt.flatMap { message =>
        Option(message.getField(fieldDescriptor)).collect { case message: Message => message }
      }

      FormMessage(fieldDescriptor, fieldDescriptor.getMessageType.getFields.asScala.map(Form.build(_, nestedMessageOpt)).toList)
    case _ =>
      val valueOpt: Option[Any] = messageOpt
        .map(message => message.getField(fieldDescriptor))
        .map {
          case v: java.util.List[_] if v.size > 0 => v.getFirst
          case v => v
        }

      val node = fieldDescriptor.getJavaType match {
        case JavaType.STRING | JavaType.BYTE_STRING =>
          new TextField()
            .tap(_.setPromptText(fieldDescriptor.getType.toString.toLowerCase))
            .tap { textField =>
              valueOpt
                .flatMap(value => Option(value).collect { case v: String if v.nonEmpty => v })
                .map(value => textField.setText(value))
            }
        case JavaType.INT | JavaType.LONG =>
          new TextField()
            .tap(_.setPromptText(fieldDescriptor.getType.toString.toLowerCase))
            .tap { textField =>
              valueOpt
                .flatMap(value =>
                  Option(value)
                    .collect {
                      case v: Long => v
                      case v: Int => v.toLong
                    }
                    .filter(_ > 0)
                )
                .map(value => textField.setText(value.toString))
            }
        case JavaType.DOUBLE | JavaType.FLOAT =>
          new TextField()
            .tap(_.setPromptText(fieldDescriptor.getType.toString.toLowerCase))
            .tap { textField =>
              valueOpt
                .flatMap(value =>
                  Option(value)
                    .collect {
                      case v: Double => v
                      case v: Float => v.toDouble
                    }
                    .filter(_ > 0.0)
                )
                .map(value => textField.setText(value.toString))
            }
        case JavaType.BOOLEAN =>
          new CheckBox(fieldDescriptor.getName)
            .tap(_.setMnemonicParsing(false))
            .tap { checkBox =>
              valueOpt
                .flatMap(value => Option(value).collect { case v: Boolean => v })
                .map(value => checkBox.setSelected(value))
            }
        case JavaType.ENUM =>
          new ComboBox[Descriptors.EnumValueDescriptor]
            .tap(_.setConverter(new EnumValueDescriptorConverter))
            .tap(_.getItems.addAll(fieldDescriptor.getEnumType.getValues))
            .tap(_.getSelectionModel.select(0))
            .tap { comboBox =>
              valueOpt
                .flatMap(value => Option(value).collect { case v: Descriptors.EnumValueDescriptor => v })
                .map(value => comboBox.getSelectionModel.select(value))
            }
      }

      FormField(fieldDescriptor, node)
  }
}
