package keymap.model;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.input.KeyCombination;
import keymap.IllegalKeyCombinationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class KeyActionClass extends KeyAction implements ReadOnlyKeyActionClass {

    private static final Logger logger = LogManager.getLogger(KeyActionClass.class);

    private final ReadOnlyObjectWrapper<KeyCombination> inversionKeyCombinationProperty;

    public KeyActionClass(Action action) {
        this(action, null);
    }

    public KeyActionClass(Action action, KeyCombination keyCombination) {
        this(action, keyCombination, null);
    }

    public KeyActionClass(Action action, KeyCombination keyCombination, KeyCombination inversionKeyCombination) {
        super(action, keyCombination);
        inversionKeyCombinationProperty = new ReadOnlyObjectWrapper<>(inversionKeyCombination);
    }

    /**
     * Must not contain a CONTROL_DOWN modifier.
     *
     * @param keyCombination primary class shortcut
     */
    @Override
    public void setKeyCombination(KeyCombination keyCombination) throws IllegalKeyCombinationException {
        if (keyCombination == null || keyCombination.getControl() == KeyCombination.ModifierValue.UP)
            super.setKeyCombination(keyCombination);
        else throw logger.throwing(new IllegalKeyCombinationException("Class key combination is not allow to have a Ctrl modifier"));
    }

    public void setKeyCombination(KeyCombination keyCombination, boolean ignoreCtrlCheck) throws IllegalKeyCombinationException {
        if (ignoreCtrlCheck) {
            super.setKeyCombination(keyCombination);
            return;
        }
        this.setKeyCombination(keyCombination);
    }

    /**
     * Must contain a CONTROL_DOWN modifier.
     *
     * @param keyCombination inversion shortcut
     */
    public void setInversionKeyCombination(KeyCombination keyCombination) throws IllegalKeyCombinationException {
        if (keyCombination == null || keyCombination.getControl() == KeyCombination.ModifierValue.DOWN)
            inversionKeyCombinationProperty.set(keyCombination);
        else throw logger.throwing(new IllegalKeyCombinationException("Inversion key combination must have a Ctrl modifier"));
    }

    @Override
    public Optional<KeyCombination> getInversionKeyCombination() {
        return Optional.ofNullable(inversionKeyCombinationProperty.get());
    }

    @Override
    public ReadOnlyObjectProperty<KeyCombination> inversionKeyCombinationProperty() {
        return inversionKeyCombinationProperty.getReadOnlyProperty();
    }

    public void setBothKeyCombinations(KeyCombination classKC, KeyCombination inversionKC) throws IllegalKeyCombinationException {
        setKeyCombination(classKC);
        setInversionKeyCombination(inversionKC);
    }

    @Override
    public KeyActionClass deepCopy() {
        if (getKeyCombination() == null && getInversionKeyCombination().isEmpty())
            return new KeyActionClass(getAction());
        KeyCombination inversionKC = null;
        if (getInversionKeyCombination().isPresent())
            inversionKC = KeyCombination.valueOf(getInversionKeyCombination().get().toString());
        return new KeyActionClass(getAction(), KeyCombination.valueOf(getKeyCombination().toString()), inversionKC);
    }

    @Override
    public String toString() {
        return String.format("KeyActionClass{%s, %s, %s}",
                getAction().getActionName(),
                getKeyCombination() != null ? getKeyCombination().getName() : "null",
                getInversionKeyCombination().map(KeyCombination::getName).orElse("null"));
    }
}
