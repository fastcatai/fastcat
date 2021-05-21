package ui;

import annotation.UIAnnotation;
import controller.FrameModeController;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;
import model.ImageData;

public class ListViewAnnotations implements Callback<ListView<UIAnnotation<?>>, ListCell<UIAnnotation<?>>> {

    @FXML
    private ListView<UIAnnotation<?>> listViewAnnotations;

    private final FrameModeController fmController;

    public ListViewAnnotations(FrameModeController fmController) {
        this.fmController = fmController;
    }

    @FXML
    private void initialize() {
        // list view annotations
//        listViewAnnotations.getSelectionModel().selectedItemProperty().addListener((observable, oldItem, newItem) -> {
//            if (newItem != null) fmController.setCurrentSelectedAnnotation(newItem);
//        });

        // binds dynamically the annotations from the current displayed frame,
        // if image changes or a new ImageData object was added to listViewAnnotatedImages
        listViewAnnotations.itemsProperty().bind(Bindings.createObjectBinding(() -> {
            // check if trigger came from new ImageData
            ImageData newImageData = fmController.getAnnotationPane().newAnnotatedImageProperty().get();
            if (newImageData != null) {
                fmController.getAnnotationPane().newAnnotatedImageProperty().set(null); // reset
                return newImageData.getAnnotations();
            }
            // trigger came from frame viewer => get search ImageData from display frame
            int frameNumber;
            try { // method getFrameLoader or getCurrentFrame could be null
                frameNumber = fmController.getFrameLoader().getCurrentFrame().getFrameNumber();
            } catch (NullPointerException e) {
                return FXCollections.emptyObservableList();
            }

            return fmController.getVrController().getListAnnotatedImagesController()
                    .getImageData(frameNumber)
                    .map(ImageData::getAnnotations)
                    .orElse(FXCollections.emptyObservableList());

        }, fmController.getFrameViewer().imageProperty(), fmController.getAnnotationPane().newAnnotatedImageProperty()));
    }

    @Override
    public ListCell<UIAnnotation<?>> call(ListView<UIAnnotation<?>> param) {
        return new ListCellAnnotation();
    }

    /**
     * List Cell
     */
    public static class ListCellAnnotation extends ListCell<UIAnnotation<?>> {

        private TextField textFieldLabel;

        @Override
        protected void updateItem(UIAnnotation item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                textProperty().unbind();
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    textProperty().unbind();
                    setText(null);
                    setGraphic(textFieldLabel);
                } else {
                    textProperty().bind(item.getAnnotationModel().labelProperty());
                    setGraphic(null);
                }
            }
        }

        @Override
        public void startEdit() {
            if (!isEditable() || !getListView().isEditable())
                return;
            super.startEdit();

            if (isEditing()) {
                if (textFieldLabel == null) {
                    textFieldLabel = new TextField();
                    textFieldLabel.setPrefWidth(110);
                    textFieldLabel.setOnKeyReleased(event -> {
                        if (event.getCode() == KeyCode.ENTER) {
                            getItem().getAnnotationModel().setLabel(textFieldLabel.getText().trim());
                            commitEdit(getItem());
                            if (getListView().getSelectionModel().getSelectedItem() == null)
                                getListView().getSelectionModel().select(getItem());

                        } else if (event.getCode() == KeyCode.ESCAPE) {
                            cancelEdit();
                        }
                    });
                }
                textProperty().unbind();
                setText(null);
                setGraphic(textFieldLabel);

                textFieldLabel.setText(getItem().getAnnotationModel().getLabel());
                textFieldLabel.selectAll();
                textFieldLabel.requestFocus();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            if (getItem() != null) {
                textProperty().unbind();
                textProperty().bind(getItem().getAnnotationModel().labelProperty());
                setGraphic(null);
            }
        }
    }
}
