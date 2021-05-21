package settings;

import i18n.I18N;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.Dialogs;

import java.io.IOException;
import java.io.InputStream;

public class SettingsStage {

    private final Stage stage;

    public SettingsStage() {
        stage = new Stage();
        loadContent();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(I18N.getString("settings.title"));
        InputStream iconIS = Dialogs.class.getClassLoader().getResourceAsStream("logo_polygon.png");
        if (iconIS != null) stage.getIcons().add(new Image(iconIS));
        stage.setMinWidth(950);
        stage.setMinHeight(600);
        stage.setWidth(stage.getMinWidth());
        stage.setHeight(stage.getMinHeight());
    }

    public void show() {
        stage.show();
        stage.centerOnScreen();
    }

    private void loadContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Settings.fxml"));
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
