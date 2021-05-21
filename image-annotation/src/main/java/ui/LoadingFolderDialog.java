package ui;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import controller.ImageAnnotationController;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import json.IAJsonModule;
import jsonsaver.JsonSaver;
import model.ImageData;
import model.LoadedAnnotationData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.Utils;
import util.WindowsExplorerComparator;
import wrapper.IAJsonWrapper;
import wrapper.VRJsonWrapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class LoadingFolderDialog {
    private static final Logger logger = LogManager.getLogger(LoadingFolderDialog.class);

    @FXML
    private BorderPane borderPane;
    @FXML
    private BorderPane borderPaneBottom;

    @FXML
    private TextField textFieldImageFolder;
    @FXML
    private TextField textFieldMaskFolder;
    @FXML
    private TextField textFieldJsonPath;
    @FXML
    private CheckBox checkBoxOnyJsonImages;
    @FXML
    private CheckBox checkBockOnlyVRRanges;
    @FXML
    private Label labelDetectedVRJSON;

    @FXML
    private HBox rangesComponent;
    @FXML
    private LFDialogRanges rangesComponentController;

    @FXML
    private VBox infoText;
    @FXML
    private Hyperlink infoButton;
    @FXML
    private Button buttonOk;
    @FXML
    private Button buttonCancel;

    private final ObjectProperty<Path> jsonFile = new SimpleObjectProperty<>();
    private final ObjectProperty<Path> imageFolder = new SimpleObjectProperty<>();
    private final ObjectProperty<Path> maskFolder = new SimpleObjectProperty<>();
    //    private final ObjectProperty<JsonLoader> jsonLoader = new SimpleObjectProperty<>();
    private final ObjectProperty<Set<Integer>> rangeFrameNumbers = new SimpleObjectProperty<>();

    private LoadedAnnotationData loadedAnnotationData;
    private File initDir;

    private final Stage stage;
    private final ImageAnnotationController imageAnnotationController;

    private LoadingFolderDialog(Stage stage, ImageAnnotationController imageAnnotationController) {
        this.stage = stage;
        this.imageAnnotationController = imageAnnotationController;
    }

    @FXML
    private void initialize() {

        // hide list view as default and only display when VRJson is loaded
        setListViewVRRangesVisibility(false);
        checkBockOnlyVRRanges.selectedProperty().addListener(onlyVRRangesCheckedListener);

        maskFolder.bind(Bindings.createObjectBinding(() -> {
            if (textFieldMaskFolder.getText().isBlank())
                return null;
            return Path.of(textFieldMaskFolder.getText().strip());
        }));

        textFieldImageFolder.textProperty().addListener(imageFolderChangeListener);
        textFieldJsonPath.textProperty().addListener(jsonFileChangeListener);
        checkBoxOnyJsonImages.disableProperty().bind(checkBockOnlyVRRanges.selectedProperty());
        buttonOk.disableProperty().bind(textFieldImageFolder.textProperty().isEmpty());

        isExpanded.set(false);
    }

    @FXML
    private void onChooseImageFolder(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder");
        directoryChooser.setInitialDirectory(initDir);
        File folder = directoryChooser.showDialog(((Node) event.getTarget()).getScene().getWindow());
        if (folder != null) {
            textFieldImageFolder.clear();
            textFieldImageFolder.setText(folder.toString());
        }
    }

    @FXML
    private void onChooseMaskFolder(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder");
        directoryChooser.setInitialDirectory(initDir);
        File folder = directoryChooser.showDialog(((Node) event.getTarget()).getScene().getWindow());
        if (folder != null) {
            textFieldMaskFolder.clear();
            textFieldMaskFolder.setText(folder.toString());
        }
    }

    @FXML
    private void onChooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select JSON File");
        fileChooser.setInitialDirectory(initDir);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"));
        File file = fileChooser.showOpenDialog(((Node) event.getTarget()).getScene().getWindow());
        if (file != null) {
            textFieldJsonPath.clear();
            textFieldJsonPath.setText(file.toString());
        }
    }

    @FXML
    private void onInfoClicked(ActionEvent event) {
        isExpanded.set(!isExpanded.get());
    }

    private final BooleanProperty isExpanded = new SimpleBooleanProperty(this, "expanded", true) {
        @Override
        protected void invalidated() {
            infoButton.setText(get() ? "Hide Info" : "Show Info");
            infoButton.setVisited(false);
            borderPaneBottom.setCenter(get() ? infoText : null);
            stage.sizeToScene();
        }
    };

    private void setListViewVRRangesVisibility(boolean visible) {
        rangesComponentController.getTableViewVRRanges().getItems().clear();
        borderPaneBottom.setTop(visible ? rangesComponent : null);
        stage.sizeToScene();
    }

    private final ChangeListener<String> imageFolderChangeListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (newValue == null || newValue.isBlank())
                return;

            Path newFolder = Path.of(textFieldImageFolder.getText().strip());
            imageFolder.set(newFolder);
            initDir = newFolder.getParent().toFile();

            // only look for a json if text field is empty
            if (textFieldJsonPath.getText().isBlank()) {
                // check if a manual saved json file is in parent folder and display the latest one
                Optional<Path> manualJson = getLatestModifiedAnnotationFile(newFolder.getParent(), JsonSaver.IA_FILE_EXTENSION);
                if (manualJson.isPresent()) {
                    textFieldJsonPath.setText(manualJson.get().toAbsolutePath().normalize().toString());
                } else {
                    // check if a auto saved json file is in parent folder and display the latest one
                    Optional<Path> autoJson = getLatestModifiedAnnotationFile(newFolder.getParent(), JsonSaver.IA_AUTO_SAVE_FILE_EXTENSION);
                    autoJson.ifPresentOrElse(path -> textFieldJsonPath.setText(path.toAbsolutePath().normalize().toString()),
                            () -> textFieldJsonPath.clear());
                }
            }

            // folder must exist
            if (!Files.exists(newFolder)) {
                Dialogs.showWarning("Directory Not Existent", newFolder.toString(),
                        "This directory does not exist. Please selected a new image directory.",
                        event -> textFieldImageFolder.clear());
                return;
            }
            // folder must be a directory
            if (!Files.isDirectory(newFolder)) {
                Dialogs.showWarning("Directory Not Existent", newFolder.toString(),
                        "The selected file is not a directory. Please select a new image directory.",
                        event -> textFieldImageFolder.clear());
                return;
            }
            // folder should only hold images
            if (!onlyContainsImages(newFolder)) {
                Dialogs.showWarning("No Image Type", newFolder.toString(), "It was detected that not all files in the image directory are images. " +
                                "Please check this and remove all non-images from the folder, otherwise errors may occur.",
                        event -> textFieldImageFolder.clear());
            }
        }
    };

    private final ChangeListener<String> jsonFileChangeListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (newValue == null || newValue.isBlank()) {
//                jsonLoader.set(null);
                jsonFile.set(null);
                jsonWrapper.set(null);
                setListViewVRRangesVisibility(false);
                return;
            }

            Path newJson = Path.of(textFieldJsonPath.getText().strip());
            jsonFile.set(newJson);
            initDir = newJson.getParent().toFile();

            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new IAJsonModule());
                InjectableValues.Std injectableValues = new InjectableValues.Std();
                injectableValues.addValue("annotationPane", imageAnnotationController.getAnnotationPane());
                injectableValues.addValue("jsonFilePath", newJson);
                mapper.setInjectableValues(injectableValues);

                // Loads VR JSON without errors
                IAJsonWrapper json = mapper.readValue(newJson.toFile(), IAJsonWrapper.class);
                jsonWrapper.set(json);

            } catch (IOException e) {
                logger.error(e);
                Dialogs.showWarning("JSON Error", "Error occurred during JSON deserialization", e.getMessage());
            }
        }
    };

    private final ObjectProperty<IAJsonWrapper> jsonWrapper = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            if (get() == null) {
                checkBockOnlyVRRanges.setSelected(false);
            } else {
                if (get().getMetadata().getType().equals("VideoReview")) {
                    labelDetectedVRJSON.setVisible(get().getMetadata().getType().equals("VideoReview"));
                    rangesComponentController.loadVRJson(jsonFile.get());
                }
            }
        }
    };

    private final InvalidationListener onlyVRRangesCheckedListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable observable) {
            if (checkBockOnlyVRRanges.isSelected()) {
                // deselect 'Only Annotated Images' check box
                if (checkBoxOnyJsonImages.isSelected())
                    checkBoxOnyJsonImages.setSelected(false);
                // load ranges
                if (jsonWrapper.get() != null && jsonWrapper.get().getMetadata().getType().equals("VideoReview")) {
                    setListViewVRRangesVisibility(true);
                    rangeFrameNumbers.set(rangesComponentController.loadVRRanges(jsonFile.get()));
                }
            } else {
                setListViewVRRangesVisibility(false);
                rangeFrameNumbers.set(null);
            }
        }
    };

