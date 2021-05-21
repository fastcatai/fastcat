package videoreview;

import annotation.UIAnnotation;
import boundingbox.SimpleBoundingBoxV3;
import boundingbox.UIBoundingBox;
import controller.FrameModeController;
import controller.VideoReviewController;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import model.ImageData;
import model.LoadedReviewData;
import modelV3.BoundingBox;
import util.Utils;

import java.nio.file.Path;

public class AnnotationPaneVR extends Pane implements EventHandler<MouseEvent> {

    private final FrameModeController frameViewerController;
    private final VideoReviewController vrController;

    private ObjectProperty<ImageData> newAnnotatedImage;

    public AnnotationPaneVR(FrameModeController frameViewerController, VideoReviewController vrController) {
        this.frameViewerController = frameViewerController;
        this.vrController = vrController;

        setOnMousePressed(this);
        setOnMouseMoved(this);
    }

    public ObjectProperty<ImageData> newAnnotatedImageProperty() {
        if (newAnnotatedImage == null)
            newAnnotatedImage = new SimpleObjectProperty<>();
        return newAnnotatedImage;
    }

    @Override
    public void handle(MouseEvent event) {
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {

            // loose focus of text fields
            if (vrController.getTextFieldAnnotationLabel().isFocused()
                    || vrController.getFrameViewerMode().getTextFieldJumpToFrameNumber().isFocused()
                    || vrController.getFrameViewerMode().getTextAreaComment().isFocused()) {
                requestFocus();
            }

            if (!event.isPrimaryButtonDown() || event.getX() < 0 || event.getY() < 0 || event.getX() > getMaxWidth() || event.getY() > getMaxHeight())
                return;

            // lose current annotation selection
            vrController.getListAnnotatedImagesController()
                    .getImageData(frameViewerController.getFrameLoader().getCurrentFrame().getFrameNumber())
                    .ifPresent(ImageData::removeAnnotationSelection);

            TextField labelField = vrController.getTextFieldAnnotationLabel();
            String labelStr = labelField.getText().strip();

            // if no label was defined
            if (labelStr.isEmpty()) {
                labelField.requestFocus();
                return;
            }

            ReviewAnnotationChoice annotationChoice = vrController.getChoiceBoxAnnotations().getSelectionModel().getSelectedItem();
            if (annotationChoice == ReviewAnnotationChoice.BOX_CLICK_DRAG) {
                BoundingBox bb = new BoundingBox(event.getX(), event.getY(), 0, 0, labelStr);
                LoadedReviewData loadedData = vrController.getCurrentlyLoadedData();
                SimpleBoundingBoxV3 uiBox = new SimpleBoundingBoxV3(this, bb, loadedData.getFrameWidth(), loadedData.getFrameHeight());
                uiBox.setOnCreationFinished(this::onCreationFinish);
                uiBox.startCreation(UIBoundingBox.CreationType.CLICK_DRAG);
            }

        } else if (event.getEventType() == MouseEvent.MOUSE_MOVED) {
            if (event.getX() < 0 || event.getY() < 0 || event.getX() > getMaxWidth() || event.getY() > getMaxHeight())
                setCursor(Cursor.DEFAULT);
            else setCursor(Cursor.CROSSHAIR);
        }
    }

    private void onCreationFinish(UIAnnotation<?> annotation) {
        Path loadedImageFile = frameViewerController.getFrameLoader().getCurrentFrame().getFrame();

        int frameNumber = Utils.getFrameNumber(loadedImageFile.getFileName().toString());
        ImageData imageData = vrController.getListAnnotatedImagesController().getImageData(frameNumber)
                .orElseGet(() -> { // if not found then create object, add it to list and return new object
                    LoadedReviewData loadedData = vrController.getCurrentlyLoadedData();
                    // We need to pass the width and height of the frame so that it will not be loaded from disk.
                    ImageData data = new ImageData(loadedImageFile, loadedData.getFrameWidth(), loadedData.getFrameHeight());
                    vrController.getListAnnotatedImagesController().addImageData(data);
                    vrController.getListAnnotatedImagesController().fireImageDataEvent(data);
                    newAnnotatedImageProperty().set(data);
                    return data;
                });
        imageData.addAnnotation(annotation);
    }
}
