package keymap.model;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.input.KeyCombination;
import keymap.IllegalKeyCombinationException;

public class KeyAction implements ReadOnlyKeyAction {

    private final Action action;
    private final ReadOnlyObjectWrapper<KeyCombination> keyCombinationProperty;

    public KeyAction(final Action action) {
        this(action, null);
    }

    public KeyAction(final Action action, final KeyCombination keyCombination) {
        this.action = action;
        keyCombinationProperty = new ReadOnlyObjectWrapper<>(keyCombination);
    }

    @Override
    public ReadOnlyObjectProperty<KeyCombination> keyCombinationProperty() {
        return keyCombinationProperty.getReadOnlyProperty();
    }

    public void setKeyCombination(KeyCombination keyCombination) throws IllegalKeyCombinationException {
        keyCombinationProperty.set(keyCombination);
    }

    @Override
    public KeyCombination getKeyCombination() {
        return keyCombinationProperty.get();
    }

    @Override
    public Action getAction() {
        return action;
    }

    @Override
    public String getActionName() {
        return action.getActionName();
    }

    @Override
    public String getResourceKey() {
        return action.getResourceKey();
    }

    @Override
    public KeyAction deepCopy() {
        if (keyCombinationProperty.isNull().get())
            return new KeyAction(action);
        return new KeyAction(action, KeyCombination.valueOf(keyCombinationProperty.get().toString()));
    }

    @Override
    public String toString() {
        return String.format("KeyAction{%s, %s}",
                action.getActionName(),
                getKeyCombination() != null ? getKeyCombination().getName() : "null");
    }
}