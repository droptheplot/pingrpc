package pingrpc.ui

import com.google.protobuf.DescriptorProtos.MethodDescriptorProto
import io.grpc.reflection.v1.ServiceResponse
import javafx.geometry.Insets
import javafx.scene.control.{Button, ComboBox, Label, TextArea, TextField}
import javafx.scene.layout.{HBox, Pane, Priority, VBox}

import scala.util.chaining.scalaUtilChainingOps

class RequestPane(
    urlField: TextField,
    requestArea: TextArea,
    curlArea: TextArea,
    syncButton: Button,
    submitButton: Button,
    servicesBox: ComboBox[ServiceResponse],
    methodsBox: ComboBox[MethodDescriptorProto],
    requestMessageLabel: Label
) {
  private val requestLabel = new Label("REQUEST")
    .tap(_.setFont(titleFont))
    .tap(_.setTextFill(grayColor))

  private val curlLabel = new Label("GRPCURL")
    .tap(_.setFont(titleFont))
    .tap(_.setTextFill(grayColor))

  private lazy val submitPane: Pane = new HBox()
    .tap(_.setSpacing(10))
    .tap(_.getChildren.add(urlField))
    .tap(_.getChildren.add(syncButton))
    .tap(_.getChildren.add(submitButton))

  def build: Pane = new VBox()
    .tap(_.setSpacing(10))
    .tap(_.setPadding(new Insets(10, 5, 10, 10)))
    .tap(_.getChildren.add(requestLabel))
    .tap(_.getChildren.add(submitPane))
    .tap(_.getChildren.add(servicesBox))
    .tap(_.getChildren.add(methodsBox))
    .tap(_.getChildren.add(requestMessageLabel))
    .tap(_.getChildren.add(requestArea))
    .tap(_.getChildren.add(curlLabel))
    .tap(_.getChildren.add(curlArea))
}
