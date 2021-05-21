package keymap.model;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.input.KeyCombination;

import java.util.Optional;

public interface ReadOnlyKeyActionClass extends ReadOnlyKeyAction {
    ReadOnlyObjectProperty<KeyCombination> inversionKeyCombinationProperty();
    Optional<KeyCombination> getInversionKeyCombination();
    KeyActionClass deepCopy();
}
