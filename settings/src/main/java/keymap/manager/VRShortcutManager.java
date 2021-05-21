package keymap.manager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.sun.javafx.collections.MappingChange;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import keymap.KeymapUtils;
import keymap.model.Action;
import keymap.model.ActionClass;
import keymap.model.ActionVR;
import keymap.model.KeyActionClass;
import keymap.model.ReadOnlyKeyActionClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.SystemProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class VRShortcutManager extends ShortcutManager {

    private static final Logger logger = LogManager.getLogger(VRShortcutManager.class);

    private final Map<String, ReadOnlyKeyActionClass> readOnlyDefaultClassMappings;

    private final ObservableMap<String, KeyActionClass> classMappings;
    private final ObservableMap<String, ReadOnlyKeyActionClass> readOnlyClassMappings;
    private final ObservableMap<String, ReadOnlyKeyActionClass> unmodifiableClassMappings;

    VRShortcutManager() {
        Map<String, KeyActionClass> defaultClassMappings = loadDefaultJSONClasses();
        // create read only default map
        LinkedHashMap<String, ReadOnlyKeyActionClass> roDefaultClasses = new LinkedHashMap<>();
        for (String c : defaultClassMappings.keySet())
            roDefaultClasses.put(c, defaultClassMappings.get(c));
        readOnlyDefaultClassMappings = Collections.unmodifiableMap(roDefaultClasses);

        classMappings = FXCollections.observableMap(new LinkedHashMap<>());
        // copy class defaults into working map
        for (String c : defaultClassMappings.keySet()) {
            classMappings.put(c, defaultClassMappings.get(c).deepCopy());
        }

        readOnlyClassMappings = FXCollections.observableMap(new LinkedHashMap<>(classMappings));
        // sync read only map with map
        classMappings.addListener((MapChangeListener<String, KeyActionClass>) change -> {
            if (change.wasRemoved()) readOnlyClassMappings.remove(change.getKey(), change.getValueRemoved());
            if (change.wasAdded()) readOnlyClassMappings.put(change.getKey(), change.getValueAdded());
        });
        unmodifiableClassMappings = FXCollections.unmodifiableObservableMap(readOnlyClassMappings);

        importClassMappings(loadCustomJSONClasses());
    }

    private Map<String, KeyActionClass> loadDefaultJSONClasses() {
        // load an read JSON default classes
        Map<String, List<String>> jsonMappings = new LinkedHashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode keymap = mapper.readTree(getClass().getResourceAsStream(getDefaultKeymap()));
            ObjectNode classes = (ObjectNode) keymap.get("classes");
            // deserialize when classes array is not empty
            if (classes != null && !classes.isEmpty()) {
                MapType mapType = TypeFactory.defaultInstance().constructMapType(LinkedHashMap.class, String.class, List.class);
                jsonMappings = mapper.readValue(classes.toString(), mapType);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // convert string shortcuts into key action
        Map<String, KeyActionClass> classMappings = new LinkedHashMap<>();
        for (String c : jsonMappings.keySet()) {
            List<String> shortcuts = jsonMappings.get(c);
            KeyActionClass kac = createKeyActionClass(c, shortcuts);
            classMappings.put(c, kac);
        }

        return classMappings;
    }

    protected Map<String, ReadOnlyKeyActionClass> loadCustomJSONClasses() {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, List<String>> jsonMappings = new LinkedHashMap<>();

        try {
            JsonNode userKeymap = mapper.readTree(new FileInputStream(getCustomKeymap()));
            ObjectNode userClasses = (ObjectNode) userKeymap.get("classes");
            // deserialize when classes array is not empty
            if (userClasses != null && !userClasses.isEmpty()) {
                MapType mapType = TypeFactory.defaultInstance().constructMapType(LinkedHashMap.class, String.class, List.class);
                jsonMappings = mapper.readValue(userClasses.toString(), mapType);
            }
        } catch (FileNotFoundException e) {
            logger.warn("{} for class mappings could not be found: Custom classes are not loaded", getCustomKeymap());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // convert string shortcuts into key action
        Map<String, ReadOnlyKeyActionClass> classMappings = new LinkedHashMap<>();
        for (String c : jsonMappings.keySet()) {
            List<String> shortcuts = jsonMappings.get(c);
            ReadOnlyKeyActionClass kac = createKeyActionClass(c, shortcuts);
            classMappings.put(c, kac);
        }

        return classMappings;
    }

    private KeyActionClass createKeyActionClass(final String className, final List<String> shortcuts) {
        if (className == null || className.isBlank())
            throw logger.throwing(new NullPointerException("Class name must not be null or empty"));

        if (shortcuts.size() <= 0)
            return new KeyActionClass(new ActionClass(className));

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

        return new KeyActionClass(new ActionClass(className), classKC, inversionKC);
    }

    public void importClassMappings(Map<String, ReadOnlyKeyActionClass> changedClassMappings) {
        if (changedClassMappings == null)
            return;
        // replace shortcuts of classes if class exists in map and add new classes
        for (String c : changedClassMappings.keySet()) {
            ReadOnlyKeyActionClass changedKAC = changedClassMappings.get(c);
            if (changedKAC == null) {
                classMappings.remove(c);
            } else if (classMappings.containsKey(c)) {
                KeyActionClass kac = classMappings.get(c);
                kac.setBothKeyCombinations(changedKAC.getKeyCombination(), changedKAC.getInversionKeyCombination().orElse(null));
            } else {
                classMappings.put(c, new KeyActionClass(new ActionClass(c),
                        changedKAC.getKeyCombination(), changedKAC.getInversionKeyCombination().orElse(null)));
            }
        }
    }

    @Override
    public String getDefaultKeymap() {
        return "/videoReview.keymap";
    }

    @Override
    public String getCustomKeymap() {
        return System.getProperty(SystemProperties.VR_CONFIG_PATH) + File.separatorChar + "videoReview.keymap";
    }

    @Override
    public Action[] getAllActions() {
        return ActionVR.values();
    }

    public ReadOnlyKeyActionClass getKeyActionClass(String className) {
        return classMappings.get(className);
    }

    public ObservableMap<String, ReadOnlyKeyActionClass> getClassActionMappings() {
        return unmodifiableClassMappings;
    }

    public Map<String, ReadOnlyKeyActionClass> getDefaultClassMappings() {
        return readOnlyDefaultClassMappings;
    }
}
