package landingpage;

import about.AboutStage;
import events.ViewEvent;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import settings.SettingsStage;
import util.BaseController;

public class LandingPageController implements BaseController {

    @FXML
    private VBox buttonVideoReview;
    @FXML
    private VBox buttonVideoAnnotation;
    @FXML
    private VBox buttonImageAnnotation;
    @FXML
    private HBox buttonSettings;
    @FXML
    public HBox buttonAbout;

    private final Application application;
    private final Stage primaryStage;

    public LandingPageController(Application application, Stage primaryStage) {
        this.application = application;
        this.primaryStage = primaryStage;
    }

    @FXML
    private void initialize() {
        buttonImageAnnotation.setOnMouseClicked(event ->
                primaryStage.fireEvent(new ViewEvent(primaryStage, ViewEvent.IMAGE_ANNOTATION)));
        buttonVideoReview.setOnMouseClicked(event ->
                primaryStage.fireEvent(new ViewEvent(primaryStage, ViewEvent.VIDEO_REVIEW)));
        buttonVideoAnnotation.setOnMouseClicked(event ->
                primaryStage.fireEvent(new ViewEvent(primaryStage, ViewEvent.VIDEO_ANNOTATION)));
        buttonSettings.setOnMouseClicked(event -> new SettingsStage().show());
        buttonAbout.setOnMouseClicked(event -> AboutStage.show(application));
    }

    @Override
    public void close() {
        // nothing
    }
}
