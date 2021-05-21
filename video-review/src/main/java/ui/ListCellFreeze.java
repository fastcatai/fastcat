package ui;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.util.Callback;
import model.SuperframeData;

import java.util.Iterator;
import java.util.Map;

/**
 * For the visualization of a list item for the freezes.
 */
public class ListCellFreeze extends ListCell<SuperframeData> implements Callback<ListView<SuperframeData>, ListCell<SuperframeData>> {

    public ListCellFreeze() {
        Popup popup = new Popup();
        Label classes = new Label();
        Label freezeNumber = new Label();
        freezeNumber.setGraphic(new Label("Freeze:"));
        VBox vBox = new VBox();
        vBox.getChildren().add(freezeNumber);
        vBox.getChildren().add(new Separator());
        vBox.getChildren().add(classes);
        vBox.getStylesheets().add(getClass().getResource("/css/VideoReviewPopUp.css").toExternalForm());
        vBox.getStyleClass().add("superframe-popup");
        popup.getContent().add(vBox);


        hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (getItem() == null)
                return;
            Map<String, Integer> classToCount = getItem().getMappingClassToCount();
            if (newValue && !isEmpty() && !classToCount.isEmpty()) {
                StringBuilder countStr = new StringBuilder();
                Iterator<String> iter = classToCount.keySet().iterator();
                while (iter.hasNext()) {
                    String aClass = iter.next();
                    countStr.append(aClass).append(":\t").append(classToCount.get(aClass));
                    if (iter.hasNext())
                        countStr.append("\n");
                }
                freezeNumber.setText(String.valueOf(getItem().getFrameNumber()));
                classes.setText(countStr.toString());
                // popup follows cursor
                setOnMouseMoved(event -> popup.show(this, event.getScreenX() + 15, event.getScreenY()));
            } else {
                popup.hide();
                setOnMouseMoved(null);
                classes.setText("");
                freezeNumber.setText("#");
            }
        });
    }

    @Override
    protected void updateItem(SuperframeData item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText("Freeze " + item.getFrameNumber());
        }
    }

    @Override
    public ListCell<SuperframeData> call(ListView<SuperframeData> listView) {
        return new ListCellFreeze();
    }

}
