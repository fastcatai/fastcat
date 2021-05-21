package keymap.model;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.input.KeyCombination;

public interface ReadOnlyKeyAction {
    ReadOnlyObjectProperty<KeyCombination> keyCombinationProperty();
    KeyCombination getKeyCombination();
    Action getAction();
    String getActionName();
    String getResourceKey();
    KeyAction deepCopy();
}
