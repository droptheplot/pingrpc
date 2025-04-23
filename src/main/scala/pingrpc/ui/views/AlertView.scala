package pingrpc.ui.views

import javafx.scene.control.Alert

class AlertView(header: String, content: String) extends Alert(Alert.AlertType.ERROR) {
  setTitle("Something went wrong")
  setHeaderText(header)
  setContentText(content)
}
