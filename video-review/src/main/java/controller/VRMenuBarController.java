package controller;

import about.AboutStage;
import annotation.UIAnnotation;
import com.fasterxml.jackson.databind.ObjectMapper;
import events.ViewEvent;
import i18n.I18N;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;
import json.VRJsonModule;
import jsonsaver.JsonSaver;
import keymap.Program;
import keymap.manager.SMFactory;
import keymap.manager.VRShortcutManager;
import keymap.model.ActionVR;
import model.FrameData;
import model.ImageData;
import model.LoadedReviewData;
import model.SuperframeData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import settings.SettingsProperties;
import settings.SettingsStage;
import ui.Dialogs;
import util.SystemProperties;
import videoreview.ViewMode;
import wrapper.VRJsonWrapper;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class VRMenuBarController {
    private static final Logger logger = LogManager.getLogger(VRMenuBarController.class);
    private final VideoReviewController vrController;

    @FXML
    private MenuItem menuItemLoadVideo;
    @FXML
    private MenuItem menuItemSave;
    @FXML
    private MenuItem menuItemAddStartTag;
    @FXML
    private MenuItem menuItemAddEndTag;
    @FXML
    private MenuItem menuItemDeleteAnnotation;
    @FXML
    private MenuItem menuItemSkipImageForwardSingle;
    @FXML
    private MenuItem menuItemSkipImageBackwardsSingle;
    @FXML
    private MenuItem menuItemSkipImageForwardDouble;
    @FXML
    private MenuItem menuItemSkipImageBackwardsDouble;
    @FXML
    private MenuItem menuItemSkipImageForwardTriple;
    @FXML
    private MenuItem menuItemSkipImageBackwardsTriple;
    @FXML
    private MenuItem menuItemSkipSuperframeOneForward;
    @FXML
    private MenuItem menuItemSkipSuperframeOneBackwards;

    public VRMenuBarController(VideoReviewController vrController) {
        this.vrController = vrController;
    }

    @FXML
    private void initialize() {
        VRShortcutManager shortcutManager = (VRShortcutManager) SMFactory.getShortcutManager(Program.VIDEO_REVIEW);
        bindMenuItems(shortcutManager);

        menuItemAddStartTag.disableProperty().bind(vrController.defaultAnnotationLabelProperty().isEmpty());
        menuItemAddEndTag.disableProperty().bind(vrController.defaultAnnotationLabelProperty().isEmpty());

        menuItemAddStartTag.textProperty().bind(Bindings.createStringBinding(() -> {
            String defaultLabel = vrController.getDefaultAnnotationLabel();
            return MessageFormat.format(I18N.getString("vr.menu.addStartTag"),
                    defaultLabel != null ? defaultLabel.strip() : "");
        }, vrController.defaultAnnotationLabelProperty()));

        menuItemAddEndTag.textProperty().bind(Bindings.createStringBinding(() -> {
            String defaultLabel = vrController.getDefaultAnnotationLabel();
            return MessageFormat.format(I18N.getString("vr.menu.addEndTag"),
                    defaultLabel != null ? defaultLabel.strip() : "");
        }, vrController.defaultAnnotationLabelProperty()));
    }

    public void bindMenuItems(VRShortcutManager sm) {
        menuItemLoadVideo.acceleratorProperty().bind(sm.getKeyAction(ActionVR.OPEN_LOAD_VIDEO_DIALOG).keyCombinationProperty());
        menuItemSave.acceleratorProperty().bind(sm.getKeyAction(ActionVR.SAVE_DIALOG).keyCombinationProperty());
        menuItemAddStartTag.acceleratorProperty().bind(sm.getKeyAction(ActionVR.ADD_START_TAG).keyCombinationProperty());
        menuItemAddEndTag.acceleratorProperty().bind(sm.getKeyAction(ActionVR.ADD_END_TAG).keyCombinationProperty());
        menuItemDeleteAnnotation.acceleratorProperty().bind(sm.getKeyAction(ActionVR.DELETE_ANNOTATION).keyCombinationProperty());
        menuItemSkipImageForwardSingle.acceleratorProperty().bind(sm.getKeyAction(ActionVR.SKIP_IMAGE_FORWARD_SINGLE).keyCombinationProperty());
        menuItemSkipImageBackwardsSingle.acceleratorProperty().bind(sm.getKeyAction(ActionVR.SKIP_IMAGE_BACKWARDS_SINGLE).keyCombinationProperty());
        menuItemSkipImageForwardDouble.acceleratorProperty().bind(sm.getKeyAction(ActionVR.SKIP_IMAGE_FORWARD_DOUBLE).keyCombinationProperty());
        menuItemSkipImageBackwardsDouble.acceleratorProperty().bind(sm.getKeyAction(ActionVR.SKIP_IMAGE_BACKWARDS_DOUBLE).keyCombinationProperty());
        menuItemSkipImageForwardTriple.acceleratorProperty().bind(sm.getKeyAction(ActionVR.SKIP_IMAGE_FORWARD_TRIPLE).keyCombinationProperty());
        menuItemSkipImageBackwardsTriple.acceleratorProperty().bind(sm.getKeyAction(ActionVR.SKIP_IMAGE_BACKWARDS_TRIPLE).keyCombinationProperty());
        menuItemSkipSuperframeOneForward.acceleratorProperty().bind(sm.getKeyAction(ActionVR.SKIP_SUPERFRAME_FORWARD_SINGLE).keyCombinationProperty());
        menuItemSkipSuperframeOneBackwards.acceleratorProperty().bind(sm.getKeyAction(ActionVR.SKIP_SUPERFRAME_BACKWARDS_SINGLE).keyCombinationProperty());
    }

    public void fireAction(ActionVR action) {
        if (action == null) return;
        if (action == ActionVR.OPEN_LOAD_VIDEO_DIALOG) menuItemLoadVideo.fire();
        else if (action == ActionVR.SAVE_DIALOG) menuItemSave.fire();
        else if (action == ActionVR.ADD_START_TAG) menuItemAddStartTag.fire();
        else if (action == ActionVR.ADD_END_TAG) menuItemAddEndTag.fire();
        else if (action == ActionVR.DELETE_ANNOTATION) menuItemDeleteAnnotation.fire();
        else if (action == ActionVR.SKIP_IMAGE_FORWARD_SINGLE) menuItemSkipImageForwardSingle.fire();
        else if (action == ActionVR.SKIP_IMAGE_BACKWARDS_SINGLE) menuItemSkipImageBackwardsSingle.fire();
        else if (action == ActionVR.SKIP_IMAGE_FORWARD_DOUBLE) menuItemSkipImageForwardDouble.fire();
        else if (action == ActionVR.SKIP_IMAGE_BACKWARDS_DOUBLE) menuItemSkipImageBackwardsDouble.fire();
        else if (action == ActionVR.SKIP_IMAGE_FORWARD_TRIPLE) menuItemSkipImageForwardTriple.fire();
        else if (action == ActionVR.SKIP_IMAGE_BACKWARDS_TRIPLE) menuItemSkipImageBackwardsTriple.fire();
        else if (action == ActionVR.SKIP_SUPERFRAME_FORWARD_SINGLE) menuItemSkipSuperframeOneForward.fire();
        else if (action == ActionVR.SKIP_SUPERFRAME_BACKWARDS_SINGLE) menuItemSkipSuperframeOneBackwards.fire();
        else {
            throw logger.throwing(new IllegalArgumentException(String.format("Action '%s' is not available in menu bar",
                    action.getActionName())));
        }
    }

    @FXML
    private void loadVideo() {
        Optional<LoadedReviewData> loadedData = LoadingVideoDialog.showAndWait(vrController);
        loadedData.ifPresent(vrController.currentlyLoadedDataProperty()::set);
    }

    @FXML
    private void save() {
        if (vrController.getCurrentlyLoadedData() == null
                || vrController.getListViewSuperframes().getItems() == null
                || vrController.getListViewSuperframes().getItems().isEmpty()) {
            Dialogs.showInformation("Save", null, "No data is loaded to be saved.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Results");

        // set init directory
        chooser.setInitialDirectory(vrController.getCurrentlyLoadedData().getVideoFile().getParent().toFile());

        // set init file name and extensions
        String datetime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        chooser.setInitialFileName(String.format("video(%s)%s", datetime, JsonSaver.VR_FILE_EXTENSION));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Review JSON files", "*.review.json"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files", "*.json"));

        File jsonFile = chooser.showSaveDialog(menuItemSave.getParentPopup().getOwnerWindow());
        if (jsonFile != null) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new VRJsonModule());
            VRJsonWrapper jsonWrapper = new VRJsonWrapper(vrController.getListViewSuperframes().getItems(),
                    vrController.getListViewAnnotatedImages().getItems(),
                    vrController.getCurrentlyLoadedData());

            try {
                mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, jsonWrapper);
            } catch (IOException e) {
                logger.error(e);
                Dialogs.showError("Save Error", "Data could not be saved!", e.getMessage());
            }
        }
    }

    @FXML
    private void deleteAnnotation() {
        ImageData imageData = vrController.getListAnnotatedImagesController().getCurrentImageData();
        if (imageData == null)
            return;
        UIAnnotation<?> selectedAnnotation = imageData.selectedAnnotationProperty().get();
        if (selectedAnnotation == null)
            return;
        selectedAnnotation.removeFromPane();
        imageData.removeAnnotation(selectedAnnotation);

    }

    //<editor-fold desc="Skip Image Single">
    /*
    Scene's key event filter is involved for a single forward/backwards skip.
    Order of execution:
    1. Scene: Key Pressed (ignored, is handled by menu validation)
    2. Menu Item: Menu Validation (when key accelerator is triggered)
    3. Menu Item: On Action
    4. Scene: Key Released
     */

    // flags to detect continuous press (frame player)
    private int consecutiveFrameCalls = 0;
    private final int numberOfConsecutiveFrameCallsUntilSpeedMode = Integer.parseInt(SettingsProperties.getProperty(
            SettingsProperties.VIDEO_REVIEW_DISPLAY_FRAME_PLAYER_DELAY));

    @FXML
    private void onSkipImageForwardSingleMenuValidation() {
        // if key is pressed over multiple frames then the view changes
        consecutiveFrameCalls++;
        if (consecutiveFrameCalls == numberOfConsecutiveFrameCallsUntilSpeedMode + 1)
            vrController.getFrameViewerMode().setCurrentFrameViewerState(FrameModeController.FrameViewerStates.FRAME_PLAYER);
    }

    @FXML
    private void onSkipImageForwardSingle() {
        vrController.getFrameViewerMode().getFrameLoader().nextFrame();
    }

    /**
     * Key released method triggered by Scene.
     */
    public void onSkipImageForwardSingleReleased() {
        // reset view if key is released
        consecutiveFrameCalls = 0;
        vrController.getFrameViewerMode().setCurrentFrameViewerState(FrameModeController.FrameViewerStates.DEFAULT);
    }

    @FXML
    private void onSkipImageBackwardsSingleMenuValidation() {
        // if key is pressed over multiple frames then the view changes
        consecutiveFrameCalls++;
        if (consecutiveFrameCalls == numberOfConsecutiveFrameCallsUntilSpeedMode)
            vrController.getFrameViewerMode().setCurrentFrameViewerState(FrameModeController.FrameViewerStates.FRAME_PLAYER);
    }

    @FXML
    private void onSkipImageBackwardsSingle() {
        vrController.getFrameViewerMode().getFrameLoader().previousFrame();
    }

    /**
     * Key released method triggered by Scene.
     */
    public void onSkipImageBackwardsSingleReleased() {
        // reset view if key is released
        consecutiveFrameCalls = 0;
        vrController.getFrameViewerMode().setCurrentFrameViewerState(FrameModeController.FrameViewerStates.DEFAULT);
    }
    //</editor-fold>

    @FXML
    private void onSkipImageForwardDouble() {
        vrController.getFrameViewerMode().getFrameLoader().skipFrames(10);
    }

    @FXML
    private void onSkipImageBackwardsDouble() {
        vrController.getFrameViewerMode().getFrameLoader().skipFrames(-10);
    }

    @FXML
    private void onSkipImageForwardTriple() {
        vrController.getFrameViewerMode().getFrameLoader().skipFrames(100);
    }

    @FXML
    private void onSkipImageBackwardsTriple() {
        vrController.getFrameViewerMode().getFrameLoader().skipFrames(-100);
    }

    @FXML
    private void onSkipSuperframeOneForward() {
        vrController.getListViewSuperframes().getSelectionModel().select(vrController.getCurrentSuperframeData());
        vrController.getListViewSuperframes().getSelectionModel().selectNext();
        SuperframeData superframe = vrController.getCurrentlySelectedSuperframe();
        vrController.currentSuperframeDataProperty().set(superframe);
        vrController.getFrameViewerMode().getFrameLoader().jumpToFrame(superframe.getFrameNumber());
        vrController.setCurrentViewMode(ViewMode.FRAMES);
    }

    @FXML
    private void onSkipSuperframeOneBackwards() {
        vrController.getListViewSuperframes().getSelectionModel().select(vrController.getCurrentSuperframeData());
        vrController.getListViewSuperframes().getSelectionModel().selectPrevious();
        SuperframeData superframe = vrController.getCurrentlySelectedSuperframe();
        vrController.currentSuperframeDataProperty().set(superframe);
        vrController.getFrameViewerMode().getFrameLoader().jumpToFrame(superframe.getFrameNumber());
        vrController.setCurrentViewMode(ViewMode.FRAMES);
    }

    @FXML
    private void addStartTag() {
        addTag("#START");
    }

    @FXML
    private void addEndTag() {
        addTag("#END");
    }

    private void addTag(final String tag) {
        // label text field must not be empty
        String defaultLabel = vrController.getTextFieldAnnotationLabel().getText();
        if (defaultLabel == null || defaultLabel.isBlank())
            return;
        // concat class string
        String taggedClass = defaultLabel + tag;

        // get ImageData object of frames
        ImageData imageData = vrController.getListAnnotatedImagesController().getCurrentImageData();
        UIAnnotation<?> annotationUI = null;
        if (imageData != null)
            annotationUI = imageData.selectedAnnotationProperty().get();

        // if annotation selected then add class to annotation, otherwise to frame
        if (annotationUI != null) {
            if (annotationUI.getAnnotationModel().getAdditionalLabels().contains(taggedClass))
                annotationUI.getAnnotationModel().removeAdditionalLabel(taggedClass);
            else annotationUI.getAnnotationModel().addAdditionalLabel(taggedClass);

        } else {
            SuperframeData currentSuperframe = vrController.getCurrentSuperframeData();
            if (currentSuperframe != null) {
                int frameNumber = vrController.getFrameViewerMode().getFrameLoader().getCurrentFrame().getFrameNumber();
                FrameData frameData = currentSuperframe.getFrame(frameNumber).orElseGet(() -> {
                    FrameData fd = new FrameData(currentSuperframe, frameNumber);
                    currentSuperframe.addChildFrame(fd);
                    vrController.getListViewFrameDataController().fireFrameDataEvent(fd);
                    return fd;
                });
                frameData.toggleClass(taggedClass);

                vrController.getListViewFrameData().getSelectionModel().select(frameData);
                vrController.getListViewFrameData().scrollTo(vrController.getListViewFrameData().getSelectionModel().getSelectedIndex());
            }
        }
    }

    @FXML
    private void openSettings() {
        new SettingsStage().show();
    }

    @FXML
    private void backToMenu() {
        vrController.getVideoReviewView().getStage().fireEvent(new ViewEvent(
                vrController.getVideoReviewView().getStage(), ViewEvent.MENU));
    }

    @FXML
    private void exitProgram() {
        vrController.getVideoReviewView().getStage().fireEvent(new WindowEvent(
                vrController.getVideoReviewView().getStage(), WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    @FXML
    private void openAbout() {
        AboutStage.show(vrController.getVideoReviewView().getApplication());
    }

    @FXML
    private void openLogFolder() {
        try {
            Desktop.getDesktop().open(new File(System.getProperty(SystemProperties.VR_LOG_PATH)));
        } catch (IOException | IllegalArgumentException e) {
            Dialogs.showError("Error", null, e.getMessage());
        }
    }

    public MenuItem getMenuItemDeleteAnnotation() {
        return menuItemDeleteAnnotation;
    }
}
