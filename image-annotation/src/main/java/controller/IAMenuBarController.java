package controller;

import about.AboutStage;
import annotation.UIAnnotation;
import com.fasterxml.jackson.databind.ObjectMapper;
import events.ViewEvent;
import handler.AutoSaveIA;
import i18n.I18N;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;
import settings.SettingsProperties;
import wrapper.IAJsonWrapper;
import json.IAJsonModule;
import jsonsaver.JsonSaver;
import keymap.Program;
import keymap.manager.IAShortcutManager;
import keymap.manager.SMFactory;
import keymap.model.ActionIA;
import model.ImageData;
import model.LoadedAnnotationData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import settings.SettingsStage;
import ui.Dialogs;
import ui.LoadingFolderDialog;
import util.SystemProperties;

import javax.swing.text.StyledEditorKit;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class IAMenuBarController {
    private static final Logger logger = LogManager.getLogger(IAMenuBarController.class);

    @FXML
    private MenuItem menuItemLoad;
    @FXML
    private MenuItem menuItemSave;
    @FXML
    public CheckMenuItem menuItemEditOnSelect;
    @FXML
    public MenuItem menuItemCopyAnnotation;
    @FXML
    public MenuItem menuItemDeleteAnnotation;
    @FXML
    public MenuItem menuItemNextImage;
    @FXML
    public MenuItem menuItemPreviousImage;
    @FXML
    public MenuItem menuItemNextVerifiedAnnotation;
    @FXML
    public MenuItem menuItemAddUserLabelToImage;
    @FXML
    public MenuItem menuItemTriggerSingleDetection;

    private final ImageAnnotationController iaController;

    public IAMenuBarController(ImageAnnotationController iaController) {
        this.iaController = iaController;
    }

    @FXML
    private void initialize() {
        IAShortcutManager shortcutManager = (IAShortcutManager) SMFactory.getShortcutManager(Program.IMAGE_ANNOTATION);
        bindMenuItems(shortcutManager);

        menuItemCopyAnnotation.disableProperty().bind(iaController.currentSelectedAnnotationProperty().isNull());
        menuItemDeleteAnnotation.disableProperty().bind(iaController.currentSelectedAnnotationProperty().isNull());


        boolean isEditOnSelect = Boolean.parseBoolean(SettingsProperties.getProperty(SettingsProperties.IMAGE_ANNOTATION_EDIT_ON_SELECT));
        menuItemEditOnSelect.setSelected(isEditOnSelect);
        menuItemEditOnSelect.selectedProperty().addListener(observable -> {
            SettingsProperties.setProperty(SettingsProperties.IMAGE_ANNOTATION_EDIT_ON_SELECT,
                    String.valueOf(menuItemEditOnSelect.isSelected()));
            SettingsProperties.saveProperty();
        });
    }

    /**
     * Needs to be called in {@link ImageAnnotationController#initialize}
     */
    void imageAnnotationControllerInitialize() {
        menuItemAddUserLabelToImage.disableProperty().bind(iaController.getTextFieldDefaultImageLabel().textProperty().isEmpty());
        menuItemAddUserLabelToImage.textProperty().bind(Bindings.createStringBinding(() -> {
            String defaultLabel = iaController.getTextFieldDefaultImageLabel().getText().strip();
            if (!defaultLabel.isBlank())
                defaultLabel = String.format("'%s' ", defaultLabel);
            return MessageFormat.format(I18N.getString("ia.menu.addLabelToImage"), defaultLabel);
        }, iaController.getTextFieldDefaultImageLabel().textProperty()));
    }

    private void bindMenuItems(IAShortcutManager shortcutManager) {
        menuItemLoad.acceleratorProperty().bind(shortcutManager.getKeyAction(ActionIA.OPEN_LOAD_FOLDER_WINDOW).keyCombinationProperty());
        menuItemSave.acceleratorProperty().bind(shortcutManager.getKeyAction(ActionIA.SAVE_ANNOTATIONS_DIALOG).keyCombinationProperty());
        menuItemCopyAnnotation.acceleratorProperty().bind(shortcutManager.getKeyAction(ActionIA.COPY_SELECTED_ANNOTATION).keyCombinationProperty());
        menuItemDeleteAnnotation.acceleratorProperty().bind(shortcutManager.getKeyAction(ActionIA.DELETE_ANNOTATION).keyCombinationProperty());
        menuItemNextImage.acceleratorProperty().bind(shortcutManager.getKeyAction(ActionIA.NEXT_IMAGE).keyCombinationProperty());
        menuItemPreviousImage.acceleratorProperty().bind(shortcutManager.getKeyAction(ActionIA.PREVIOUS_IMAGE).keyCombinationProperty());
        menuItemNextVerifiedAnnotation.acceleratorProperty().bind(shortcutManager.getKeyAction(ActionIA.JUMP_TO_NEXT_VERIFIED_ANNOTATION).keyCombinationProperty());
        menuItemAddUserLabelToImage.acceleratorProperty().bind(shortcutManager.getKeyAction(ActionIA.ADD_DEFAULT_IMAGE_LABEL).keyCombinationProperty());
        menuItemTriggerSingleDetection.acceleratorProperty().bind(shortcutManager.getKeyAction(ActionIA.TRIGGER_SINGLE_DETECTION).keyCombinationProperty());
    }

    @FXML
    void loadFolder() {
        Optional<LoadedAnnotationData> loadedData = LoadingFolderDialog.showAndWait(iaController);
        loadedData.ifPresent(data -> {
            iaController.setCurrentlyLoadedData(data);
            AutoSaveIA.stopAutoSaveScheduler();

            if (data.getImageDataList() != null) {
                iaController.getListViewImages().setItems(FXCollections.observableArrayList(data.getImageDataList()));
                iaController.getLabelLoadedImageFolderPath().setText(data.getImageFolder().normalize().toString());
                iaController.getListViewImages().requestFocus();
                iaController.getListViewImages().getSelectionModel().selectFirst();
                AutoSaveIA.startAutoSaveScheduler(iaController.getListViewImages().getItems(), data);
            } else iaController.getLabelLoadedImageFolderPath().setText("");
        });
    }

    @FXML
    void saveAnnotations() {
        final LoadedAnnotationData loadedData = iaController.getCurrentlyLoadedData();
        if (loadedData == null
                || iaController.getListViewImages().getItems() == null
                || iaController.getListViewImages().getItems().isEmpty()) {
            Dialogs.showInformation("Save Annotations", null, "No images are loaded. A folder with images must be loaded first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Annotations");

        // set initial directory
        Path initDir = loadedData.getJsonFile() != null ? loadedData.getJsonFile() : loadedData.getImageFolder();
        fileChooser.setInitialDirectory(initDir.getParent().toFile());

        // set init file name and file extensions
        String datetime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        fileChooser.setInitialFileName(String.format("images(%s)%s", datetime, JsonSaver.IA_FILE_EXTENSION));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Annotation JSON files (*.annotation.json)", "*.annotation.json"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"));

        File jsonFile = fileChooser.showSaveDialog(menuItemSave.getParentPopup().getOwnerWindow());
        if (jsonFile != null) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new IAJsonModule());
            try {
                // only get non-empty images
                List<ImageData> annotatedImages = iaController.getListViewImages().getItems()
                        .filtered(Predicate.not(ImageData::isEmpty));
                IAJsonWrapper jsonWrapper = new IAJsonWrapper(annotatedImages, loadedData);
                mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, jsonWrapper);
            } catch (IOException e) {
                logger.error("Serialization to JSON failed!", e);
                Dialogs.showExceptionDialog(e);
            }
        }
    }

    @FXML
    public void selectNextImage() {
        // init tracker before next image if necessary
        if (iaController.getTracker() != null && iaController.getToggleTrack().isSelected()
                && (iaController.getCheckBoxOneTimeTrack().isSelected()
                || (iaController.getCheckBoxSizeChangedInit().isSelected() && iaController.getTracker().wasLatestBoxChanged()))) {
            iaController.initializeTracker(iaController.getCurrentSelectedAnnotation());
        }

        int nextIndex = iaController.getListViewImages().getSelectionModel().getSelectedIndex() + 1;
        if (nextIndex < iaController.getListViewImages().getItems().size()) {
            iaController.getListViewImages().getSelectionModel().clearAndSelect(nextIndex);
            iaController.getListViewImages().scrollTo(nextIndex - 2);
        }
    }

    @FXML
    public void selectPreviousImage() {
        int prevIndex = iaController.getListViewImages().getSelectionModel().getSelectedIndex() - 1;
        // only allow backwards when tracker is not activated
        if (!iaController.getToggleTrack().isSelected() && prevIndex >= 0) {
            iaController.getListViewImages().getSelectionModel().clearAndSelect(prevIndex);
            iaController.getListViewImages().scrollTo(prevIndex - 2);
        }
    }

    @FXML
    void jumpToNextVerifiedBox() {
        int selectedIndex = iaController.getListViewImages().getSelectionModel().getSelectedIndex();
        int length = iaController.getListViewImages().getItems().size();

        search:
        for (int i = selectedIndex + 1; i < length; i++) {
            ImageData imageData = iaController.getListViewImages().getItems().get(i);
            for (UIAnnotation<?> annotationUI : imageData.getAnnotations()) {
                if (annotationUI.getAnnotationModel().isVerified()) {
                    iaController.getListViewImages().getSelectionModel().clearAndSelect(i);
                    break search;
                }
            }
        }
    }

    @FXML
    void copySelectedAnnotation() {
        if (iaController.currentSelectedAnnotationProperty().isNotNull().get()) {
            UIAnnotation<?> annotation = iaController.getCurrentSelectedAnnotation();
            int selectedIndex = iaController.getListViewImages().getSelectionModel().getSelectedIndex();
            ImageData imageData = iaController.getListViewImages().getItems().get(selectedIndex + 1);
            UIAnnotation<?> annotationCopy = annotation.copy(imageData.getWidth(), imageData.getHeight());
            imageData.addAnnotation(annotationCopy);
            if (iaController.getCheckBoxAutoNext().isSelected()) selectNextImage();
        }
    }

    @FXML
    void deleteAnnotation() {
        UIAnnotation<?> delAnnotation = iaController.getCurrentSelectedAnnotation();
        if (delAnnotation != null) {
            delAnnotation.removeFromPane();
            iaController.getCurrentSelectedImageData().removeAnnotation(delAnnotation);
            iaController.getListViewAnnotations().requestFocus();
            if (iaController.getCheckBoxAutoNext().isSelected()) selectNextImage();
        }
    }

    @FXML
    private void openSettings() {
        new SettingsStage().show();
    }

    @FXML
    private void backToMenu() {
        iaController.getImageAnnotationView().getStage().fireEvent(new ViewEvent(
                iaController.getImageAnnotationView().getStage(), ViewEvent.MENU));
    }

    @FXML
    private void exitProgram() {
        iaController.getImageAnnotationView().getStage().fireEvent(new WindowEvent(
                iaController.getImageAnnotationView().getStage(), WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    @FXML
    private void openAbout() {
        AboutStage.show(iaController.getImageAnnotationView().getApplication());
    }

    @FXML
    private void openLogFolder() {
        try {
            Desktop.getDesktop().open(new File(System.getProperty(SystemProperties.IA_LOG_PATH)));
        } catch (IOException | IllegalArgumentException e) {
            Dialogs.showError("Error", null, e.getMessage());
        }
    }

    @FXML
    private void addUserLabelToImage() {
        String defaultLabel = iaController.getTextFieldDefaultImageLabel().getText().strip();
        if (defaultLabel.isBlank()) return;
        List<ImageData> selectedImages = iaController.getListViewImages().getSelectionModel().getSelectedItems();
        for (ImageData imageData : selectedImages) {
            if (imageData.getImageClassification().getAdditionalLabels().contains(defaultLabel))
                imageData.getImageClassification().removeAdditionalLabel(defaultLabel);
            else imageData.getImageClassification().addAdditionalLabel(defaultLabel);
        }
        if (iaController.getCheckBoxAutoNext().isSelected()) selectNextImage();
    }

    @FXML
    public void singleDetection() {
        iaController.getDetectionServer().singleDetection(iaController.getCurrentSelectedImageData(),
                iaController.getListViewImages().getSelectionModel().selectedItemProperty(),
                iaController.getAnnotationPane());
    }

    public boolean editOnSelect() {
        return menuItemEditOnSelect.isSelected();
    }
}
