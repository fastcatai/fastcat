package ui;

import boundingbox.SimpleBoundingBoxV3;
import boundingbox.UIBoundingBox;
import controller.ImageAnnotationController;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import model.ImageData;
import modelV3.BoundingBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.ImageAnnotationChoice;

public class AnnotationPane extends Pane implements EventHandler<MouseEvent> {
    private static final Logger logger = LogManager.getLogger(AnnotationPane.class);

    private final ImageAnnotationController iaController;

    public AnnotationPane(ImageAnnotationController iaController) {
        this.iaController = iaController;
        setMouseListener(true);
    }

    public void setMouseListener(boolean activate) {
        if (activate) {
            setOnMousePressed(this);
            setOnMouseMoved(this);
        } else {
            setOnMousePressed(null);
            setOnMouseMoved(null);
        }
    }

    @Override
    public void handle(MouseEvent event) {
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            if (!event.isPrimaryButtonDown() || event.getX() < 0 || event.getY() < 0 || event.getX() > getMaxWidth() || event.getY() > getMaxHeight())
                return;

            // lose current annotation selection
            iaController.getListViewAnnotations().getSelectionModel().clearSelection();
            // remove annotation selection
            iaController.getCurrentSelectedImageData().removeAnnotationSelection();
            // get annotation choice
            final ImageAnnotationChoice annotationChoice = iaController.getChoiceBoxAnnotations().getSelectionModel().getSelectedItem();

            if (annotationChoice == ImageAnnotationChoice.BOX_CLICK_DRAG)
                choiceBoxClickAndDrag(event);
            else if (annotationChoice == ImageAnnotationChoice.BOX_CLICK_CLICK)
                choiceBoxClickAndClick(event);
            else throw logger.throwing(new IllegalArgumentException("Unknown choice: " + annotationChoice.toString()));

        } else if (event.getEventType() == MouseEvent.MOUSE_MOVED) {
            if (event.getX() < 0 || event.getY() < 0 || event.getX() > getMaxWidth() || event.getY() > getMaxHeight())
                setCursor(Cursor.DEFAULT);
            else setCursor(Cursor.CROSSHAIR);
        }
    }

    private void choiceBoxClickAndDrag(MouseEvent event) {
        final ImageData selectedImageData = iaController.getCurrentSelectedImageData();
        final String label = iaController.getTextFieldDefaultAnnotationLabel().getText().strip();

        // create bounding box
        BoundingBox bb = new BoundingBox(event.getX(), event.getY(), 0, 0, label);
        SimpleBoundingBoxV3 uiBox = new SimpleBoundingBoxV3(this, bb, selectedImageData.getWidth(), selectedImageData.getHeight());

        uiBox.setOnCreationFinished(uiAnnotation -> {
            selectedImageData.addAnnotation(uiAnnotation);
            if (iaController.getCheckBoxAutoNext().isSelected())
                iaController.getMenuBarController().selectNextImage();
            else iaController.getListViewAnnotations().getSelectionModel().select(uiAnnotation);
        });

        uiBox.startCreation(UIBoundingBox.CreationType.CLICK_DRAG);
    }

    private void choiceBoxClickAndClick(MouseEvent event) {
        final ImageData selectedImageData = iaController.getCurrentSelectedImageData();
        final String label = iaController.getTextFieldDefaultAnnotationLabel().getText().strip();

        // create bounding box
        BoundingBox bb = new BoundingBox(event.getX(), event.getY(), 0, 0, label);
        SimpleBoundingBoxV3 uiBox = new SimpleBoundingBoxV3(this, bb, selectedImageData.getWidth(), selectedImageData.getHeight());

        uiBox.setOnCreationFinished(uiAnnotation -> {
            selectedImageData.addAnnotation(uiAnnotation);
            setOnMousePressed(this);
            if (iaController.getCheckBoxAutoNext().isSelected())
                iaController.getMenuBarController().selectNextImage();
            else iaController.getListViewAnnotations().getSelectionModel().select(uiAnnotation);
        });

        uiBox.setOnCreatedCanceled(uiAnnotation -> setOnMousePressed(this));

        uiBox.startCreation(UIBoundingBox.CreationType.CLICK_CLICK);
    }
}
