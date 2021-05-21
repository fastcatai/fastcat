package tracking;

import boundingbox.SimpleBoundingBoxV3;
import javafx.scene.shape.Rectangle;
import modelV3.BoundingBox;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.Rect2d;
import org.opencv.tracking.Tracker;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Optional;


public class OpenCVTracker {

    private Tracker tracker;

    public OpenCVTracker(Class<? extends Tracker> trackerClass) {
        try {
            tracker = (Tracker) trackerClass.getDeclaredMethod("create").invoke(null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public boolean init(Path image, SimpleBoundingBoxV3 initBox) {
        BoundingBox initBoundingBox = initBox.getAnnotationModel();
        return init(image.toString(), initBoundingBox.getX(), initBoundingBox.getY(), initBoundingBox.getWidth(), initBoundingBox.getHeight());
    }

    public boolean init(String imagePath, SimpleBoundingBoxV3 initBox) {
        BoundingBox initBoundingBox = initBox.getAnnotationModel();
        return init(imagePath, initBoundingBox.getX(), initBoundingBox.getY(), initBoundingBox.getWidth(), initBoundingBox.getHeight());
    }

    public boolean init(Path image, double x, double y, double width, double height) {
        return init(image.toString(), x, y, width, height);
    }

    public boolean init(String imagePath, double x, double y, double width, double height) {
        if (width > 1 || height > 1) {
            Mat img = Imgcodecs.imread(imagePath);
            Rect2d rect = new Rect2d(x, y, width, height);
            return tracker.init(img, rect);
        } else {
            throw new RuntimeException(String.format("Bounding box is to small. All dimensions must be bigger than 1. (width=%f, height=%f)", width, height));
        }
    }

    public Optional<Rectangle> update(Path image) {
        return update(image.toString());
    }

    public Optional<Rectangle> update(String imagePath) {
        if (tracker == null)
            return Optional.empty();

        Mat img = Imgcodecs.imread(imagePath);
        Rect2d box = new Rect2d();
        if (tracker.update(img, box)) {
            return Optional.of(new Rectangle(box.x, box.y, box.width, box.height));
        } else return Optional.empty();
    }
}
