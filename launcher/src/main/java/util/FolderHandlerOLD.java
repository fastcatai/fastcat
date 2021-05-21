package util;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

import java.io.File;
import java.util.List;

public class FolderHandlerOLD {
    private File[] images = null;
    private File[] masks = null;
    private int fileIdx = 0;
    private long fileCount = 0;
    private final OpenCVPolyApprox polygonDetector;

    private ObjectProperty<Double> epsilon;
    private ObjectProperty<Boolean> showMask;

    /**
     * Handles the browsing of images, masks and the polygon detection.
     * @param epsilonProperty value property of the epsilon spinner.
     */
    public FolderHandlerOLD(ObjectProperty<Double> epsilonProperty) {
        this.epsilon = epsilonProperty;
        this.showMask = new SimpleObjectProperty<>();
        this.polygonDetector = new OpenCVPolyApprox();
    }

    /**
     * Loads a folder with sub-folders named images and masks
     * @param imageFolder the selected image folder
     * @param maskFolder the selected mask folder
     * @return the first image or mask in the folder dependent on the check box or <code>null</code> if sub-folder
     * contain different count of files.
     */
    public Image loadFolder(File imageFolder, File maskFolder) {
        images = null;
        masks = null;
        fileIdx = 0;

        if (imageFolder.exists() && maskFolder.exists()) {
            images = imageFolder.listFiles();
            masks = maskFolder.listFiles();

            if (imageFolder.length() == maskFolder.length())
                fileCount = imageFolder.length();
            else return null;

            if (images != null && masks != null)
                this.loadForDetection();
        }
        return this.loadImageForDisplay();
    }
//
//    public Image loadFolder(File imageFolder) {
//        images = null;
//        masks = null;
//        fileIdx = 0;
//        if (imageFolder.exists()) {
//            images = imageFolder.listFiles();
//            fileCount = imageFolder.length();
//        }
//        return this.loadImageForDisplay();
//    }

    public Image loadPreviousFile() {
        if (fileIdx > 0) {
            fileIdx--;
            this.loadForDetection();
            return this.loadImageForDisplay();
        }
        return null;
    }

    public Image loadNextFile() {
        if (images != null && masks != null && fileIdx < fileCount - 1) {
            fileIdx++;
            this.loadForDetection();
            return this.loadImageForDisplay();
        }
        return null;
    }

    public Image loadImageForDisplay() {
        Image displayImage;
        if (showMask.get()) displayImage = new Image("file:/" + masks[fileIdx].getPath());
        else displayImage = new Image("file:/" + images[fileIdx].getPath());
        return displayImage;
    }

    private void loadForDetection() {
        String file = masks[fileIdx].getPath();
        polygonDetector.loadImage(file);
        epsilon.setValue(polygonDetector.getEpsilon());
    }

    public List<List<Double>> detectPolygon() {
        return polygonDetector.detect(epsilon.get());
    }

    public ObjectProperty<Boolean> showMaskProperty() {
        return showMask;
    }
}
