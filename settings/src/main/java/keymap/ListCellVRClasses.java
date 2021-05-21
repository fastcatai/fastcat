package keymap;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import keymap.dialogs.ClassShortcutModifierDialog;
import keymap.model.Action;
import keymap.model.KeyAction;
import keymap.model.KeyActionClass;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignL;

import java.util.Collection;
import java.util.Map;

public class ListCellVRClasses extends ListCell<KeyActionClass> implements Callback<ListView<KeyActionClass>, ListCell<KeyActionClass>> {

    private final Map<Action, KeyAction> keyActions;
    private final Map<String, KeyActionClass> classKeyActions;
    private final Collection<String> defaultClasses;
    private ContextMenu contextMenu;

    public ListCellVRClasses(final Map<Action, KeyAction> keyActions, final Map<String, KeyActionClass> classKeyActions,
                             final Collection<String> defaultClasses) {
        this.keyActions = keyActions;
        this.classKeyActions = classKeyActions;
        this.defaultClasses = defaultClasses;
    }

    @Override
    protected void updateItem(KeyActionClass item, boolean empty) {
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

    private HBox createCell(KeyActionClass item) {
        FontIcon icon = new FontIcon(MaterialDesignL.LABEL_OUTLINE);
        icon.setIconSize(20);
        if (defaultClasses.contains(item.getActionName()))
            icon.setIconColor(Color.GRAY);

        HBox labelCell = new HBox(new Label(item.getActionName()));
        labelCell.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(labelCell, Priority.ALWAYS);

        Label labelShortcut = new Label();
        labelShortcut.getStyleClass().add("shortcut-label");
        labelShortcut.visibleProperty().bind(labelShortcut.textProperty().isNotEmpty());
        labelShortcut.textProperty().bind(Bindings.createStringBinding(() ->
                        item.getKeyCombination() != null ? item.getKeyCombination().getName() : "",
                item.keyCombinationProperty()));

        Label labelInversionShortcut = new Label();
        labelInversionShortcut.getStyleClass().add("shortcut-label");
        labelInversionShortcut.visibleProperty().bind(labelInversionShortcut.textProperty().isNotEmpty());
        labelInversionShortcut.textProperty().bind(Bindings.createStringBinding(() ->
                        item.getInversionKeyCombination().map(KeyCombination::getName).orElse(""),
                item.inversionKeyCombinationProperty()));

        HBox cell = new HBox(3, icon, labelCell, new HBox(5, labelShortcut, labelInversionShortcut));
        cell.setAlignment(Pos.CENTER_LEFT);
        return cell;
    }

    private ContextMenu createContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        if (getItem().getKeyCombination() == null && defaultClasses.contains(getItem().getActionName()))
            contextMenu.getItems().addAll(createAddMenuItem());
        else if (getItem().getKeyCombination() == null && !defaultClasses.contains(getItem().getActionName()))
            contextMenu.getItems().addAll(createAddMenuItem(), new SeparatorMenuItem(), createRemoveClassMenuItem());
        else if (defaultClasses.contains(getItem().getActionName()))
            contextMenu.getItems().addAll(createChangeMenuItem(), createRemoveMenuItem());
        else contextMenu.getItems().addAll(createChangeMenuItem(), createRemoveMenuItem(),
                    new SeparatorMenuItem(), createRemoveClassMenuItem());
        return contextMenu;
    }

    private MenuItem createAddMenuItem() {
        MenuItem menuItem = new MenuItem("Add Shortcut");
        menuItem.setOnAction(event ->
                ClassShortcutModifierDialog.showAndWait(getItem().getActionName(), null, keyActions, classKeyActions)
                        .ifPresent(cs -> getItem().setBothKeyCombinations(cs.shortcut, cs.inversionShortcut)));
        return menuItem;
    }

    private MenuItem createChangeMenuItem() {
        MenuItem menuItem = new MenuItem("Change Shortcut");
        menuItem.setOnAction(event ->
                ClassShortcutModifierDialog.showAndWait(getItem().getActionName(),
                        (KeyCodeCombination) getItem().getKeyCombination(),
                        (KeyCodeCombination) getItem().getInversionKeyCombination().orElse(null),
                        keyActions,
                        classKeyActions)
                        .ifPresent(s -> {
                            getItem().setBothKeyCombinations(s.shortcut, s.inversionShortcut);
                            System.out.println(getItem());
                        }));
        return menuItem;
    }

    private MenuItem createRemoveMenuItem() {
        MenuItem menuItem = new MenuItem("Remove Shortcut");
        menuItem.setOnAction(event -> getItem().setBothKeyCombinations(null, null));
        return menuItem;
    }

    private MenuItem createRemoveClassMenuItem() {
        MenuItem menuItem = new MenuItem("Remove Class");
        menuItem.setOnAction(event -> classKeyActions.remove(getItem().getActionName(), getItem()));
        return menuItem;
    }

    @Override
    public ListCell<KeyActionClass> call(ListView<KeyActionClass> param) {
        return new ListCellVRClasses(keyActions, classKeyActions, defaultClasses);
    }
}