//    @FXML
//    private void onOkClicked(ActionEvent event) {
//        // load image data
//        List<ImageData> imageDataList;
//        if (jsonFile.get() != null && imageFolder.get() != null) { // if json is available
//
//            if (jsonLoader.get() != null && jsonLoader.get().isVideoReviewJson()) {
//                if (checkBockOnlyVRRanges.isSelected()) { // if ranges is selected
//                    if (rangeFrameNumbers.get() != null) {
//                        Set<Integer> framesNumbers = rangeFrameNumbers.get();
//                        List<ImageData> subset = loadImageSubsetFromFolder(imageFolder.get(), framesNumbers);
//                        List<ImageData> annotatedImages = loadJsonImages(true);
//
//                        // merge annotated images into subset
//                        Map<String, ImageData> mappingFilenameSubsetData = subset.stream().collect(Collectors.toMap(ImageData::getFilename, imageData -> imageData));
//                        // iterate over annotated images and replace with annotated data object if it is in subset
//                        for (ImageData data : annotatedImages) {
//                            if (mappingFilenameSubsetData.containsKey(data.getFilename())) {
//                                subset.remove(mappingFilenameSubsetData.get(data.getFilename()));
//                                subset.add(data);
//                            }
//                        }
//
//                        // sort and save
//                        imageDataList = subset.stream().sorted((o1, o2) -> WindowsExplorerComparator2.compare(o1.getFile(), o2.getFile())).collect(Collectors.toList());
//
//                        // when no ranges are present then take images from JSON only
//                    } else imageDataList = loadJsonImages(true);
//                    // if ranges not selected then take 'Only JSON' check box
//                } else imageDataList = loadJsonImages(checkBoxOnyJsonImages.isSelected());
//                // if no VR JSON then take 'Only JSON' check box
//            } else imageDataList = loadJsonImages(checkBoxOnyJsonImages.isSelected());
//
//        } else if (jsonFile.get() == null && imageFolder.get() != null) { // if only image folder is defined
//            imageDataList = loadAllImagesFromFolder(imageFolder.get());
//
//        } else throw new RuntimeException("Could not load images because there was no image folder defined");
//
//        if (textFieldMaskFolder.getText().isBlank()) {
//            loadedAnnotationData = new LoadedAnnotationData(jsonFile.get(), imageFolder.get(), imageDataList);
//        } else {
//            loadedAnnotationData = new LoadedAnnotationData(jsonFile.get(), imageFolder.get(), imageDataList, maskFolder.get());
//        }
//
//        ((Stage) buttonOk.getScene().getWindow()).close();
//    }

    @FXML
    private void onOkClicked(ActionEvent event) {
        // load image data
        List<ImageData> imageDataList;

        // if only image folder is defined
        if (jsonFile.get() == null && imageFolder.get() != null) {
            imageDataList = loadAllImagesFromFolder(imageFolder.get());
        }
        // if VR JSON loaded, ranges is selected and ranges exists
        else if (jsonFile.get() != null && imageFolder.get() != null && labelDetectedVRJSON.isVisible() && checkBockOnlyVRRanges.isSelected() && rangeFrameNumbers.get() != null) {
            Set<Integer> framesNumbers = rangeFrameNumbers.get();
            List<ImageData> subset = loadImageSubsetFromFolder(imageFolder.get(), framesNumbers);
//            List<ImageData> annotatedImages = loadJsonImages(true);
            List<ImageData> annotatedImages = jsonWrapper.get().getImages();

            // merge annotated images into subset
            Map<String, ImageData> mappingFilenameSubsetData = subset.stream().collect(Collectors.toMap(ImageData::getFilename, imageData -> imageData));
            // iterate over annotated images and replace with annotated data object if it is in subset
            for (ImageData data : annotatedImages) {
                if (mappingFilenameSubsetData.containsKey(data.getFilename())) {
                    subset.remove(mappingFilenameSubsetData.get(data.getFilename()));
                    subset.add(data);
                }
            }

            // sort and save
            imageDataList = subset.stream().sorted((o1, o2) -> WindowsExplorerComparator.compare(o1.getFile(), o2.getFile())).collect(Collectors.toList());
        }
        // if VR JSON loaded, ranges selected and NO ranges exists, use ImageData from VR JSON
        else if (jsonFile.get() != null && imageFolder.get() != null && labelDetectedVRJSON.isVisible()) {
            VRJsonWrapper vrJson = rangesComponentController.getVrJsonWrapper();
            imageDataList = vrJson != null ? vrJson.getImages() : List.of();
        }
        // if only annotated images should be loaded
        else if (jsonFile.get() != null && imageFolder.get() != null && checkBoxOnyJsonImages.isSelected()) {
            IAJsonWrapper iaJson = jsonWrapper.get();
            imageDataList = iaJson != null ? iaJson.getImages() : List.of();
        }
        // default: load all images with annotations
        else if (jsonFile.get() != null && imageFolder.get() != null) {
            imageDataList = loadAllImagesWithAnnotations(jsonWrapper.get().getImages());
        } else {
            throw logger.throwing(new RuntimeException("Could not load images"));
        }

        if (textFieldMaskFolder.getText().isBlank()) {
            loadedAnnotationData = new LoadedAnnotationData(jsonFile.get(), imageFolder.get(), imageDataList);
        } else {
            loadedAnnotationData = new LoadedAnnotationData(jsonFile.get(), imageFolder.get(), imageDataList, maskFolder.get());
        }

        ((Stage) buttonOk.getScene().getWindow()).close();
    }

    @FXML
    private void onCancelClicked(ActionEvent event) {
        loadedAnnotationData = null;
        ((Stage) buttonCancel.getScene().getWindow()).close();
    }

    private List<ImageData> loadAllImagesWithAnnotations(List<ImageData> jsonImages) {
        // load all files in folder as string
        List<String> allImages;
        try {
            allImages = Files.list(imageFolder.get()).parallel()
                    .sorted(WindowsExplorerComparator::compare)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.throwing(e);
            allImages = new ArrayList<>();
        }

        // merge annotated images with all images in folder
        List<ImageData> images = new ArrayList<>();
        Map<String, ImageData> mappingFilenameData = jsonImages.stream().collect(Collectors.toMap(ImageData::getFilename, imageData -> imageData));
        for (String filename : allImages) {
            if (mappingFilenameData.containsKey(filename))
                images.add(mappingFilenameData.get(filename));
            else images.add(new ImageData(imageFolder.get().resolve(filename)));
        }
        return images;
    }

    /**
     * Load a folder with images
     *
     * @param imageFolder directory with images inside
     * @return list of image data objects
     */
    private List<ImageData> loadAllImagesFromFolder(Path imageFolder) {
        try {
            return Files.list(imageFolder)
                    .sorted(WindowsExplorerComparator::compare)
                    .map(ImageData::new)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return FXCollections.emptyObservableList();
        }
    }

    private List<ImageData> loadImageSubsetFromFolder(Path imageFolder, Set<Integer> frameNumbers) {
        try {
            return Files.list(imageFolder)
                    .parallel()
                    .filter(path -> frameNumbers.contains(Utils.getFrameNumber(path.getFileName().toString())))
                    .sorted(WindowsExplorerComparator::compare)
                    .map(ImageData::new)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return FXCollections.emptyObservableList();
        }
    }

    /**
     * Checks if the directory only contains images based on the MIME type.
     *
     * @param imageFolder the folder that should be checked
     * @return <code>true</code> if the folder only contains image files, otherwise <code>false</code>
     */
    public static boolean onlyContainsImages(Path imageFolder) {
        try {
            return Files.list(imageFolder)
                    .map(path -> {
                        try {
                            return Files.probeContentType(path);
                        } catch (IOException e) {
                            return "";
                        }
                    })
                    .allMatch(s -> s.startsWith("image/"));
        } catch (IOException | NullPointerException e) {
            logger.error("Folder content checking error. Not only images?", e);
            return false;
        }
    }

    private Optional<Path> getLatestModifiedAnnotationFile(Path folder, String fileExtension) {
        try {
            return Files.list(folder)
                    .filter(path -> path.getFileName().toString().matches(".*\\.annotation(\\.auto)?\\.json"))
                    .max(Comparator.comparingLong(o -> o.toFile().lastModified()));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public Optional<LoadedAnnotationData> getResult() {
        return Optional.ofNullable(loadedAnnotationData);
    }

    public static Optional<LoadedAnnotationData> showAndWait(ImageAnnotationController imageAnnotationController) {
        Stage stage = new Stage();
        stage.setTitle("Load Folder");
        stage.initModality(Modality.APPLICATION_MODAL);

        InputStream iconIS = LoadingFolderDialog.class.getClassLoader().getResourceAsStream("logo_polygon.png");
        if (iconIS != null) stage.getIcons().add(new Image(iconIS));

        FXMLLoader loader;
        try {
            loader = new FXMLLoader(LoadingFolderDialog.class.getResource("/fxml/LoadingFolderDialog.fxml"));
            loader.setControllerFactory(param -> {
                if (param == LoadingFolderDialog.class)
                    return new LoadingFolderDialog(stage, imageAnnotationController);
                else if (param == LFDialogRanges.class)
                    return new LFDialogRanges(imageAnnotationController);
                return null;
            });
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            logger.error(e);
            return Optional.empty();
        }

        stage.centerOnScreen();
        stage.sizeToScene();
        stage.showAndWait();

        return ((LoadingFolderDialog) loader.getController()).getResult();
    }
}
