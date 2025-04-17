package pingrpc.ui

import com.google.protobuf.DescriptorProtos.MethodDescriptorProto
import io.grpc.reflection.v1.ServiceResponse
import javafx.geometry.Insets
import javafx.scene.control._
import javafx.scene.layout.{HBox, Pane, VBox}

import scala.util.chaining.scalaUtilChainingOps

class RequestPane(
    urlField: TextField,
    jsonArea: TextArea,
    syncButton: Button,
    submitButton: Button,
    servicesBox: ComboBox[ServiceResponse],
    methodsBox: ComboBox[MethodDescriptorProto],
    formPane: ScrollPane,
    tabPane: TabPane
) {
  private val requestLabel = new Label("REQUEST")
    .tap(_.setFont(boldFont))
    .tap(_.setTextFill(grayColor))

  private lazy val submitPane: Pane = new HBox()
    .tap(_.setSpacing(10))
    .tap(_.getChildren.add(urlField))
    .tap(_.getChildren.add(syncButton))
    .tap(_.getChildren.add(submitButton))

  private val formContainer = new VBox()
    .tap(_.setPadding(new Insets(10, 0, 0, 0)))
    .tap(_.getChildren.add(formPane))

  private val jsonContainer = new VBox()
    .tap(_.setPadding(new Insets(10, 0, 0, 0)))
    .tap(_.getChildren.add(jsonArea))

  private val formTab = new Tab("FORM", formContainer)
    .tap(_.setId("form"))
  private val jsonTab = new Tab("JSON", jsonContainer)
    .tap(_.setId("json"))

  tabPane.getTabs.add(formTab)
  tabPane.getTabs.add(jsonTab)

  def build: Pane = new VBox()
    .tap(_.setSpacing(10))
    .tap(_.setPadding(new Insets(10, 5, 10, 10)))
    .tap(_.getChildren.add(requestLabel))
    .tap(_.getChildren.add(submitPane))
    .tap(_.getChildren.add(servicesBox))
    .tap(_.getChildren.add(methodsBox))
    .tap(_.getChildren.add(tabPane))
}
