package ui;

import annotation.UIAnnotation;
import controller.FrameModeController;
import controller.VideoReviewController;
import frameloader.FrameLoaderBase;
import frameloader.QueuedFrameLoader;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import keymap.Program;
import keymap.manager.SMFactory;
import keymap.manager.VRShortcutManager;
import keymap.model.ActionVR;
import keymap.model.ReadOnlyKeyActionClass;
import model.FrameData;
import model.ImageData;
import model.SuperframeData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class ListViewClasses implements Callback<ListView<ReadOnlyKeyActionClass>, ListCell<ReadOnlyKeyActionClass>> {
    private static final Logger logger = LogManager.getLogger(ListViewClasses.class);

    @FXML
    public ListView<ReadOnlyKeyActionClass> listViewClasses;

    private final VideoReviewController vrController;

    public ListViewClasses(FrameModeController fmController) {
        vrController = fmController.getVrController();

        VRShortcutManager sm = ((VRShortcutManager) SMFactory.getShortcutManager(Program.VIDEO_REVIEW));
        // put already available class into list
        ObservableMap<String, ReadOnlyKeyActionClass> classMap = sm.getClassActionMappings();
        for (String c : classMap.keySet())
            getClassList().add(classMap.get(c));
        // sync with list
        classMap.addListener((MapChangeListener<String, ReadOnlyKeyActionClass>) change -> {
            if (change.wasRemoved()) getClassList().remove(change.getValueRemoved());
            if (change.wasAdded()) getClassList().add(change.getValueAdded());
        });
    }

    @Override
    public ListCell<ReadOnlyKeyActionClass> call(ListView<ReadOnlyKeyActionClass> param) {
        return new ClassCell();
    }

    @FXML
    private void initialize() {
        // none
    }

    private ListProperty<ReadOnlyKeyActionClass> classList;

    public ObservableList<ReadOnlyKeyActionClass> getClassList() {
        return classListProperty().get();
    }

    public ReadOnlyListProperty<ReadOnlyKeyActionClass> classListProperty() {
        if (classList == null)
            classList = new SimpleListProperty<>(FXCollections.observableArrayList());
        return classList;
    }

    @FXML
    private void onClassClicked(MouseEvent event) {
        if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
            String className = listViewClasses.getSelectionModel().getSelectedItem().getActionName();
            addClass(className, false);
        }
    }

    /**
     * Adds a class to to frame of a loaded freeze frame.
     *
     * @param theClass      must be from type <code>String</code>
     * @param isControlDown invert auto next selection, if Ctrl modifier is pressed down
     */
    public void addClass(String theClass, boolean isControlDown) {
        FrameModeController fmController = vrController.getFrameViewerMode();
        FrameLoaderBase frameLoader = fmController.getFrameLoader();

        if (frameLoader instanceof QueuedFrameLoader && ((QueuedFrameLoader) frameLoader).checkDelay())
            return;

        ImageData imageData = vrController.getListAnnotatedImagesController().getCurrentImageData();
        UIAnnotation<?> selectedAnnotation = null;
        if (imageData != null)
            selectedAnnotation= imageData.selectedAnnotationProperty().get();

        // add class to annotation
        if (selectedAnnotation != null) {
            // add class label to annotation if not in list, otherwise remove
            if (selectedAnnotation.getAnnotationModel().getAdditionalLabels().contains(theClass))
                selectedAnnotation.getAnnotationModel().removeAdditionalLabel(theClass);
            else selectedAnnotation.getAnnotationModel().addAdditionalLabel(theClass);

        } else { // add class to frame
            boolean frameDataWasCreated = false;
            FrameData frameData = null;

            SuperframeData currentSuperframe = vrController.getCurrentSuperframeData();
            if (currentSuperframe != null) {
                int frameNumber = frameLoader.getCurrentFrame().getFrameNumber();
                Optional<FrameData> optFrameData = currentSuperframe.getFrame(frameNumber);
                frameDataWasCreated = optFrameData.isEmpty();
                frameData = optFrameData.orElseGet(() -> {
                    FrameData fd = new FrameData(currentSuperframe, frameNumber);
                    currentSuperframe.addChildFrame(fd);
                    return fd;
                });
                frameData.toggleClass(theClass);
            }

            // next frame
            boolean isAutoNextSelected = fmController.isCheckBoxAutoNextSelected();
            if ((isAutoNextSelected && !isControlDown) || (!isAutoNextSelected && isControlDown)) {
                if (fmController.isCheckBoxJumpSuperframesSelected())
                    vrController.getMenuBarController().fireAction(ActionVR.SKIP_SUPERFRAME_FORWARD_SINGLE);
                else frameLoader.nextFrame();

            } else { // if not switching to next frame then fire event if frame data was newly created
                if (frameData != null && frameDataWasCreated)
                    vrController.getListViewFrameDataController().fireFrameDataEvent(frameData);
            }

            vrController.getListViewFrameData().getSelectionModel().select(frameData);
            vrController.getListViewFrameData().scrollTo(vrController.getListViewFrameData().getSelectionModel().getSelectedIndex());
        }
    }

    /**
     * List Cell
     */
    private static class ClassCell extends ListCell<ReadOnlyKeyActionClass> {
        @Override
        protected void updateItem(ReadOnlyKeyActionClass item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(null);
                setGraphic(createCell(item));
            }
        }

        private HBox createCell(ReadOnlyKeyActionClass item) {
            HBox labelCell = new HBox(new Label(item.getActionName()));
            HBox.setHgrow(labelCell, Priority.ALWAYS);

            Label labelShortcut = new Label(item.getKeyCombination().getDisplayText());
            labelShortcut.setStyle("-fx-border-style: dotted;");
            HBox shortcuts = new HBox(5, labelShortcut);
            if (item.getInversionKeyCombination().isPresent()) {
                Label labelShortcutInv = new Label(item.getInversionKeyCombination().get().getDisplayText());
                labelShortcutInv.setStyle("-fx-border-style: dotted;");
                shortcuts.getChildren().add(labelShortcutInv);
            }

            HBox cell = new HBox(labelCell, shortcuts);
            cell.setAlignment(Pos.CENTER_LEFT);
            return cell;
        }
    }
}
