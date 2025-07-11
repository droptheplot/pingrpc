package pingrpc.form

import com.google.protobuf.Descriptors.Descriptor
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import pingrpc.ui.boldFont

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

case class FormRoot(descriptor: Descriptor, children: Seq[Form]) extends Form with MessageBuilder {
  override def toNode: Node =
    new VBox()
      .tap(_.setSpacing(10))
      .tap(_.getStyleClass.add("form-root"))
      .tap(_.getChildren.add(new Label(descriptor.getFullName).tap(_.setFont(boldFont))))
      .tap(_.getChildren.addAll(children.map(_.toNode).asJava))
}
