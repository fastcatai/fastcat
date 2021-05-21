package about;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;

public class AboutStage {

    private final Stage stage;

    private AboutStage(Application application) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(AboutStage.class.getResource("/about/About.fxml"));
        loader.setResources(ResourceBundle.getBundle("i18n.AppBundle"));
        loader.setController(new AboutController(application));
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        InputStream isLogo = getClass().getClassLoader().getResourceAsStream("logo_polygon.png");
        if (isLogo != null) {
            stage.getIcons().add(new Image(isLogo));
        }

        try {
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAndWait() {
        stage.showAndWait();
        stage.sizeToScene();
        stage.centerOnScreen();
    }

    public static void show(Application application) {
        AboutStage aboutStage = new AboutStage(application);
        aboutStage.showAndWait();
    }
}
