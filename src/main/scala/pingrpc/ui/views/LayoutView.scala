package pingrpc.ui.views

import com.typesafe.scalalogging.StrictLogging
import javafx.scene.layout._
import pingrpc.ui.controllers.ActionController

import scala.util.chaining.scalaUtilChainingOps

class LayoutView(controller: ActionController) extends FlowPane with StrictLogging {
  private val requestView = new RequestView
  private val responseView = new ResponseView

  private val gridPane = new GridPane()
    .tap(_.getColumnConstraints.add(new ColumnConstraints().tap(_.setPercentWidth(50))))
    .tap(_.getColumnConstraints.add(new ColumnConstraints().tap(_.setPercentWidth(50))))
    .tap { pane =>
      requestView.prefHeightProperty.bind(pane.heightProperty)
      responseView.prefHeightProperty.bind(pane.heightProperty)
    }
    .tap(_.add(requestView, 0, 0))
    .tap(_.add(responseView, 1, 0))

  setHgap(10)
  setVgap(10)
  setPrefHeight(Double.MaxValue)
  setMinHeight(Double.MaxValue)
  getChildren.add(gridPane)

  gridPane.prefWidthProperty.bind(widthProperty)
  gridPane.prefHeightProperty.bind(heightProperty)

  requestView.methodsBox
    .setOnAction(controller.methodAction(responseView.responseMessageLabel, requestView.submitButton, requestView.formPane)(_))

  requestView.servicesBox
    .setOnAction(controller.serviceAction(requestView.urlField, requestView.methodsBox)(_))

  requestView.submitButton
    .setOnAction(
      controller.submitAction(
        requestView.urlField,
        requestView.servicesBox,
        requestView.methodsBox,
        requestView.formPane,
        requestView.tabPane,
        requestView.jsonArea,
        responseView.curlArea,
        responseView.jsonArea,
        responseView.metadataView.headers
      )(_)
    )

  requestView.tabPane.getSelectionModel.selectedItemProperty
    .addListener(controller.requestTabsListener(requestView.jsonArea, requestView.formPane, requestView.methodsBox))

  requestView.syncButton
    .setOnAction(controller.syncAction(requestView.urlField, requestView.servicesBox, requestView.methodsBox)(_))

  controller.applyState(
    requestView.urlField,
    requestView.servicesBox,
    requestView.methodsBox,
    responseView.metadataView.headers,
    requestView.formPane,
    responseView.jsonArea,
    requestView.submitButton,
    responseView.responseMessageLabel
  )
}
