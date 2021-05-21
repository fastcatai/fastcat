package util;

import controller.FrameModeController;
import controller.VRMenuBarController;
import controller.VideoModeController;
import controller.VideoReviewController;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableMap;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Builder;
import keymap.Program;
import keymap.manager.SMFactory;
import keymap.manager.VRShortcutManager;
import keymap.model.ActionVR;
import keymap.model.ReadOnlyKeyActionClass;
import ui.FlowAnnotationClasses;
import ui.ListFrameData;
import ui.ListViewClasses;
import ui.FlowFrameClasses;
import ui.ListAnnotatedImages;
import ui.ListViewAnnotations;

public class VideoReviewView extends BaseView {

    private VideoReviewController controller;
    private VRMenuBarController menuBarController;
    private FrameModeController fmController;
    private VideoModeController vmController;

    public VideoReviewView(final Application application, final Stage stage) {
        super("/fxml/VideoReview.fxml", application, stage, "Video Review");
        addKeyEvents();
    }

    private void addKeyEvents() {
        final VRShortcutManager sm = (VRShortcutManager) SMFactory.getShortcutManager(Program.VIDEO_REVIEW);

        ReadOnlyObjectProperty<KeyCombination> actionForwardSingle = sm.getKeyAction(ActionVR.SKIP_IMAGE_FORWARD_SINGLE).keyCombinationProperty();
        ReadOnlyObjectProperty<KeyCombination> actionBackwardsSingle = sm.getKeyAction(ActionVR.SKIP_IMAGE_BACKWARDS_SINGLE).keyCombinationProperty();

        getScene().addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (actionForwardSingle.get().match(event))
                menuBarController.onSkipImageForwardSingleReleased();
            if (actionBackwardsSingle.get().match(event))
                menuBarController.onSkipImageBackwardsSingleReleased();
        });


        final ObservableMap<String, ReadOnlyKeyActionClass> classMappings = sm.getClassActionMappings();
        getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            for (String c : classMappings.keySet()) {
                if (classMappings.get(c).getInversionKeyCombination().orElse(KeyCombination.NO_MATCH).match(event)
                        || classMappings.get(c).getKeyCombination().match(event)) {
                    controller.getListClassesController().addClass(c, event.isControlDown());
                    break;
                }
            }
        });
    }

    @Override
    public void setDefaultControllers() {
        controller = new VideoReviewController(this);
        menuBarController = new VRMenuBarController(controller);
        fmController = new FrameModeController(controller);
        vmController = new VideoModeController(controller);
    }

    @Override
    public VideoReviewController getController() {
        return controller;
    }

    @Override
    public Object call(Class<?> param) {
        Object obj = null;
        if (param == VideoReviewController.class) obj = controller;
        else if (param == VRMenuBarController.class) obj = menuBarController;
        else if (param == FrameModeController.class) obj = fmController;
        else if (param == VideoModeController.class) obj = vmController;
        else if (param == ListViewClasses.class) obj = new ListViewClasses(fmController);
        else if (param == ListViewAnnotations.class) obj = new ListViewAnnotations(fmController);
        else if (param == ListAnnotatedImages.class) obj = new ListAnnotatedImages(fmController);
        else if (param == FlowFrameClasses.class) obj = new FlowFrameClasses(fmController);
        else if (param == FlowAnnotationClasses.class) obj = new FlowAnnotationClasses(fmController);
        else if (param == ListFrameData.class) obj = new ListFrameData(controller);
        if (obj != null)
            addChildComponentControllers(obj);
        return obj;
    }

    @Override
    public Builder<?> getBuilder(Class<?> type) {
        return null;
    }
}
