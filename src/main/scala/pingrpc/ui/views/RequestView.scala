package pingrpc.ui.views

import javafx.geometry.Insets
import javafx.scene.control._
import javafx.scene.layout.{HBox, Pane, Priority, VBox}
import org.fxmisc.richtext.CodeArea
import pingrpc.proto.{MethodConverter, ServiceConverter}
import pingrpc.ui._
import protobuf.MethodOuterClass.Method
import protobuf.ServiceOuterClass.Service

import scala.util.chaining.scalaUtilChainingOps

class RequestView extends VBox {
  val urlField: TextField = new TextField()
    .tap(_.setText("localhost:8080"))
  HBox.setHgrow(urlField, Priority.ALWAYS)

  val jsonArea: CodeArea = new CodeArea()
    .tap(_.setWrapText(true))
    .tap(_.textProperty.addListener((_, _, _) => JsonHighlighter.highlight(jsonArea)))
    .tap(_.setBorder(areaBorder))
    .tap(_.setPadding(areaInsets))
    .tap(_.setBackground(areaBackground))
    .tap(_.setStyle(areaFont))
  VBox.setVgrow(jsonArea, Priority.ALWAYS)

  val syncButton: Button = new Button("Sync")

  lazy val formPane = new ScrollPane()
  VBox.setVgrow(formPane, Priority.ALWAYS)

  val tabPane: TabPane = new TabPane()
    .tap(_.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE))
  VBox.setVgrow(tabPane, Priority.ALWAYS)

  val servicesBox: ComboBox[Service] = new ComboBox[Service]()
    .tap(_.setConverter(new ServiceConverter))
    .tap(_.setPrefWidth(Double.MaxValue))
    .tap(_.setPromptText("..."))
    .tap(_.setDisable(true))

  val methodsBox: ComboBox[Method] = new ComboBox[Method]()
    .tap(_.setConverter(new MethodConverter))
    .tap(_.setPrefWidth(Double.MaxValue))
    .tap(_.setPromptText("..."))
    .tap(_.setDisable(true))

  val sendButton: Button = new Button("Send")
    .tap(_.setDisable(true))
    .tap(_.getStyleClass.add("accent"))
    .tap(_.setDefaultButton(true))

  private val requestLabel = new Label("REQUEST")
    .tap(_.setFont(boldFont))
    .tap(_.setTextFill(grayColor))

  private lazy val sendPane: Pane = new HBox()
    .tap(_.setSpacing(10))
    .tap(_.getChildren.add(urlField))
    .tap(_.getChildren.add(syncButton))
    .tap(_.getChildren.add(sendButton))

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
  getChildren.add(sendPane)
  getChildren.add(servicesBox)
  getChildren.add(methodsBox)
  getChildren.add(tabPane)
}
