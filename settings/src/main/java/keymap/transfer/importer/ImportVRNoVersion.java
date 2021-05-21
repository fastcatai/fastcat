package keymap.transfer.importer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import keymap.KeymapUtils;
import keymap.Program;
import keymap.manager.SMFactory;
import keymap.manager.ShortcutManager;
import keymap.model.Action;
import keymap.model.ActionClass;
import keymap.model.KeyAction;
import keymap.model.KeyActionClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ImportVRNoVersion implements ImportKeymap, ImportClassMappings {

    private static final Logger logger = LogManager.getLogger(ImportVRNoVersion.class);

    @Override
    public Map<String, KeyActionClass> importClassMappings(Path keymapFile) throws IOException {
        Map<String, KeyActionClass> userClasses = new LinkedHashMap<>();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(Files.newInputStream(keymapFile));
        ArrayNode actions = (ArrayNode) root.get("VideoReviewFrameViewer");
        CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(List.class, String.class);

        for (JsonNode a : actions) {

            String actionKey = a.has("action") ? "action" : "actionName";
            String oldActionName = a.get(actionKey).textValue();

            if (!oldActionName.equals("AddClass"))
                continue;
            String className = a.get("class").textValue();
            List<String> shortcuts = mapper.convertValue(a.get("shortcuts"), collectionType);

            if (shortcuts.size() > 0) {
                // check if list has more then 2 shortcuts
                if (shortcuts.size() > 2) {
                    logger.info("Class {} has more then 2 shortcuts: " +
                                    "Fist shortcut without Ctrl modifier will be used, the rest will be ignored. " +
                                    "If there is an equivalent shortcut with Ctrl, then this will be used as inversion shortcut, " +
                                    "otherwise this will be empty",
                            className);
                }

                KeyCodeCombination classKC = null;
                KeyCodeCombination inversionKC = null;
                Optional<KeyCodeCombination> optClassKC = KeymapUtils.searchClassShortcut(shortcuts);
                if (optClassKC.isPresent()) {
                    classKC = optClassKC.get();
                    Optional<KeyCodeCombination> optInversionKC = KeymapUtils.searchClassInversionShortcut(shortcuts, classKC);
                    if (optInversionKC.isPresent())
                        inversionKC = optInversionKC.get();
                }

                // add to class mappings
                userClasses.put(className, new KeyActionClass(new ActionClass(className), classKC, inversionKC));
            }
        }
        return userClasses;
    }

    @Override
    public Map<Action, KeyAction> loadKeymap(final Path keymapFile) throws IOException {
        Map<Action, KeyAction> userKeyActions = new LinkedHashMap<>();
        ShortcutManager shortcutManager = SMFactory.getShortcutManager(Program.VIDEO_REVIEW);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(Files.newInputStream(keymapFile));
        userKeyActions.putAll(loadVideoReviewActions(root, shortcutManager, mapper));
        userKeyActions.putAll(loadVideoReviewFrameViewerActions(root, shortcutManager, mapper));
        return userKeyActions;
    }

    private Map<Action, KeyAction> loadVideoReviewActions(JsonNode root, ShortcutManager shortcutManager, ObjectMapper mapper) {
        Map<Action, KeyAction> userKeyActions = new LinkedHashMap<>();
        ArrayNode actions = (ArrayNode) root.get("VideoReview");
        CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        for (JsonNode a : actions) {

            String actionKey = a.has("action") ? "action" : "actionName";
            String actionName = actionNameConverter(a.get(actionKey).textValue());

            List<String> shortcuts = mapper.convertValue(a.get("shortcuts"), collectionType);
            // only add if action has shortcut
            if (shortcuts.size() > 0) {
                if (shortcuts.size() > 1)
                    logger.info("Action '{}' has {} shortcuts: First shortcut is used, the rest will be ignored", actionName, shortcuts.size());
                Action action = Action.valueOfActionName(shortcutManager.getAllActions(), actionName);
                userKeyActions.put(action, new KeyAction(action, KeyCombination.valueOf(shortcuts.get(0))));
            }
        }
        return userKeyActions;
    }

    private Map<Action, KeyAction> loadVideoReviewFrameViewerActions(JsonNode root, ShortcutManager shortcutManager, ObjectMapper mapper) {
        Map<Action, KeyAction> userKeyActions = new LinkedHashMap<>();
        ArrayNode actions = (ArrayNode) root.get("VideoReviewFrameViewer");
        CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        for (JsonNode a : actions) {

            String actionKey = a.has("action") ? "action" : "actionName";
            String oldActionName = a.get(actionKey).textValue();

            if (oldActionName.equals("AddClass"))
                continue;
            String actionName = actionNameConverter(oldActionName);
            List<String> shortcuts = mapper.convertValue(a.get("shortcuts"), collectionType);
            // only add if action has shortcut
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
            case "VROpenDialog":
                return "OpenLoadVideoDialog";
            case "VRSaveDialog":
                return "SaveDialog";
            case "VRDeleteAnnotation":
                return "DeleteAnnotation";
            case "VRAddStartTag":
                return "AddStartTag";
            case "VRAddEndTag":
                return "AddEndTag";
            default:
                return oldActionName;
        }
    }
}
