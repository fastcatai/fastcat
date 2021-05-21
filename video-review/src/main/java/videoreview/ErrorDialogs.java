package videoreview;

import javafx.scene.control.Alert;

public class ErrorDialogs {

    public static void showFolderContentError(String contentText) {
        showErrorAlert(
                "An error has occurred regarding\nthe content of the selected folder",
                contentText);
    }

    public static void showErrorAlert(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }
}
