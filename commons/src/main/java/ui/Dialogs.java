package ui;

import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public class Dialogs {

    public static void showError(String title, String header, String content) {
        showError(title, header, content, null);
    }

    public static void showError(String title, String header, String content, EventHandler<DialogEvent> onCloseRequest) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        InputStream iconIS = Dialogs.class.getClassLoader().getResourceAsStream("logo_polygon.png");
        if (iconIS != null) ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(iconIS));
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setOnCloseRequest(onCloseRequest);
        alert.show();
    }

    public static void showWarning(String title, String header, String content) {
        showWarning(title, header, content, null);
    }

    public static void showWarning(String title, String header, String content, EventHandler<DialogEvent> onCloseRequest) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        InputStream iconIS = Dialogs.class.getClassLoader().getResourceAsStream("logo_polygon.png");
        if (iconIS != null) ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(iconIS));
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setOnCloseRequest(onCloseRequest);
        alert.show();
    }

    public static Alert createInformation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        InputStream iconIS = Dialogs.class.getClassLoader().getResourceAsStream("logo_polygon.png");
        if (iconIS != null) ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(iconIS));
        alert.initModality(Modality.APPLICATION_MODAL);
        return alert;
    }

    public static void showInformation(String title, String header, String content) {
        createInformation(title, header, content).show();
    }

    public static Optional<ButtonType> exitConfirmation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Confirm Exit");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to exit?");
        InputStream iconIS = Dialogs.class.getClassLoader().getResourceAsStream("logo_polygon.png");
        if (iconIS != null) ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(iconIS));
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.getButtonTypes().clear();
        alert.getButtonTypes().setAll(new ButtonType("Exit", ButtonBar.ButtonData.OK_DONE), ButtonType.CANCEL);
        return alert.showAndWait();
    }

    public static Optional<ButtonType> menuConfirmation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Back to Menu");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to back to menu?");
        InputStream iconIS = Dialogs.class.getClassLoader().getResourceAsStream("logo_polygon.png");
        if (iconIS != null) ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(iconIS));
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.getButtonTypes().clear();
        alert.getButtonTypes().setAll(new ButtonType("Back", ButtonBar.ButtonData.OK_DONE), ButtonType.CANCEL);
        return alert.showAndWait();
    }

    public static void showExceptionDialog(Exception exception) {
        showExceptionDialog("Error", exception.getMessage(), exception);
    }

    public static void showExceptionDialog(String title, Exception exception) {
        showExceptionDialog(title, exception.getMessage(), exception);
    }

    public static void showExceptionDialog(String title, String content, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Ups, an error occurred");
        alert.setContentText(content);

        InputStream iconIS = Dialogs.class.getClassLoader().getResourceAsStream("logo_polygon.png");
        if (iconIS != null) ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(iconIS));

        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));

        TextArea textArea = new TextArea(sw.toString());
        textArea.setEditable(false);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(new Label("The exception stacktrace was:"), 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }
}
