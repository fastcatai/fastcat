package keymap;

import i18n.I18N;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import keymap.dialogs.ShortcutModifierDialog;
import keymap.model.Action;
import keymap.model.KeyAction;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignK;

import java.util.Map;

public class ListCellShortcuts extends ListCell<KeyAction> implements Callback<ListView<KeyAction>, ListCell<KeyAction>> {

    private final Map<Action, KeyAction> keyActions;
    private ContextMenu contextMenu;

    public ListCellShortcuts(final Map<Action, KeyAction> keyActions) {
        this.keyActions = keyActions;
    }

    @Override
    protected void updateItem(KeyAction item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            setOnMouseClicked(null);
        } else {
            setText(null);
            setGraphic(createCell(item));
            setOnMouseClicked(this::onCellMouseClicked);
        }
    }

    private void onCellMouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2 || event.getButton() == MouseButton.SECONDARY) {
            if (contextMenu != null) contextMenu.hide();
            contextMenu = createContextMenu();
            contextMenu.show(this, event.getScreenX(), event.getScreenY());
        }
    }

    private HBox createCell(KeyAction item) {
        FontIcon icon = new FontIcon(MaterialDesignK.KEYBOARD_VARIANT);
        icon.setIconSize(20);

        HBox labelCell = new HBox(new Label(I18N.getString(item.getResourceKey())));
        labelCell.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(labelCell, Priority.ALWAYS);

        Label labelShortcut = new Label();
        labelShortcut.getStyleClass().add("shortcut-label");
        labelShortcut.visibleProperty().bind(labelShortcut.textProperty().isNotEmpty());
        labelShortcut.textProperty().bind(Bindings.createStringBinding(() ->
                        item.getKeyCombination() != null ? item.getKeyCombination().getName() : "",
                item.keyCombinationProperty()));

        HBox cell = new HBox(3, icon, labelCell, labelShortcut);
        cell.setAlignment(Pos.CENTER_LEFT);
        return cell;
    }

    private ContextMenu createContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        if (getItem().getKeyCombination() == null)
            contextMenu.getItems().add(createAddMenuItem());
        else contextMenu.getItems().addAll(createChangeMenuItem(), createRemoveMenuItem());
        return contextMenu;
    }

    private MenuItem createAddMenuItem() {
        MenuItem menuItem = new MenuItem("Add Shortcut");
        menuItem.setOnAction(event ->
                ShortcutModifierDialog.showAndWait(I18N.getString(getItem().getResourceKey()), keyActions).ifPresent(s ->
                        getItem().setKeyCombination(KeyCombination.valueOf(s))));
        return menuItem;
    }

    private MenuItem createChangeMenuItem() {
        MenuItem menuItem = new MenuItem("Change Shortcut");
        menuItem.setOnAction(event ->
                ShortcutModifierDialog.showAndWait(I18N.getString(getItem().getResourceKey()), keyActions, getItem().getKeyCombination())
                        .ifPresent(s -> getItem().setKeyCombination(KeyCombination.valueOf(s))));
        return menuItem;
    }

    private MenuItem createRemoveMenuItem() {
        MenuItem menuItem = new MenuItem("Remove Shortcut");
        menuItem.setOnAction(event -> getItem().setKeyCombination(null));
        return menuItem;
    }

    @Override
    public ListCell<KeyAction> call(ListView<KeyAction> param) {
        return new ListCellShortcuts(keyActions);
    }
}
