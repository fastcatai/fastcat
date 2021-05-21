package controller;

import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import keymap.ListCellShortcuts;
import keymap.ListCellVRClasses;
import keymap.Program;
import keymap.dialogs.ClassCreatorDialog;
import keymap.manager.SMFactory;
import keymap.manager.VRShortcutManager;
import keymap.model.Action;
import keymap.model.ActionClass;
import keymap.model.KeyAction;
import keymap.model.KeyActionClass;
import keymap.model.ReadOnlyKeyAction;
import keymap.model.ReadOnlyKeyActionClass;
import keymap.transfer.KeymapTransfer;
import keymap.transfer.exporter.ExportUtils;
import keymap.transfer.importer.ImportClassMappings;
import keymap.transfer.importer.ImportKeymap;
import ui.Dialogs;
import util.SystemProperties;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class KeymapVRController implements SettingsBaseController {

    @FXML
    public ListView<KeyAction> listViewShortcuts;
    @FXML
    public ListView<KeyActionClass> listViewClasses;
    @FXML
    public Label labelCreateClass;

    private final VRShortcutManager shortcutManager;
    private Map<Action, KeyAction> keyActionsTempCopy;
    private ObservableMap<String, KeyActionClass> classKeyActionsTempCopy;

    public KeymapVRController() {
        shortcutManager = (VRShortcutManager) SMFactory.getShortcutManager(Program.VIDEO_REVIEW);
    }

    @FXML
    private void initialize() {
        // make a deep copy of list, so that changed not directly are reflected into the program
        keyActionsTempCopy = new LinkedHashMap<>();
        for (Map.Entry<Action, ReadOnlyKeyAction> a : shortcutManager.getActionMappings().entrySet())
            keyActionsTempCopy.put(a.getKey(), a.getValue().deepCopy());
        listViewShortcuts.getItems().addAll(keyActionsTempCopy.values());

        // make deep copy of class shortcuts, so that changed not directly are reflected into the program
        classKeyActionsTempCopy = FXCollections.observableMap(new LinkedHashMap<>());
        Map<String, ReadOnlyKeyActionClass> classActionMapping = shortcutManager.getClassActionMappings();
        for (String c : classActionMapping.keySet())
            classKeyActionsTempCopy.put(c, classActionMapping.get(c).deepCopy());

        // add defaults to list view
        listViewClasses.getItems().addAll(classKeyActionsTempCopy.values());
        // synchronize list view with class map (all modifications must go though the map)
        classKeyActionsTempCopy.addListener((MapChangeListener<? super String, ? super KeyActionClass>) change -> {
            if (change.wasRemoved()) listViewClasses.getItems().remove(change.getValueRemoved());
            if (change.wasAdded()) listViewClasses.getItems().add(change.getValueAdded());
        });

        listViewShortcuts.setCellFactory(new ListCellShortcuts(Collections.unmodifiableMap(keyActionsTempCopy)));
        listViewClasses.setCellFactory(new ListCellVRClasses(keyActionsTempCopy, classKeyActionsTempCopy,
                shortcutManager.getDefaultClassMappings().keySet()));

        labelCreateClass.setOnMouseClicked(event ->
                ClassCreatorDialog.showAndWait(keyActionsTempCopy, classKeyActionsTempCopy).ifPresent(cs ->
                        classKeyActionsTempCopy.put(cs.className, new KeyActionClass(new ActionClass(cs.className), cs.shortcut, cs.inversionShortcut))));
    }

    @FXML
    public void importKeymap() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Keymap File for Import");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Keymap files (*.keymap)", "*.keymap"));
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File keymapFile = chooser.showOpenDialog(listViewShortcuts.getScene().getWindow());
        if (keymapFile != null) {
            try {
                // get user shortcuts
                ImportKeymap userVrImporter = KeymapTransfer.getImporter(keymapFile.toPath(), Program.VIDEO_REVIEW);
                Map<Action, KeyAction> userImports = userVrImporter.loadKeymap(keymapFile.toPath());

                // get user classes
                ImportClassMappings userVrClassImporter = KeymapTransfer.getClassMappingsImporter(keymapFile.toPath(), Program.VIDEO_REVIEW);
                Map<String, KeyActionClass> userClasses = userVrClassImporter.importClassMappings(keymapFile.toPath());

                // import user mapping to temp map copy
                for (Map.Entry<Action, KeyAction> a : userImports.entrySet())
                    keyActionsTempCopy.get(a.getKey()).setKeyCombination(a.getValue().getKeyCombination());

                // import user classes mappings to temp map copy
                for (Map.Entry<String, KeyActionClass> c : userClasses.entrySet()) {
                    if (classKeyActionsTempCopy.containsKey(c.getKey())) {
                        KeyActionClass kac = classKeyActionsTempCopy.get(c.getKey());
                        kac.setKeyCombination(c.getValue().getKeyCombination());
                        kac.setInversionKeyCombination(c.getValue().getInversionKeyCombination().orElse(null));
                    } else { // create new class
                        classKeyActionsTempCopy.put(c.getKey(), new KeyActionClass(new ActionClass(c.getKey()),
                                c.getValue().getKeyCombination(),
                                c.getValue().getInversionKeyCombination().orElse(null)));
                    }
                }

                Dialogs.showInformation("Import Successful", null, "File was successfully imported");
            } catch (Exception e) {
                Dialogs.showExceptionDialog("Import Error", e);
            }
        }
    }

    @FXML
    public void exportKeymap() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Video Review Keymap");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Keymap file (*.keymap)", "*.keymap"));
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File exportFile = chooser.showSaveDialog(listViewShortcuts.getScene().getWindow());
        if (exportFile != null) {
            try {
                KeymapTransfer.getExporter(Program.VIDEO_REVIEW).export(ExportUtils.convertToReadOnlyMap(keyActionsTempCopy), exportFile.toPath());
                KeymapTransfer.getClassMappingsExporter(Program.VIDEO_REVIEW).exportClassMappings(ExportUtils.convertToReadOnlyClassMap(classKeyActionsTempCopy), exportFile.toPath());
                Dialogs.showInformation("Export Successful", null, "Video Review keymap was successfully exported");
            } catch (Exception e) {
                Dialogs.showError("Error", null, e.getMessage());
            }
        }
    }

    @Override
    public void onClickOk() {
        // get changes and import into working map
        Map<Action, ReadOnlyKeyAction> changes = ExportUtils.getMapChanges(keyActionsTempCopy,
                shortcutManager.getDefaultActionMappings(), shortcutManager.getActionMappings());
        shortcutManager.importChanges(changes);
        // clean changes for export (changes that are the same as default)
        Map<Action, ReadOnlyKeyAction> exportChanges = ExportUtils.cleanChanges(changes, shortcutManager.getDefaultActionMappings());

        // get changes and import into working class map
        Map<String, ReadOnlyKeyActionClass> classChanges = ExportUtils.getClassMapChanges(classKeyActionsTempCopy,
                shortcutManager.getDefaultClassMappings(), shortcutManager.getClassActionMappings());
        shortcutManager.importClassMappings(classChanges);
        // clean changes for export (changes that are the same as default)
        Map<String, ReadOnlyKeyActionClass> exportClassChanges = ExportUtils.cleanClassChanges(classChanges, shortcutManager.getDefaultClassMappings());

        // save changes into user config folder
        Path exportFile = Path.of(System.getProperty(SystemProperties.VR_CONFIG_PATH), "videoReview.keymap");
        try {
            KeymapTransfer.getExporter(Program.VIDEO_REVIEW).export(exportChanges, exportFile);
            KeymapTransfer.getClassMappingsExporter(Program.VIDEO_REVIEW).exportClassMappings(exportClassChanges, exportFile);
        } catch (Exception e) {
            Dialogs.showError("Error", null, e.getMessage());
        }
    }

    @Override
    public void onClickCancel() {
        // nothing
    }
}
