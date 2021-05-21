package listviewcells;

import annotation.UIAnnotation;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;

public class ListCellAnnotation extends ListCell<UIAnnotation<?>>
        implements Callback<ListView<UIAnnotation<?>>, ListCell<UIAnnotation<?>>> {

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
    public ListCell<UIAnnotation<?>> call(ListView<UIAnnotation<?>> param) {
        return new ListCellAnnotation();
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
