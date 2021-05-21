package util;

import annotation.UIAnnotation;
import controller.IAMenuBarController;
import controller.ImageAnnotationController;
import controller.ListViewAnnotations;
import javafx.application.Application;
import javafx.collections.ObservableMap;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Builder;
import keymap.Program;
import keymap.manager.IAShortcutManager;
import keymap.manager.SMFactory;
import keymap.model.ReadOnlyKeyActionClass;
import model.ImageData;

public class ImageAnnotationView extends BaseView {

    private ImageAnnotationController controller;
    private IAMenuBarController menuBarController;

    public ImageAnnotationView(final Application application, final Stage stage) {
        super("/fxml/ImageAnnotation.fxml", application, stage, "Image Annotation");
        addKeyEvents();
    }

    private void addKeyEvents() {
        final IAShortcutManager sm = (IAShortcutManager) SMFactory.getShortcutManager(Program.IMAGE_ANNOTATION);
        final ObservableMap<String, ReadOnlyKeyActionClass> classMappings = sm.getClassActionMappings();
        getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            for (String c : classMappings.keySet()) {
                if (classMappings.get(c).getKeyCombination().match(event)) {
                    ImageData data = controller.getCurrentSelectedImageData();
                    if (data != null) {
                        if (!data.getImageClassification().getAdditionalLabels().contains(c))
                            data.getImageClassification().addAdditionalLabel(c);
                        else data.getImageClassification().removeAdditionalLabel(c);
                    }
                    if (controller.getCheckBoxAutoNext().isSelected())
                        controller.getMenuBarController().selectNextImage();
                    break;
                }
            }
        });
    }

    @Override
    public ImageAnnotationController getController() {
        return controller;
    }

    @Override
    public void setDefaultControllers() {
        controller = new ImageAnnotationController(this);
        menuBarController = new IAMenuBarController(controller);
    }

    @Override
    public Object call(Class<?> param) {
        if (param == ImageAnnotationController.class) {
            return controller;
        } else if (param == IAMenuBarController.class) {
            return menuBarController;
        } else return null;
    }

    @Override
    public Builder<?> getBuilder(Class<?> type) {
        if (type == ListViewAnnotations.class)
            return (Builder<ListViewAnnotations>) () -> new ListViewAnnotations(controller);
        return null;
    }
}
