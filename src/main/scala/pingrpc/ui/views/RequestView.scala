package pingrpc.ui.views

import javafx.geometry.Insets
import javafx.scene.control._
import javafx.scene.layout.{HBox, Pane, VBox}
import pingrpc.ui.{boldFont, grayColor}

import scala.util.chaining.scalaUtilChainingOps

class RequestView(
    urlField: TextField,
    jsonArea: TextArea,
    syncButton: Button,
    submitButton: Button,
    servicesBox: ComboBox[_],
    methodsBox: ComboBox[_],
    formPane: ScrollPane,
    tabPane: TabPane
) extends VBox {
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

  setSpacing(10)
  setPadding(new Insets(10, 5, 10, 10))
  getChildren.add(requestLabel)
  getChildren.add(submitPane)
  getChildren.add(servicesBox)
  getChildren.add(methodsBox)
  getChildren.add(tabPane)
}
