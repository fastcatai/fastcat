package tracking;

import boundingbox.SimpleBoundingBoxV3;
import events.AnnotationChange;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import model.ImageData;
import modelV3.BoundingBox;
import org.opencv.tracking.Tracker;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class TrackingHandler {

    private OpenCVTracker openCVTracker;
    private final ObjectProperty<SimpleBoundingBoxV3> latestBoundingBox;
    private final AtomicBoolean wasLatestBoxChanged;

    private String initLabel = "";
    private String initInstance = "";
    private final Pane annotationPane;

    public TrackingHandler(Pane annotationPane) {
        this.annotationPane = annotationPane;

        wasLatestBoxChanged = new AtomicBoolean(false);
        AnnotationChange listener = uiAnnotation -> wasLatestBoxChanged.set(true);

        latestBoundingBox = new SimpleObjectProperty<>();
        latestBoundingBox.addListener((observable, oldBox, newBox) -> {
            wasLatestBoxChanged.set(false);
            if (oldBox != null)
                oldBox.removeAnnotationChangedListener(listener);
            if (newBox != null)
                newBox.addAnnotationChangedListener(listener);
        });
    }

    public boolean initializeTracker(Class<? extends Tracker> selectedTrackerClass, ImageData selectedImage,
                                     SimpleBoundingBoxV3 selectedBoundingBox) {
        if (selectedBoundingBox == null)
            return false;
        BoundingBox initBox = selectedBoundingBox.getAnnotationModel();
        initLabel = initBox.getLabel();
        initInstance = initBox.getInstance();
        openCVTracker = new OpenCVTracker(selectedTrackerClass);
        return openCVTracker.init(selectedImage.getFile(), initBox.getX(), initBox.getY(), initBox.getWidth(), initBox.getHeight());
    }

    public Optional<SimpleBoundingBoxV3> update(ImageData imageData) {
        Optional<Rectangle> box = openCVTracker.update(imageData.getFile());
        if (box.isPresent()) {
            Rectangle rect = box.get();
            BoundingBox boundingBox = new BoundingBox(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), initLabel, initInstance, false, true);
            SimpleBoundingBoxV3 uiBoundingBox = new SimpleBoundingBoxV3(annotationPane, boundingBox, true, imageData.getWidth(), imageData.getHeight());
            uiBoundingBox.imported();

            latestBoundingBox.set(uiBoundingBox);
            return Optional.of(uiBoundingBox);
        }
        latestBoundingBox.set(null);
        return Optional.empty();
    }

    public boolean wasLatestBoxChanged() {
        if (latestBoundingBox == null || latestBoundingBox.get() == null)
            return false;
        return wasLatestBoxChanged.get();
    }
}
