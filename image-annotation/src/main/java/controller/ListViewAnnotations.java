package controller;

import annotation.UIAnnotation;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;
import model.ImageData;

public class ListViewAnnotations extends ListView<UIAnnotation<?>>
        implements Callback<ListView<UIAnnotation<?>>, ListCell<UIAnnotation<?>>> {

    private final ImageAnnotationController controller;

    public ListViewAnnotations(@NamedArg("controller") ImageAnnotationController controller) {
        this.controller = controller;
        setEditable(true);
        setCellFactory(this);

        // bind annotation list to current selected image annotation and display annotations
        itemsProperty().bind(Bindings.createObjectBinding(() -> {
            ImageData data = controller.getCurrentSelectedImageData();

            if (controller.getAnnotationPane() != null)
                controller.getAnnotationPane().getChildren().clear();

            if (data != null) {
                for (UIAnnotation<?> annotation : data.getAnnotations()) {
                    annotation.addToPane();
                    annotation.setVisible(true);
                }
                return data.getAnnotations();
            }
            return FXCollections.emptyObservableList();
        }, controller.getListViewImages().getSelectionModel().selectedItemProperty()));

        // select first annotation automatically when list changes
        itemsProperty().addListener(observable -> Platform.runLater(() -> getSelectionModel().selectFirst()));
    }

    @Override
    public ListCell<UIAnnotation<?>> call(ListView<UIAnnotation<?>> param) {
        return new ListCellAnnotation();
    }

    /**
     * Cell style
     */
    private static class ListCellAnnotation extends ListCell<UIAnnotation<?>> {

        private TextField textField;

        @Override
        protected void updateItem(UIAnnotation<?> item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                textProperty().unbind();
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    setText(null);
                    setGraphic(textField);
                } else {
                    StringProperty labelProperty = item.getAnnotationModel().labelProperty();
                    textProperty().bind(Bindings.when(labelProperty.isEmpty()).then(UIAnnotation.NO_LABEL).otherwise(labelProperty));
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
                if (textField == null) {
                    textField = new TextField();
                    textField.setOnKeyReleased(event -> {
                        if (event.getCode() == KeyCode.ENTER) {
                            getItem().getAnnotationModel().setLabel(textField.getText());
                            commitEdit(getItem());
                        } else if (event.getCode() == KeyCode.ESCAPE) {
                            cancelEdit();
                        }
                    });
                }
                textProperty().unbind();
                setText(null);
                setGraphic(textField);

                textField.setText(getItem().getAnnotationModel().getLabel());
                textField.requestFocus();
                textField.selectAll();
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
