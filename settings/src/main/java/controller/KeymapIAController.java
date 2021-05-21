package controller;

import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import keymap.ListCellICClasses;
import keymap.ListCellShortcuts;
import keymap.Program;
import keymap.dialogs.ClassCreatorDialog;
import keymap.dialogs.ICCreatorDialog;
import keymap.manager.IAShortcutManager;
import keymap.manager.SMFactory;
import keymap.manager.ShortcutManager;
import keymap.model.Action;
import keymap.model.ActionClass;
import keymap.model.KeyAction;
import keymap.model.KeyActionClass;
import keymap.model.ReadOnlyKeyAction;
import keymap.model.ReadOnlyKeyActionClass;
import keymap.transfer.KeymapTransfer;
import keymap.transfer.exporter.ExportUtils;
import keymap.transfer.importer.ImportKeymap;
import ui.Dialogs;
import util.SystemProperties;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class KeymapIAController implements SettingsBaseController {

    @FXML
    public Hyperlink hyperlinkImport;
    @FXML
    public Hyperlink hyperlinkExport;
    @FXML
    public ListView<KeyAction> listViewShortcuts;
    @FXML
    public Label labelCreateClass;
    @FXML
    public ListView<KeyActionClass> listViewClasses;

    private final IAShortcutManager shortcutManager;
    private Map<Action, KeyAction> keyActionsTempCopy;
    private ObservableMap<String, KeyActionClass> classKeyActionsTempCopy;


    public KeymapIAController() {
        shortcutManager = (IAShortcutManager) SMFactory.getShortcutManager(Program.IMAGE_ANNOTATION);
    }

    @FXML
    private void initialize() {
        // make a deep copy of list, so that changed not directly are reflected into the program
        keyActionsTempCopy = new LinkedHashMap<>();
        for (Map.Entry<Action, ReadOnlyKeyAction> a : shortcutManager.getActionMappings().entrySet())
            keyActionsTempCopy.put(a.getKey(), a.getValue().deepCopy());
        listViewShortcuts.getItems().addAll(keyActionsTempCopy.values());

        listViewShortcuts.setCellFactory(new ListCellShortcuts(Collections.unmodifiableMap(keyActionsTempCopy)));
        listViewClasses.setCellFactory(new ListCellICClasses(keyActionsTempCopy, classKeyActionsTempCopy));

        hyperlinkImport.setOnAction(this::importKeymap);
        hyperlinkExport.setOnAction(this::exportKeymap);

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

        labelCreateClass.setOnMouseClicked(event ->
                ICCreatorDialog.showAndWait(keyActionsTempCopy, classKeyActionsTempCopy).ifPresent(cs ->
                        classKeyActionsTempCopy.put(cs.className, new KeyActionClass(new ActionClass(cs.className), cs.shortcut))));

    }

    private void importKeymap(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Keymap File for Import");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Keymap files (*.keymap)", "*.keymap"));
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File keymapFile = chooser.showOpenDialog(hyperlinkImport.getScene().getWindow());
        if (keymapFile != null) {
            try {
                // import user mapping to temp map copy
                ImportKeymap userIaImporter = KeymapTransfer.getImporter(keymapFile.toPath(), Program.IMAGE_ANNOTATION);
                Map<Action, KeyAction> userImports = userIaImporter.loadKeymap(keymapFile.toPath());
                for (Map.Entry<Action, KeyAction> a : userImports.entrySet())
                    keyActionsTempCopy.get(a.getKey()).setKeyCombination(a.getValue().getKeyCombination());
                Dialogs.showInformation("Import Successful", null, "File was successfully imported");
            } catch (Exception e) {
                Dialogs.showError("Import Error", null, e.getMessage());
            }
        }
    }

    private void exportKeymap(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Image Annotation Keymap");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Keymap file (*.keymap)", "*.keymap"));
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File exportFile = chooser.showSaveDialog(hyperlinkExport.getScene().getWindow());
        if (exportFile != null) {
            try {
                KeymapTransfer.getExporter(Program.IMAGE_ANNOTATION)
                        .export(ExportUtils.convertToReadOnlyMap(keyActionsTempCopy), exportFile.toPath());
                Dialogs.showInformation("Export Successful", null, "Image Annotation keymap was successfully exported");
            } catch (Exception e) {
                Dialogs.showExceptionDialog("Import Error", e);
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
        Path exportFile = Path.of(System.getProperty(SystemProperties.IA_CONFIG_PATH), "imageAnnotation.keymap");
        try {
            KeymapTransfer.getExporter(Program.IMAGE_ANNOTATION).export(exportChanges, exportFile);
            KeymapTransfer.getClassMappingsExporter(Program.VIDEO_REVIEW).exportClassMappings(exportClassChanges, exportFile);
        } catch (Exception e) {
            Dialogs.showExceptionDialog("IA Keymap Save Error", e);
        }
    }

    @Override
    public void onClickCancel() {
        // nothing
    }
}
