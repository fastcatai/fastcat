package listviewcells;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class ListCellSecondaryLabel extends ListCell<String>
        implements Callback<ListView<String>, ListCell<String>> {

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            HBox label = new HBox(new Label(getItem()));
            label.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(label, Priority.ALWAYS);

            Region buttonClose = new Region();
            buttonClose.getStyleClass().add("additional-label-delete");
            buttonClose.setOnMousePressed(event -> getListView().getItems().remove(getIndex()));
            VBox close = new VBox(buttonClose);
            close.setAlignment(Pos.CENTER);

            setText(null);
            setGraphic(new HBox(label, close));
        }
    }

    @Override
    public ListCell<String> call(ListView<String> param) {
        return new ListCellSecondaryLabel();
    }
}
