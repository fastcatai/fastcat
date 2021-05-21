package keymap.transfer.importer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import javafx.scene.input.KeyCombination;
import keymap.Program;
import keymap.manager.SMFactory;
import keymap.manager.ShortcutManager;
import keymap.model.Action;
import keymap.model.KeyAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ImportIANoVersion implements ImportKeymap {

    private static final Logger logger = LogManager.getLogger(ImportIANoVersion.class);

    @Override
    public Map<Action, KeyAction> loadKeymap(final Path keymapFile) throws IOException {
        Map<Action, KeyAction> userKeyActions = new LinkedHashMap<>();
        ShortcutManager shortcutManager = SMFactory.getShortcutManager(Program.IMAGE_ANNOTATION);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(Files.newInputStream(keymapFile));
        ArrayNode actions = (ArrayNode) root.get("ImageAnnotation");
        CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        for (JsonNode a : actions) {

            String actionKey = a.has("action") ? "action" : "actionName";
            String actionName = actionNameConverter(a.get(actionKey).textValue());

            List<String> shortcuts = mapper.convertValue(a.get("shortcuts"), collectionType);
            // only add if action has shortcuts
            if (shortcuts.size() > 0) {
                if (shortcuts.size() > 1)
                    logger.info("Action '{}' has {} shortcuts: First shortcut is used, the rest will be ignored", actionName, shortcuts.size());
                Action action = Action.valueOfActionName(shortcutManager.getAllActions(), actionName);
                userKeyActions.put(action, new KeyAction(action, KeyCombination.valueOf(shortcuts.get(0))));
            }
        }
        return userKeyActions;
    }

    /**
     * Converts old names into new names.
     *
     * @param oldActionName old action name
     * @return a new action name if available, otherwise the same value
     */
    private String actionNameConverter(String oldActionName) {
        switch (oldActionName) {
            case "JumpToNextVerifiedBox":
                return "JumpToNextVerifiedAnnotation";
            case "IAAddDefaultImageLabel":
                return "AddDefaultImageLabel";
            case "IADeleteAnnotation":
                return "DeleteAnnotation";
            default:
                return oldActionName;
        }
    }
}
