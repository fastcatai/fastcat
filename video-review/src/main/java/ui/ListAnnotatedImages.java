package ui;

import annotation.SelectionGroup;
import controller.FrameModeController;
import frameloader.Frame;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import model.ImageData;
import util.ImageDataCreatedListener;
import util.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ListAnnotatedImages implements Callback<ListView<ImageData>, ListCell<ImageData>> {

    @FXML
    private ListView<ImageData> listViewAnnotatedImages;

    private final FrameModeController fmController;

    public ListAnnotatedImages(final FrameModeController fmController) {
        this.fmController = fmController;
    }

    @FXML
    private void initialize() {
        // set new ImageData object when new frame is loaded
        fmController.getFrameLoader().currentFrameProperty().addListener(observable -> {
            Frame frame = fmController.getFrameLoader().getCurrentFrame();
            getCurrentImageDataWrapper().set(frame == null ? null : getImageData(frame.getFrameNumber()).orElse(null));
        });
        // set new ImageData object when a new one is created
        addImageDataListener(imageData -> getCurrentImageDataWrapper().set(imageData));
    }

    @Override
    public ListCell<ImageData> call(ListView<ImageData> param) {
        return new ListCellAnnotatedImage();
    }

    /**
     * Displays frame of the ImageData.
     */
    private final Consumer<FrameModeController> jumpToImageData = fmController -> {
        if (fmController != null && listViewAnnotatedImages.getSelectionModel().getSelectedItem() != null) {
            String filename = listViewAnnotatedImages.getSelectionModel().getSelectedItem().getFilename();
            fmController.getFrameLoader().jumpToFrame(Utils.getFrameNumber(filename));
        }
    };

    @FXML
    private void onAnnotatedImageMouseClicked(MouseEvent event) {
        if (event.getClickCount() == 1 && event.getButton() == MouseButton.PRIMARY)
            jumpToImageData.accept(fmController);
    }

    @FXML
    private void onAnnotatedImageKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER)
            jumpToImageData.accept(fmController);
    }

    // Mapping frame number -> object, for fast access
    private final Map<Integer, ImageData> mappingNumberToData = new HashMap<>();

    public Optional<ImageData> getImageData(int frameNumber) {
        return Optional.ofNullable(mappingNumberToData.get(frameNumber));
    }

    //    private final ListProperty<ImageData> imageDataList = new SimpleListProperty<>(ImageDataMap.getImageDataList());
    private ObservableList<ImageData> imageDataList;
    private ObservableList<ImageData> unmodifiableImageDataList;

    public ObservableList<ImageData> getImageDataList() {
        if (imageDataList == null) {
            imageDataList = FXCollections.observableArrayList();
            imageDataList.addListener(imageDataListChangeListener);
            unmodifiableImageDataList = FXCollections.unmodifiableObservableList(imageDataList);
        }
        return unmodifiableImageDataList;
    }

    /**
     * Remove image data when no annotations exist
     */
    private final ChangeListener<Boolean> emptyAnnotationListListener = (observable, oldValue, newValue) -> {
        if (newValue) {
            ImageData imageData = (ImageData) ((ListProperty<?>) ((ReadOnlyBooleanProperty) observable).getBean()).getBean();
            removeImageData(imageData);
        }
    };

    public void addImageData(ImageData imageData) {
        int frameNumber = Utils.getFrameNumber(imageData.getFilename());
        if (!mappingNumberToData.containsKey(frameNumber)) {
            imageData.annotationsListProperty().emptyProperty().addListener(emptyAnnotationListListener);
            imageDataList.add(imageData);
            mappingNumberToData.put(frameNumber, imageData);
            imageDataList.sort(Comparator.comparingInt(i -> Utils.getFrameNumber(i.getFilename())));
        }
    }

    public void addAllImageData(Collection<? extends ImageData> imageDataCollection) {
        for (ImageData imageData : imageDataCollection) {
            addImageData(imageData);
        }
    }

    public void removeImageData(ImageData imageData) {
        imageData.annotationsListProperty().emptyProperty().removeListener(emptyAnnotationListListener);
        imageDataList.remove(imageData);
        mappingNumberToData.remove(Utils.getFrameNumber(imageData.getFilename()), imageData);
    }

    // Used for synchronization with Map.
    private final ListChangeListener<ImageData> imageDataListChangeListener = c -> {
        while (c.next()) {
            if (c.wasRemoved()) {
                for (ImageData imageData : c.getRemoved()) {
                    imageData.annotationsListProperty().emptyProperty().removeListener(emptyAnnotationListListener);
                    mappingNumberToData.remove(Utils.getFrameNumber(imageData.getFilename()), imageData);
                }
            }
            if (c.wasAdded()) {
                for (ImageData imageData : c.getAddedSubList()) {
                    int frameNumber = Utils.getFrameNumber(imageData.getFilename());
                    if (mappingNumberToData.containsKey(frameNumber)) {
                        imageData.annotationsListProperty().emptyProperty().addListener(emptyAnnotationListListener);
                        mappingNumberToData.put(frameNumber, imageData);
                        imageDataList.sort(Comparator.comparingInt(i -> Utils.getFrameNumber(i.getFilename())));
                    }
                }
            }
        }
    };

    // Holds the current ImageData object to the corresponding Frame.
    // Will be filled when Frame changes or a new ImageData object is created and added to the list.
    // See: #initialize()
    private ReadOnlyObjectWrapper<ImageData> currentImageData;

    /**
     * Gets the correntponding ImageData object of a Frame.
     *
     * @return ImageData object
     */
    public ImageData getCurrentImageData() {
        return currentImageDataProperty().get();
    }

    /**
     * Get the wrapper. Used for initialization and internally.
     *
     * @return object property wrapper
     */
    private ReadOnlyObjectWrapper<ImageData> getCurrentImageDataWrapper() {
        if (currentImageData == null)
            currentImageData = new ReadOnlyObjectWrapper<>() {
                @Override
                protected void invalidated() {
                    currentSelectionGroupWrapper().set(get() != null ? get().getSelectionGroup() : null);
                }
            };
        return currentImageData;
    }

    /**
     * ImageData object property.
     *
     * @return object property
     */
    public ReadOnlyObjectProperty<ImageData> currentImageDataProperty() {
        return getCurrentImageDataWrapper().getReadOnlyProperty();
    }

    private ReadOnlyObjectWrapper<SelectionGroup> currentSelectionGroupWrapper;

    public SelectionGroup getCurrentSelectionGroup() {
        return currentSelectionGroupWrapper().get();
    }

    private ReadOnlyObjectWrapper<SelectionGroup> currentSelectionGroupWrapper() {
        if (currentSelectionGroupWrapper == null)
            currentSelectionGroupWrapper = new ReadOnlyObjectWrapper<>();
        return currentSelectionGroupWrapper;
    }

    public ReadOnlyObjectProperty<SelectionGroup> currentSelectionGroupProperty() {
        return currentSelectionGroupWrapper().getReadOnlyProperty();
    }

    // Holds the list of registered listeners
    private final List<ImageDataCreatedListener> imageDataCreatedListeners = new ArrayList<>();

    public void addImageDataListener(ImageDataCreatedListener listener) {
        imageDataCreatedListeners.add(listener);
    }

    public void removeImageDataListener(ImageDataCreatedListener listener) {
        imageDataCreatedListeners.remove(listener);
    }

    /**
     * Fires a ImageData event.
     *
     * @param imageData object that was created and added.
     */
    public void fireImageDataEvent(ImageData imageData) {
        for (ImageDataCreatedListener l : imageDataCreatedListeners) {
            l.onNewImageData(imageData);
        }
    }

    public void clear() {
        imageDataList.clear();
        mappingNumberToData.clear();
    }

    /**
     * List Cell
     */
    public static class ListCellAnnotatedImage extends ListCell<ImageData> {
        @Override
        protected void updateItem(ImageData item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                textProperty().unbind();
                setText(null);
                setGraphic(null);
            } else {
                textProperty().bind(Bindings.createStringBinding(() -> {
                    String text = getItem().getFilename();
                    if (getItem().getAnnotations().size() > 0) text += " (" + getItem().getAnnotations().size() + ")";
                    return text;
                }, getItem().getAnnotations()));
                setGraphic(null);
            }
        }
    }
}
