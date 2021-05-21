package keymap.transfer.exporter;

import javafx.scene.input.KeyCombination;
import keymap.model.Action;
import keymap.model.KeyAction;
import keymap.model.KeyActionClass;
import keymap.model.ReadOnlyKeyAction;
import keymap.model.ReadOnlyKeyActionClass;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ExportUtils {

    /**
     * Looks for changes between both maps.
     * An action has a change if there exists a difference in key combinations between temporary setting and default mapping.
     * And additionally between temporary setting and the current mapping in use.
     *
     * @param tempMappings    temporary map with potential changes
     * @param defaultMappings default map
     * @param currentMappings working map that is currently in use
     * @return a map with all changes
     */
    public static Map<Action, ReadOnlyKeyAction> getMapChanges(Map<Action, KeyAction> tempMappings,
                                                               Map<Action, ReadOnlyKeyAction> defaultMappings,
                                                               Map<Action, ReadOnlyKeyAction> currentMappings) {
        final Map<Action, ReadOnlyKeyAction> changes = new LinkedHashMap<>();
        // go through each temp mapping
        for (Action a : tempMappings.keySet()) {
            // get key combos from maps
            KeyCombination tempKC = tempMappings.get(a).getKeyCombination();
            KeyCombination defaultKC = defaultMappings.get(a).getKeyCombination();
            KeyCombination currentKC = currentMappings.get(a).getKeyCombination();

            // check difference
            if (!Objects.equals(tempKC, defaultKC) || !Objects.equals(tempKC, currentKC))
                changes.put(a, tempMappings.get(a));
        }
        return changes;
    }

    /**
     * Looks for all actions equivalent to the default and therefore can be deleted from changes for export.
     *
     * @param changes         changes map
     * @param defaultMappings default mappings of the program
     * @return updated map with null entries when it can be remove from json
     */
    public static Map<Action, ReadOnlyKeyAction> cleanChanges(Map<Action, ReadOnlyKeyAction> changes,
                                                              Map<Action, ReadOnlyKeyAction> defaultMappings) {
        Map<Action, ReadOnlyKeyAction> cleanedChanges = new LinkedHashMap<>();
        // go through all changes
        for (Action a : changes.keySet()) {
            // check if action that was changes is in defaults
            if (defaultMappings.containsKey(a)) {
                KeyCombination changedKC = changes.get(a).getKeyCombination();
                KeyCombination defaultKC = defaultMappings.get(a).getKeyCombination();

                // check if it not has the same cobo
                if (!Objects.equals(changedKC, defaultKC))
                    cleanedChanges.put(a, changes.get(a));

            } else cleanedChanges.put(a, changes.get(a));
        }
        return cleanedChanges;
    }

    /**
     * Looks for changes between both maps.
     * A class has a change if there exists a difference in key combinations between temporary setting and default mapping.
     * And additionally between temporary setting and the current mapping in use.
     * The same also applies for inverse shortcut.
     *
     * @param tempClassMapping     temporary map with potential changes
     * @param defaultClassMappings default map
     * @param currentClassMappings working map that is currently in use
     * @return a map with all class changes
     */
    public static Map<String, ReadOnlyKeyActionClass> getClassMapChanges(final Map<String, KeyActionClass> tempClassMapping,
                                                                         final Map<String, ReadOnlyKeyActionClass> defaultClassMappings,
                                                                         final Map<String, ReadOnlyKeyActionClass> currentClassMappings) {
        final Map<String, ReadOnlyKeyActionClass> changes = new LinkedHashMap<>();

        // go through each temp class mapping to check for changes
        for (String c : tempClassMapping.keySet()) {
            KeyActionClass tempKAC = tempClassMapping.get(c);
            // if class is in default map
            if (defaultClassMappings.containsKey(c)) {

                KeyCombination tempKC = tempKAC.getKeyCombination();
                KeyCombination defaultKC = defaultClassMappings.get(c).getKeyCombination();
                KeyCombination currentKC = currentClassMappings.get(c).getKeyCombination();
                KeyCombination tempIKC = tempKAC.getInversionKeyCombination().orElse(null);
                KeyCombination defaultIKC = defaultClassMappings.get(c).getInversionKeyCombination().orElse(null);
                KeyCombination currentIKC = currentClassMappings.get(c).getInversionKeyCombination().orElse(null);

                // check differences
                if (!Objects.equals(tempKC, defaultKC) || !Objects.equals(tempKC, currentKC)
                        || !Objects.equals(tempIKC, defaultIKC) || !Objects.equals(tempIKC, currentIKC))
                    changes.put(c, tempKAC);

            } else {
                // class is not in default list => add to changes
                changes.put(c, tempKAC);
            }
        }

        // check for added and deleted classes

        // intersection of both sets
        Set<String> intersection = new HashSet<>(tempClassMapping.keySet());
        intersection.retainAll(currentClassMappings.keySet());

        Set<String> deletedClasses = new HashSet<>(currentClassMappings.keySet());
        deletedClasses.removeAll(intersection);
        for (String deleted : deletedClasses)
            changes.put(deleted, null);

        Set<String> addedClasses = new HashSet<>(tempClassMapping.keySet());
        addedClasses.removeAll(intersection);
        for (String added : addedClasses)
            changes.put(added, tempClassMapping.get(added));

        return changes;
    }


    /**
     * Looks for all class actions equivalent to the default and therefore can be deleted from changes for export.
     *
     * @param classChanges         class changes map
     * @param defaultClassMappings default class mappings of the program
     * @return update map with no unnecessary class change entries
     */
    public static Map<String, ReadOnlyKeyActionClass> cleanClassChanges(Map<String, ReadOnlyKeyActionClass> classChanges,
                                                                        Map<String, ReadOnlyKeyActionClass> defaultClassMappings) {
        Map<String, ReadOnlyKeyActionClass> cleanedClassChanges = new LinkedHashMap<>();
        // go through all class changes
        for (String c : classChanges.keySet()) {
            ReadOnlyKeyActionClass changedKAC = classChanges.get(c);

            // if class was deleted the jump
            if (changedKAC == null)
                continue;

            // check if class that was changed is in defaults
            if (defaultClassMappings.containsKey(c)) {
                KeyCombination changedKC = changedKAC.getKeyCombination();
                KeyCombination defaultKC = defaultClassMappings.get(c).getKeyCombination();
                KeyCombination changedIKC = changedKAC.getInversionKeyCombination().orElse(null);
                KeyCombination defaultIKC = defaultClassMappings.get(c).getInversionKeyCombination().orElse(null);
                if (!Objects.equals(changedKC, defaultKC) || !Objects.equals(changedIKC, defaultIKC))
                    cleanedClassChanges.put(c, changedKAC);

            } else cleanedClassChanges.put(c, classChanges.get(c));
        }
        return cleanedClassChanges;
    }

    public static Map<String, List<String>> convertToSavableMap(Map<Action, ReadOnlyKeyAction> changes) {
        Map<String, List<String>> savable = new LinkedHashMap<>();
        if (changes == null || changes.isEmpty())
            return savable;

        for (Action a : changes.keySet()) {
            ReadOnlyKeyAction ka = changes.get(a);
            if (ka.getKeyCombination() == null) savable.put(a.getActionName(), List.of());
            else savable.put(a.getActionName(), List.of(ka.getKeyCombination().getName()));
        }

        return savable;
    }

    public static Map<String, List<String>> convertToSavableClassMap(Map<String, ReadOnlyKeyActionClass> changes) {
        Map<String, List<String>> savable = new LinkedHashMap<>();
        if (changes == null || changes.isEmpty())
            return savable;

        for (String c : changes.keySet()) {
            KeyCombination kc = changes.get(c).getKeyCombination();
            KeyCombination invKC = changes.get(c).getInversionKeyCombination().orElse(null);
            if (kc != null && invKC != null) savable.put(c, List.of(kc.getName(), invKC.getName()));
            else if (kc == null && invKC != null) savable.put(c, List.of(invKC.getName()));
            else if (kc != null) savable.put(c, List.of(kc.getName()));
            else savable.put(c, List.of());
        }

        return savable;
    }

    public static Map<Action, ReadOnlyKeyAction> convertToReadOnlyMap(Map<Action, KeyAction> mappings) {
        return mappings.entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, ReadOnlyKeyActionClass> convertToReadOnlyClassMap(Map<String, KeyActionClass> mappings) {
        return mappings.entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
