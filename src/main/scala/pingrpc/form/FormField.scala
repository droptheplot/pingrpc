package pingrpc.form

import com.google.protobuf.Descriptors.FieldDescriptor
import javafx.scene.Node

trait FormField extends Form {
  def fieldDescriptor: FieldDescriptor

  def toNode: Node
}
