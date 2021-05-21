package app;

import events.ViewEvent;
import i18n.I18N;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import landingpage.LandingPageView;
import ui.Dialogs;
import util.BaseView;
import util.ImageAnnotationView;
import util.VideoAnnotationView;
import util.VideoReviewView;

import java.io.InputStream;

public class MainApplication extends Application implements EventHandler<ViewEvent> {

    private Stage primaryStage;
    private final ObjectProperty<BaseView> loadedView;
    private LandingPageView landingPageView;

    public MainApplication() {
        loadedView = new SimpleObjectProperty<>();
    }

    @Override
    public void init() {
        // manually trigger the static block to set system properties
        try {
            Class.forName("util.SystemProperties");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        loadedView.addListener((observable, oldView, newView) -> {
            primaryStage.titleProperty().unbind();
            if (oldView != null) {
                oldView.close();
            }
            if (newView != null) {
                primaryStage.setScene(newView.getScene());
                primaryStage.sizeToScene();
                primaryStage.centerOnScreen();
                primaryStage.titleProperty().bind(Bindings.createStringBinding(() -> {
                    if (newView.getTitle().isEmpty()) return I18N.getString("app.name");
                    else return I18N.getString("app.name") + " - " + newView.getTitle();
                }, newView.titleProperty()));
            } else primaryStage.setTitle(I18N.getString("app.name"));
        });

    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle(I18N.getString("app.name"));

        InputStream isLogo = getClass().getClassLoader().getResourceAsStream("logo_polygon.png");
        if (isLogo != null) {
            primaryStage.getIcons().add(new Image(isLogo));
        }

        // register view event changes
        primaryStage.addEventHandler(ViewEvent.MENU, this);
        primaryStage.addEventHandler(ViewEvent.IMAGE_ANNOTATION, this);
        primaryStage.addEventHandler(ViewEvent.VIDEO_REVIEW, this);
        primaryStage.addEventHandler(ViewEvent.VIDEO_ANNOTATION, this);

        // display exit confirmation dialog when window close event is fired
        primaryStage.setOnCloseRequest(event -> {
            ButtonType exitType = Dialogs.exitConfirmation().orElse(ButtonType.CANCEL);
            if (exitType == ButtonType.CANCEL) event.consume();
            else loadedView.set(null); // trigger close routine of current view
        });

        // view on start up
        landingPageView = new LandingPageView(this, primaryStage);
        loadedView.set(landingPageView);

        primaryStage.show();
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
    }

    @Override
    public void stop() {
        // none
    }

    @Override
    public void handle(ViewEvent event) {
        event.consume();
        if (event.getEventType() == ViewEvent.MENU) {
            ButtonType backType = Dialogs.menuConfirmation().orElse(ButtonType.CANCEL);
            if (backType == ButtonType.CANCEL) event.consume();
            else loadedView.set(landingPageView);
        } else if (event.getEventType() == ViewEvent.IMAGE_ANNOTATION) {
            loadedView.set(new ImageAnnotationView(this, primaryStage));
        } else if (event.getEventType() == ViewEvent.VIDEO_REVIEW) {
            loadedView.set(new VideoReviewView(this, primaryStage));
        } else if (event.getEventType() == ViewEvent.VIDEO_ANNOTATION) {
            loadedView.set(new VideoAnnotationView(this, primaryStage));
            Dialogs.showWarning("Experimental", null, "Video Annotation is in an experimental state.");
        }
    }
}
