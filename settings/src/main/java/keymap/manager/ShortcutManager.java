package keymap.manager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import javafx.scene.input.KeyCombination;
import keymap.model.Action;
import keymap.model.KeyAction;
import keymap.model.ReadOnlyKeyAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class ShortcutManager {

    private final Logger logger = LogManager.getLogger(this);

    protected final Map<Action, KeyAction> defaultKeyActions;
    protected final Map<Action, KeyAction> keyActions;

    private final Map<Action, ReadOnlyKeyAction> readOnlyDefaultKeyActions;
    private final Map<Action, ReadOnlyKeyAction> readOnlyKeyActions;

    ShortcutManager() {
        defaultKeyActions = loadDefaultJSON();

        // copy defaults into working map
        keyActions = new LinkedHashMap<>();
        for (Action a : defaultKeyActions.keySet()) {
            keyActions.put(a, defaultKeyActions.get(a).deepCopy());
        }

        // import custom JSON changes from user dir
        importChanges(loadCustomJSON());

        // create read only map
        LinkedHashMap<Action, ReadOnlyKeyAction> roDefaultKeyActions = new LinkedHashMap<>();
        for (Action a : defaultKeyActions.keySet())
            roDefaultKeyActions.put(a, defaultKeyActions.get(a));
        readOnlyDefaultKeyActions = Collections.unmodifiableMap(roDefaultKeyActions);

        // create read only map
        LinkedHashMap<Action, ReadOnlyKeyAction> roKeyActions = new LinkedHashMap<>();
        for (Action a : keyActions.keySet())
            roKeyActions.put(a, keyActions.get(a));
        readOnlyKeyActions = Collections.unmodifiableMap(roKeyActions);
    }

    /**
     * Resource path of the default keymap file.
     *
     * @return full path to file
     */
    public abstract String getDefaultKeymap();

    /**
     * Path to the user-defined keymap file.
     *
     * @return full path to custom file
     */
    public abstract String getCustomKeymap();

    /**
     * List of all available actions.
     *
     * @return array of all actions
     */
    public abstract Action[] getAllActions();

    public Map<Action, ReadOnlyKeyAction> getDefaultActionMappings() {
        return readOnlyDefaultKeyActions;
    }

    public Map<Action, ReadOnlyKeyAction> getActionMappings() {
        return readOnlyKeyActions;
    }

    public ReadOnlyKeyAction getKeyAction(Action action) {
        return keyActions.get(action);
    }

    private KeyAction createKeyAction(final Action action, final List<String> shortcuts) {
        if (action == null)
            throw logger.throwing(new NullPointerException("Action must not be null"));

        if (shortcuts.size() > 1) {
            logger.info("Action '{}' has {} shortcuts: First shortcut is used, the rest will be ignored",
                    action.getActionName(), shortcuts.size());
        }

        KeyCombination actionKeyCombination;
        if (shortcuts.size() == 0) actionKeyCombination = null;
        else actionKeyCombination = KeyCombination.valueOf(shortcuts.get(0));
        return new KeyAction(action, actionKeyCombination);
    }

    private Map<Action, KeyAction> loadDefaultJSON() {
        // load and read JSON default settings
        Map<String, List<String>> jsonMappings = new LinkedHashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode keymap = mapper.readTree(getClass().getResourceAsStream(getDefaultKeymap()));
            ObjectNode shortcuts = (ObjectNode) keymap.get("shortcuts");
            if (shortcuts != null && !shortcuts.isEmpty()) {
                MapType mapType = TypeFactory.defaultInstance().constructMapType(LinkedHashMap.class, String.class, List.class);
                jsonMappings = mapper.readValue(shortcuts.toString(), mapType);
            }
        } catch (IOException e) {
            logger.throwing(e);
        }

        // combine default actions with action in json
        Map<Action, KeyAction> keyActions = new LinkedHashMap<>();
        for (Action a : getAllActions()) {
            KeyAction ka;
            // action was in json default settings
            if (jsonMappings.containsKey(a.getActionName())) {
                List<String> shortcuts = jsonMappings.get(a.getActionName());
                ka = createKeyAction(a, shortcuts);
            } else ka = new KeyAction(a);
            keyActions.put(a, ka);
        }

        return keyActions;
    }

    private Map<Action, ReadOnlyKeyAction> loadCustomJSON() {
        // load and read JSON custom settings
        ObjectMapper mapper = new ObjectMapper();
        Map<String, List<String>> userMappings = new LinkedHashMap<>();
        try {
            JsonNode userKeymap = mapper.readTree(new FileInputStream(getCustomKeymap()));
            ObjectNode shortcuts = (ObjectNode) userKeymap.get("shortcuts");
            if (shortcuts != null && !shortcuts.isEmpty()) {
                MapType mapType = TypeFactory.defaultInstance().constructMapType(LinkedHashMap.class, String.class, List.class);
                userMappings = mapper.readValue(shortcuts.toString(), mapType);
            }
        } catch (FileNotFoundException e) {
            logger.warn("{} could not be found: Custom keymap is not loaded", getCustomKeymap());
        } catch (IOException e) {
            logger.error(e);
        }

        // create key action mapping for import
        Map<Action, ReadOnlyKeyAction> customKeyActions = new LinkedHashMap<>();
        for (String actionName : userMappings.keySet()) {
            List<String> shortcuts = userMappings.get(actionName);
            Action action = Action.valueOfActionName(getAllActions(), actionName);
            KeyAction ka = createKeyAction(action, shortcuts);
            customKeyActions.put(action, ka);
        }

        return customKeyActions;
    }

    public void importChanges(Map<Action, ReadOnlyKeyAction> changedKeyActions) {
        if (changedKeyActions == null)
            return;
        // replaces shortcut of user if key exists in map
        for (Action a : changedKeyActions.keySet()) {
            if (keyActions.containsKey(a)) {
                keyActions.get(a).setKeyCombination(changedKeyActions.get(a).getKeyCombination());
            }
        }
    }
}
