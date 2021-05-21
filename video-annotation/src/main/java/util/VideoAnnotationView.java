package util;

import controller.TimeTableController;
import controller.VAMenuBarController;
import controller.VideoAnnotationController;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.util.Builder;

public class VideoAnnotationView extends BaseView {

    private VideoAnnotationController controller;
    private VAMenuBarController menuBarController;

    public VideoAnnotationView(final Application application, final Stage stage) {
        super("/fxml/VideoAnnotationV2.fxml", application, stage, "Video Annotation");
    }

    @Override
    public VideoAnnotationController getController() {
        return controller;
    }

    @Override
    public void setDefaultControllers() {
        controller = new VideoAnnotationController(this);
        menuBarController = new VAMenuBarController(controller);
    }

    @Override
    public Object call(Class<?> param) {
        Object obj = null;
        if (param == VideoAnnotationController.class) obj = controller;
        else if (param == VAMenuBarController.class) obj = menuBarController;
        else if (param == TimeTableController.class) obj = new TimeTableController(controller);
        addChildComponentControllers(obj);
        return obj;
    }

    @Override
    public Builder<?> getBuilder(Class<?> type) {
        return null;
    }
}
