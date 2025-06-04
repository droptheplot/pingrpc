package pingrpc.ui.views

import com.typesafe.scalalogging.StrictLogging
import javafx.scene.layout._
import pingrpc.ui.controllers.AppController

import scala.util.chaining.scalaUtilChainingOps

class AppView(appController: AppController) extends FlowPane with StrictLogging {
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
    .setOnAction(appController.methodAction(responseView.responseMessageLabel, requestView.submitButton, requestView.formPane)(_))

  requestView.servicesBox
    .setOnAction(appController.serviceAction(requestView.urlField, requestView.methodsBox)(_))

  requestView.submitButton
    .setOnAction(
      appController.submitAction(
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
    .addListener(appController.requestTabsListener(requestView.jsonArea, requestView.formPane, requestView.methodsBox))

  requestView.syncButton
    .setOnAction(appController.syncAction(requestView.urlField, requestView.servicesBox, requestView.methodsBox)(_))

  appController.applyState(
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
