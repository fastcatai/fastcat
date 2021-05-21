package keymap;

import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.util.List;
import java.util.Optional;

public class KeymapUtils {

    /**
     * Search for first shortcut without Ctrl
     *
     * @param shortcuts list of string shortcuts
     * @return a key combination if found
     */
    public static Optional<KeyCodeCombination> searchClassShortcut(final List<String> shortcuts) {
        for (String s : shortcuts) {
            KeyCodeCombination kc = (KeyCodeCombination) KeyCombination.valueOf(s);
            if (kc.getControl() == KeyCombination.ModifierValue.UP) {
                return Optional.of(kc);
            }
        }
        return Optional.empty();
    }

    /**
     * Searched an equivalent inversion shortcut (with Ctrl).
     *
     * @param shortcuts list of all string shortcut
     * @param classKC   the class shortcut for with the inversion should be searched
     * @return a key combination if found
     */
    public static Optional<KeyCodeCombination> searchClassInversionShortcut(final List<String> shortcuts, final KeyCodeCombination classKC) {
        for (String s : shortcuts) {
            KeyCodeCombination kc = (KeyCodeCombination) KeyCombination.valueOf(s);
            if (kc.getControl() == KeyCombination.ModifierValue.DOWN) {
                KeyCodeCombination potentialInversionKC = new KeyCodeCombination(kc.getCode(), kc.getShift(),
                        KeyCombination.ModifierValue.UP, kc.getAlt(), kc.getMeta(), kc.getShortcut());
                if (potentialInversionKC.equals(classKC)) {
                    return Optional.of(kc);
                }
            }
        }
        return Optional.empty();
    }
}
