package util;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.BuilderFactory;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ui.Dialogs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public abstract class BaseView implements Callback<Class<?>, Object>, BuilderFactory {
    private static final Logger logger = LogManager.getLogger(BaseView.class);

    private final Application application;
    private final Stage stage;
    private final ReadOnlyStringWrapper title;

    private final List<BaseControllerInitializable> childComponentControllers;

    private final String resourceName;

    public BaseView(final String resourceName, final Application application, final Stage stage) {
        this(resourceName, application, stage,"");
    }

    public BaseView(final String resourceName, final Application application, final Stage stage, String title) {
        this.resourceName = resourceName;
        this.application = application;
        this.stage = stage;
        this.title = new ReadOnlyStringWrapper(title);
        childComponentControllers = new ArrayList<>();

        setDefaultControllers();
//        this.scene = loadFXML(resourceName);

        // init all child components
        initialize();
    }

    public abstract BaseController getController();
    public abstract void setDefaultControllers();

    public void addChildComponentControllers(Object controller) {
        if (controller instanceof BaseControllerInitializable)
            childComponentControllers.add((BaseControllerInitializable) controller);
    }

    /**
     * Initializes all child components at the same time as parent controller.
     */
    public void initialize() {
        for (BaseControllerInitializable c : childComponentControllers)
            c.initialize(getController());
    }

    protected Scene loadFXML(final String resourceName) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource(resourceName));
            loader.setResources(ResourceBundle.getBundle("i18n.AppBundle"));
            loader.setControllerFactory(this);
            loader.setBuilderFactory(this);
            return loader.load();
        } catch (IOException e) {
            Dialogs.showExceptionDialog(logger.throwing(e));
            return null;
        }
    }

    private Scene scene;

    public Scene getScene() {
        if (scene == null) {
            scene = loadFXML(resourceName);
        }
        return scene;
    }

    public void close() {
        getController().close();
    }

    public <T extends Event> void addEventHandler(final EventType<T> eventType,
                                                  final EventHandler<? super T> eventHandler) {
        getScene().addEventHandler(eventType, eventHandler);
    }

    public <T extends Event> void addEventFilter(final EventType<T> eventType,
                                                 final EventHandler<? super T> eventHandler) {
        getScene().addEventFilter(eventType, eventHandler);
    }

    public <T extends Event> void removeEventHandler(final EventType<T> eventType,
                                                     final EventHandler<? super T> eventHandler) {
        getScene().removeEventHandler(eventType, eventHandler);
    }

    public <T extends Event> void removeEventFilter(final EventType<T> eventType,
                                                    final EventHandler<? super T> eventHandler) {
        getScene().removeEventFilter(eventType, eventHandler);
    }

    public Application getApplication() {
        return application;
    }

    public Stage getStage() {
        return stage;
    }

    public String getTitle() {
        return title.get();
    }

    public ReadOnlyStringProperty titleProperty() {
        return title.getReadOnlyProperty();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }
}
