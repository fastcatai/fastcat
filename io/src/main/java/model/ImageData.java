package model;

import annotation.SelectionGroup;
import annotation.UIAnnotation;
import classification.ImageClassification;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public class ImageData {
    private static final Logger logger = LogManager.getLogger(ImageData.class);

    private final Path file;
    private final String filename;
    private int width;
    private int height;

    public ImageData(Path file) {
        this(file, -1, -1);
    }

    public ImageData(Path file, int width, int height) {
        this.file = file;
        this.width = width;
        this.height = height;
        filename = file.getFileName().toString();
        annotationCount.bind(Bindings.size(annotations));
    }

    public Path getFile() {
        return file;
    }

    public String getFilename() {
        return filename;
    }

    public int getWidth() {
        if (width == -1) readImageSize();
        return width;
    }

    public int getHeight() {
        if (height == -1) readImageSize();
        return height;
    }

    public boolean isEmpty() {
        return getImageClassification().isEmpty() && annotations.isEmpty();
    }

    private ImageClassification imageClassification;

    public ImageClassification getImageClassification() {
        if (imageClassification == null)
            imageClassification = new ImageClassification();
        return imageClassification;
    }

    private final IntegerProperty annotationCount = new SimpleIntegerProperty();

    public ReadOnlyIntegerProperty annotationCountProperty() {
        return annotationCount;
    }

    private final ListProperty<UIAnnotation<?>> annotations = new SimpleListProperty<>(this, "imageData",
            FXCollections.observableArrayList());

    public ObservableList<UIAnnotation<?>> getAnnotations() {
        return annotations;
    }

    public void addAnnotation(UIAnnotation<?> annotation) {
        annotations.add(annotation);
        getSelectionGroup().getAnnotations().add(annotation);
    }

    public void removeAnnotation(UIAnnotation<?> annotation) {
        annotations.remove(annotation);
        getSelectionGroup().getAnnotations().remove(annotation);
    }

    public ReadOnlyListProperty<UIAnnotation<?>> annotationsListProperty() {
        return annotations;
    }

    private SelectionGroup selectionGroup;

    public SelectionGroup getSelectionGroup() {
        if (selectionGroup == null)
            selectionGroup = new SelectionGroup();
        return selectionGroup;
    }

    public void removeAnnotationSelection() {
        getSelectionGroup().selectAnnotation(null);
    }

    public ReadOnlyObjectProperty<UIAnnotation<?>> selectedAnnotationProperty() {
        return getSelectionGroup().selectedAnnotationProperty();
    }

    private void readImageSize() {
        try (ImageInputStream in = ImageIO.createImageInputStream(Files.newInputStream(file))) {
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(in);
                    width = reader.getWidth(0);
                    height = reader.getHeight(0);
                } finally {
                    reader.dispose();
                }
            } else {
                throw logger.throwing(new RuntimeException("Could not read image dimensions: " +
                        file.toAbsolutePath().normalize().toString()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
