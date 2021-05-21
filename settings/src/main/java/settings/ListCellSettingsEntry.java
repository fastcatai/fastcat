package settings;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class ListCellSettingsEntry extends ListCell<SettingsEntry> implements Callback<ListView<SettingsEntry>, ListCell<SettingsEntry>> {

    @Override
    protected void updateItem(SettingsEntry item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item.getText());
            if (item.getIcon() != null)
                setGraphic(item.getIcon());
            else setGraphic(null);
        }
    }

    @Override
    public ListCell<SettingsEntry> call(ListView<SettingsEntry> param) {
        return new ListCellSettingsEntry();
    }
}
