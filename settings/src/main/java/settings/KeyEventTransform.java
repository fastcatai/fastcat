package settings;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KeyEventTransform {

    public static Optional<String> toString(KeyEvent event) {
        if (event.getCode() == KeyCode.UNDEFINED)
            return Optional.empty();
        if (!event.getCode().isModifierKey())
            return Optional.of(createKeyCombination(event).getName());
        else return Optional.of(event.getCode().getName());
    }

    public static Optional<String> toStringWithoutControl(KeyEvent event) {
        if (event.getCode() == KeyCode.UNDEFINED || (event.getCode().isModifierKey() && event.getCode() == KeyCode.CONTROL))
            return Optional.empty();
        if (!event.getCode().isModifierKey())
            return Optional.of(createKeyCombinationWithoutControl(event).getName());
        else return Optional.of(event.getCode().getName());
    }

    public static Optional<KeyCombination> createCombination(KeyEvent event) {
        if (event.getCode() == KeyCode.UNDEFINED || event.getCode().isModifierKey())
            return Optional.empty();
        return Optional.of(createKeyCombination(event));
    }

    private static KeyCombination createKeyCombinationWithoutControl(KeyEvent event) {
        List<KeyCombination.Modifier> modifiers = new ArrayList<>();
        if (event.isShiftDown())
            modifiers.add(KeyCombination.SHIFT_DOWN);
        if (event.isAltDown())
            modifiers.add(KeyCombination.ALT_DOWN);
        return new KeyCodeCombination(event.getCode(), modifiers.toArray(KeyCombination.Modifier[]::new));
    }

    private static KeyCombination createKeyCombination(KeyEvent event) {
        List<KeyCombination.Modifier> modifiers = new ArrayList<>();
        if (event.isShiftDown())
            modifiers.add(KeyCombination.SHIFT_DOWN);
        if (event.isControlDown())
            modifiers.add(KeyCombination.CONTROL_DOWN);
        if (event.isAltDown())
            modifiers.add(KeyCombination.ALT_DOWN);
        return new KeyCodeCombination(event.getCode(), modifiers.toArray(KeyCombination.Modifier[]::new));
    }
}
