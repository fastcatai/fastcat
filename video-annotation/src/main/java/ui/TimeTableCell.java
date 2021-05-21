package ui;

import annotation.UIAnnotation;
import javafx.event.ActionEvent;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import modelV3.BoundingBoxVideo;

public class TimeTableCell extends TableCell<UIAnnotation<?>, String> {

    private TextField textFieldStart;
    private TextField textFieldEnd;
    private VBox vBox;

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (vBox != null) {
                    textFieldStart.setText(getTimestampStartMillis(getTableRow().getItem()));
                    textFieldEnd.setText(getTimestampEndMillis(getTableRow().getItem()));
                }
                setText(null);
                setGraphic(vBox);
            } else {
                setText(item);
                setGraphic(null);
            }
        }
    }

    @Override
    public void startEdit() {
        if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable())
            return;
        super.startEdit();

        if (isEditing()) {
            if (vBox == null) {
                vBox = createEditableCell(getTableRow().getItem());
            } else {
                textFieldStart.setText(getTimestampStartMillis(getTableRow().getItem()));
                textFieldEnd.setText(getTimestampEndMillis(getTableRow().getItem()));
            }
            setText(null);
            setGraphic(vBox);
            textFieldStart.selectAll();
            textFieldStart.requestFocus();
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        if (getItem() != null) {
            setText(getItem());
            setGraphic(null);
        }
    }

    private String getTimestampStartMillis(UIAnnotation<?> annotation) {
        return Double.toString(((BoundingBoxVideo) annotation.getAnnotationModel()).getTimestampStartMillis());
    }

    private String getTimestampEndMillis(UIAnnotation<?> annotation) {
        return Double.toString(((BoundingBoxVideo) annotation.getAnnotationModel()).getTimestampEndMillis());
    }

    private VBox createEditableCell(UIAnnotation<?> annotation) {
        textFieldStart = new TextField(getTimestampStartMillis(annotation));
        textFieldStart.setOnAction(this::commitEditOnAction);
        textFieldEnd = new TextField(getTimestampEndMillis(annotation));
        textFieldEnd.setOnAction(this::commitEditOnAction);
        return new VBox(textFieldStart, textFieldEnd);
    }

    private void commitEditOnAction(ActionEvent event) {
        double start = Double.parseDouble(textFieldStart.getText().trim());
        double end = Double.parseDouble(textFieldEnd.getText().trim());

        if (end < start)
            return;

        UIAnnotation<?> annotation = getTableRow().getItem();
        ((BoundingBoxVideo) annotation.getAnnotationModel()).setTimestampStart(start);
        ((BoundingBoxVideo) annotation.getAnnotationModel()).setTimestampEnd(end);
        commitEdit(((BoundingBoxVideo) annotation.getAnnotationModel()).getShortTimestampString());
        event.consume();
    }
}
