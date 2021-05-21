package controller;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import json.VRJsonModule;
import model.LoadedReviewData;
import model.SuperframeData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ui.Dialogs;
import util.FrameExtraction;
import util.MaskedTextField;
import util.SupportedVideoFiles;
import util.TimeFormatter;
import videoplayer.VideoInfo2;
import videoreview.FrameSizeCheckException;
import wrapper.VRJsonWrapper;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class LoadingVideoDialog {
    private static final Logger logger = LogManager.getLogger(LoadingVideoDialog.class);

    @FXML
    private TextField textFieldJsonFile;
    @FXML
    private Button buttonChooseJsonFile;

    // Video
    @FXML
    private TextField textFieldVideoFile;
    @FXML
    private Button buttonChooseVideoFile;
    @FXML
    private Label labelFps;
    @FXML
    private Label labelResolution;
    @FXML
    private Label labelVideoLength;
    @FXML
    private Label labelFramesSize;

    // Data
    @FXML
    private TextField textFieldImageFolder;
    @FXML
    private Button buttonChooseImageFolder;
    @FXML
    private ListView<Integer> listViewFreezes;
    @FXML
    private Button buttonVideoExtraction;
    @FXML
    private Button buttonDeleteFreeze;
    @FXML
    private TextField textFieldFrameNumber;
    @FXML
    private Button buttonAddFrameNumber;
    @FXML
    private MaskedTextField textFieldMaskedTimestamp;
    @FXML
    private Button buttonAddTimestamp;

    @FXML
    private Label labelProgressMessage;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Button buttonOk;
    @FXML
    private Button buttonCancel;

    private final ObjectProperty<VideoInfo2> videoInfo = new SimpleObjectProperty<>();
    private LoadedReviewData loadedReviewData;
    private File initDir;

    private LoadDataTask loadDataTask;
    private ImageFolderChecker imageFolderChecker;
    private final AtomicBoolean cancelImageFolderChecker = new AtomicBoolean(false);
    private Timeline timelineProgressMessage;

    private final Stage stage;
    private final VideoReviewController vrController;

    public LoadingVideoDialog(Stage stage, VideoReviewController vrController) {
        this.stage = stage;
        this.vrController = vrController;
    }

    @FXML
    private void initialize() {
        timelineProgressMessage = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            labelProgressMessage.setVisible(false);
            labelProgressMessage.setText("");
        }));

        buttonChooseVideoFile.disableProperty().bind(progressBar.progressProperty().isNotEqualTo(0));
        buttonChooseJsonFile.disableProperty().bind(progressBar.progressProperty().isNotEqualTo(0));
        textFieldImageFolder.disableProperty().bind(textFieldVideoFile.textProperty().isEmpty());
        buttonChooseImageFolder.disableProperty().bind(textFieldVideoFile.textProperty().isEmpty().or(progressBar.progressProperty().isNotEqualTo(0)));
        listViewFreezes.disableProperty().bind(textFieldImageFolder.textProperty().isEmpty());
        buttonVideoExtraction.disableProperty().bind(textFieldVideoFile.textProperty().isEmpty());
        buttonDeleteFreeze.disableProperty().bind(textFieldImageFolder.textProperty().isEmpty().or(textFieldJsonFile.textProperty().isNotEmpty()));
        textFieldMaskedTimestamp.disableProperty().bind(textFieldImageFolder.textProperty().isEmpty().or(textFieldJsonFile.textProperty().isNotEmpty()));
        buttonAddTimestamp.disableProperty().bind(textFieldImageFolder.textProperty().isEmpty().or(textFieldJsonFile.textProperty().isNotEmpty()));
        textFieldFrameNumber.disableProperty().bind(textFieldImageFolder.textProperty().isEmpty().or(textFieldJsonFile.textProperty().isNotEmpty()));
        buttonAddFrameNumber.disableProperty().bind(textFieldImageFolder.textProperty().isEmpty().or(textFieldJsonFile.textProperty().isNotEmpty()));

        buttonOk.disableProperty().bind(progressBar.progressProperty().isNotEqualTo(0)
                .or(textFieldImageFolder.textProperty().isEmpty())
                .or(Bindings.size(listViewFreezes.getItems()).lessThanOrEqualTo(0))
                .or(textFieldJsonFile.textProperty().isEmpty().and(textFieldVideoFile.textProperty().isEmpty()))
                .or(timelineProgressMessage.statusProperty().isEqualTo(Animation.Status.RUNNING)));

        textFieldVideoFile.textProperty().addListener(videoFileChangeListener);
        textFieldJsonFile.textProperty().addListener(jsonFileChangeListener);
        textFieldImageFolder.textProperty().addListener(imageFolderChangeListener);

        labelFps.visibleProperty().bind(videoInfo.isNull().not());
        labelFps.textProperty().bind(Bindings.createStringBinding(() -> {
            if (videoInfo.get() != null && videoInfo.get().getFPS().isPresent()) {
                double fps = videoInfo.get().getFPS().get();
                // check if fps is an integer
                if ((fps % 1) == 0) return String.valueOf((int) fps);
                return String.valueOf(fps);
            }
            return "N/A";
        }, videoInfo));

        labelResolution.visibleProperty().bind(videoInfo.isNotNull());
        labelResolution.textProperty().bind(Bindings.createStringBinding(() -> {
            if (videoInfo.get() != null && videoInfo.get().getResolutionWidth().isPresent() && videoInfo.get().getResolutionHeight().isPresent())
                return videoInfo.get().getResolutionWidth().get() + " x " + videoInfo.get().getResolutionHeight().get();
            return "N/A";
        }, videoInfo));

        labelFramesSize.visibleProperty().bind(folderFrameSize.isNotNull());
        labelFramesSize.textProperty().bind(Bindings.createStringBinding(() -> {
            if (folderFrameSize.isNotNull().get())
                return folderFrameSize.get().frameWidth + " x " + folderFrameSize.get().frameHeight;
            return "N/A";
        }, folderFrameSize));

        labelVideoLength.visibleProperty().bind(videoInfo.isNull().not());
        labelVideoLength.textProperty().bind(Bindings.createStringBinding(() -> {
            if (videoInfo.get() != null && videoInfo.get().getVideoDuration().isPresent())
                return TimeFormatter.formatTimeDetailed(videoInfo.get().getVideoDuration().get().doubleValue());
            return "N/A";
        }, videoInfo));

        // only allow integer strings
        textFieldFrameNumber.setTextFormatter(new TextFormatter<Double>(change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty())
                return change;
            try {
                Integer.parseInt(newText);
                return change;
            } catch (NumberFormatException e) {
                return null;
            }
        }));

        stage.setOnCloseRequest(event -> {
            loadedReviewData = null;
            timelineProgressMessage.stop();
            if (imageFolderChecker != null) {
                cancelImageFolderChecker.set(true);
                imageFolderChecker.cancel();
            }
            if (loadDataTask != null) {
                loadDataTask.cancel();
            }
        });
    }

    private final ChangeListener<String> videoFileChangeListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldVideo, String newVideo) {
            if (newVideo == null || newVideo.isBlank())
                return;
            Path videoFile = Path.of(newVideo);
            initDir = videoFile.getParent().toFile();

            videoInfo.set(new VideoInfo2(newVideo));

            // find json file
            Optional<Path> jsonFilePath = Optional.empty();
            try {
                jsonFilePath = Files.list(videoFile.getParent())
                        .filter(path -> path.getFileName().toString().matches(".*\\.review(\\.auto)?\\.json"))
                        .max(Comparator.comparingLong(o -> o.toFile().lastModified()));

                jsonFilePath.ifPresent(path -> textFieldJsonFile.setText(path.toString()));
            } catch (IOException e) {
                logger.error("Can not open JSON in folder " + videoFile.getParent().toString(), e);
            }

            if (jsonFilePath.isPresent())
                return;

            // find default frames folder
            try {
                Files.list(videoFile.getParent())
                        .filter(path -> path.getFileName().toString().equalsIgnoreCase("frames"))
                        .findFirst()
                        .ifPresent(folder -> {
                            if (Files.isDirectory(folder))
                                textFieldImageFolder.setText(folder.toString());
                        });
            } catch (IOException e) {
                logger.error("Can not open " + videoFile.getParent().toString(), e);
            }
        }
    };

    private final ChangeListener<String> jsonFileChangeListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldPath, String newPath) {
            if (newPath == null || newPath.isBlank()) {
                jsonFileWrapper.set(null);
                return;
            }

            Path jsonFile = Path.of(newPath.strip());
            initDir = jsonFile.getParent().toFile();

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new VRJsonModule());

            // inject annotation pane and image folder path into deserializer for bounding box creation
            InjectableValues.Std injectableValues = new InjectableValues.Std();
            injectableValues.addValue("annotationPane", vrController.getFrameViewerMode().getAnnotationPane());
            injectableValues.addValue("jsonFilePath", jsonFile);
            mapper.setInjectableValues(injectableValues);

            // read json
            try {
                VRJsonWrapper json = mapper.readValue(jsonFile.toFile(), VRJsonWrapper.class);
                jsonFileWrapper.set(json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private final ObjectProperty<VRJsonWrapper> jsonFileWrapper = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            VRJsonWrapper json = get();
            if (json == null)
                return;

            listViewFreezes.getItems().clear();
            listViewFreezes.getItems().addAll(json.getMetadata().getSuperframes());

            Path jsonFilePath = Path.of(textFieldJsonFile.getText().strip());
            Path framesFolder = jsonFilePath.getParent().resolve(json.getMetadata().getImageFolder());
            textFieldImageFolder.setText(framesFolder.toString());
        }
    };

    private final ChangeListener<String> imageFolderChangeListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (newValue == null || newValue.isBlank())
                return;

            Path imageFolder = Path.of(newValue.strip());
            initDir = imageFolder.toFile(); // set new init folder

            // start image folder checking task
            cancelImageFolderChecker.set(false);
            imageFolderChecker = new ImageFolderChecker(imageFolder, cancelImageFolderChecker);

            imageFolderChecker.setOnRunning(event -> {
                labelProgressMessage.setVisible(true);
                labelProgressMessage.textProperty().bind(imageFolderChecker.messageProperty());
                progressBar.progressProperty().bind(imageFolderChecker.progressProperty());
            });

            imageFolderChecker.setOnSucceeded(event -> {
                FrameSize value = imageFolderChecker.getValue();
                if (value != null)
                    folderFrameSize.set(value);
                resetProgress();
            });

            imageFolderChecker.setOnFailed(event -> {
                Dialogs.showExceptionDialog("Image Folder", (Exception) imageFolderChecker.getException());
                resetProgress();
            });

            imageFolderChecker.setOnCancelled(event -> resetProgress());

            // start thread
            Thread t = new Thread(imageFolderChecker);
            t.setName("Folder Checking");
            t.start();
        }
    };

    private final ObjectProperty<FrameSize> folderFrameSize = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            if (videoInfo.isNotNull().get()) {
                int videoWidth = videoInfo.get().getResolutionWidth().orElse(-1);
                int videoHeight = videoInfo.get().getResolutionHeight().orElse(-1);
                if (get().frameWidth != videoWidth || get().frameHeight != videoHeight) {
                    Dialogs.showInformation("Resolution mismatch", null,
                            "Video resolution and frame dimensions are not the same. " +
                                    "We will take frame dimensions for annotations.");
                }
            }
        }
    };

    @FXML
    private void onChooseVideoFile(ActionEvent event) {
        showVideoChooser(((Node) event.getTarget()).getScene().getWindow())
                .ifPresent(path -> textFieldVideoFile.setText(path.toString()));
    }

    @FXML
    private void onChooseJsonFile(ActionEvent event) {
        showJsonChooser(((Node) event.getTarget()).getScene().getWindow())
                .ifPresent(path -> textFieldJsonFile.setText(path.toString()));
    }

    @FXML
    private void onChooseImageFolder(ActionEvent event) {
        showImageFolderChooser(((Node) event.getTarget()).getScene().getWindow())
                .ifPresent(path -> textFieldImageFolder.setText(path.toString()));
    }

    @FXML
    private void onVideoExtraction() {
        ExtractionDialog.showAndWait(Path.of(textFieldVideoFile.getText()))
        .ifPresent(superframes -> {
            listViewFreezes.getItems().clear();
            listViewFreezes.getItems().addAll(superframes);
        });
    }

    @FXML
    private void onFreezeKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE && !listViewFreezes.getItems().isEmpty() && textFieldJsonFile.getText().isEmpty()) {
            Integer selectedFreeze = listViewFreezes.getSelectionModel().getSelectedItem();
            if (selectedFreeze != null) {
                listViewFreezes.getItems().remove(selectedFreeze);
            }
        }
    }

    /**
     * Delete selected freeze number when button pressed.
     */
    @FXML
    private void onDeleteFreezeClicked(ActionEvent event) {
        Integer selectedFreeze = listViewFreezes.getSelectionModel().getSelectedItem();
        if (selectedFreeze != null)
            listViewFreezes.getItems().remove(selectedFreeze);
    }

    @FXML
    private void onOkClicked(ActionEvent event) {
        if (jsonFileWrapper.get() == null) {
            loadDataTask = new LoadDataTask(
                    Path.of(textFieldVideoFile.getText()),
                    Path.of(textFieldImageFolder.getText()),
                    videoInfo.get(),
                    folderFrameSize.get(),
                    listViewFreezes.getItems());
        } else {
            loadDataTask = new LoadDataTask(
                    Path.of(textFieldVideoFile.getText()),
                    Path.of(textFieldImageFolder.getText()),
                    videoInfo.get(),
                    folderFrameSize.get(),
                    Path.of(textFieldJsonFile.getText()),
                    jsonFileWrapper.get());
        }

        loadDataTask.setOnRunning(e -> {
            labelProgressMessage.setVisible(true);
            labelProgressMessage.textProperty().bind(loadDataTask.messageProperty());
            progressBar.setProgress(-1);
        });
        loadDataTask.setOnSucceeded(e -> {
            loadedReviewData = loadDataTask.getValue();
            resetProgress();
            stage.close();
        });
        loadDataTask.setOnCancelled(e -> {
            resetProgress();
            loadedReviewData = null;
            Dialogs.showError("Cancel", null, "Loading thread was canceled during data loading!");
        });
        loadDataTask.setOnFailed(e -> {
            resetProgress();
            loadedReviewData = null;
            Dialogs.showError("Failed", null, "Loading thread failed during data loading!");
        });

        Thread t = new Thread(loadDataTask);
        t.setName("Loading Data");
        t.start();
    }

    @FXML
    private void onCancelOkClicked(ActionEvent event) {
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    private boolean isFreezeNumberAbsent(int freezeNumber) {
        for (Integer number : listViewFreezes.getItems()) {
            if (number == freezeNumber)
                return false;
        }
        return true;
    }

    /**
     * Convert timestamp to frame number and add to list
     */
    @FXML
    private void addTimestampToListView() {
        if (textFieldMaskedTimestamp.getPlainText().length() == 9 && videoInfo.get() != null && videoInfo.get().getFPS().isPresent()) {
            double seconds = 0.0;
            String[] timestamp = textFieldMaskedTimestamp.getText().split(":");
            seconds += Integer.parseInt(timestamp[0]) * 60.0 * 60.0; // hours
            seconds += Integer.parseInt(timestamp[1]) * 60.0; // minutes
            seconds += Integer.parseInt(timestamp[2]); // seconds
            seconds += Integer.parseInt(timestamp[3]) / 1000.0; // milliseconds
            Integer freezeNumber = (int) Math.ceil(seconds * videoInfo.get().getFPS().get());
            if (isFreezeNumberAbsent(freezeNumber) && (videoInfo.get().getVideoDuration().isEmpty() || (seconds * 1000) <= videoInfo.get().getVideoDuration().get())) {
                listViewFreezes.getItems().add(freezeNumber);
                listViewFreezes.getItems().sort(Integer::compare);
                listViewFreezes.getSelectionModel().select(freezeNumber);
                listViewFreezes.scrollTo(freezeNumber);
            }
            textFieldMaskedTimestamp.clear();
            textFieldMaskedTimestamp.setPlainText("00");
            textFieldMaskedTimestamp.positionCaret(2);
        }
    }

    /**
     * Adding a freeze number to list
     */
    @FXML
    private void addFrameNumberToListView() {
        if (!textFieldFrameNumber.getText().isEmpty() && videoInfo.get() != null) {
            Integer newNumber = Integer.parseInt(textFieldFrameNumber.getText());
            if (isFreezeNumberAbsent(newNumber) && (videoInfo.get() == null
                    || videoInfo.get().getLastFrameNumber().isEmpty()
                    || newNumber <= videoInfo.get().getLastFrameNumber().get())) {
                listViewFreezes.getItems().add(newNumber);
                listViewFreezes.getItems().sort(Integer::compare);
                listViewFreezes.getSelectionModel().select(newNumber);
                listViewFreezes.scrollTo(newNumber);
            }
            textFieldFrameNumber.clear();
        }
    }

    private void resetProgress() {
        labelProgressMessage.textProperty().unbind();
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
        timelineProgressMessage.play();
        imageFolderChecker = null;
        loadDataTask = null;
    }

    private Optional<Path> showJsonChooser(Window ownerWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select JSON File");
        if (initDir != null) fileChooser.setInitialDirectory(initDir);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"));
        File jsonFile = fileChooser.showOpenDialog(ownerWindow);
        if (jsonFile != null) return Optional.of(jsonFile.toPath());
        return Optional.empty();
    }

    private Optional<Path> showVideoChooser(Window ownerWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Video File");
        if (initDir != null) fileChooser.setInitialDirectory(initDir);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video files", SupportedVideoFiles.videoFiles));
        File videoFile = fileChooser.showOpenDialog(ownerWindow);
        if (videoFile != null) return Optional.of(videoFile.toPath());
        return Optional.empty();
    }

    private Optional<Path> showImageFolderChooser(Window ownerWindow) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Image Folder");
        if (initDir != null) directoryChooser.setInitialDirectory(initDir);
        File folder = directoryChooser.showDialog(ownerWindow);
        if (folder != null) return Optional.of(folder.toPath());
        return Optional.empty();
    }

    public Optional<LoadedReviewData> getResults() {
        return Optional.ofNullable(loadedReviewData);
    }

    public static Optional<LoadedReviewData> showAndWait(final VideoReviewController videoReviewController) {
        Stage stage = new Stage();
        stage.setTitle("Load Video");
        stage.initModality(Modality.APPLICATION_MODAL);

        InputStream iconIS = LoadingVideoDialog.class.getClassLoader().getResourceAsStream("logo_polygon.png");
        if (iconIS != null)
            stage.getIcons().add(new Image(iconIS));

        FXMLLoader loader;
        try {
            loader = new FXMLLoader(LoadingVideoDialog.class.getResource("/fxml/LoadingVideoDialog.fxml"));
            loader.setControllerFactory(param -> new LoadingVideoDialog(stage, videoReviewController));
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            logger.error(e);
            return Optional.empty();
        }

        stage.sizeToScene();
        stage.centerOnScreen();
        stage.showAndWait();

        return ((LoadingVideoDialog) loader.getController()).getResults();
    }

    /**
     * Checks that only images are within the folder and acquires the a unique file extension.
     */
    private static class ImageFolderChecker extends Task<FrameSize> {

        private final Path folder;
        private final AtomicBoolean cancelImageFolderChecker;

        private ImageFolderChecker(Path imageFolder, AtomicBoolean cancelImageFolderChecker) {
            if (imageFolder == null)
                throw new RuntimeException("Path to the folder is needed");
            folder = imageFolder;
            this.cancelImageFolderChecker = cancelImageFolderChecker;
        }

        // TODO: Optimize with parallel stream like loading?
        @Override
        protected FrameSize call() throws FrameSizeCheckException {
            int fileCount = Objects.requireNonNull(folder.toFile().list()).length;
            updateMessage("Checking folder...");
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
                int count = 0;
                int imageWidth = -1;
                int imageHeight = -1;

                // We need to sample some frame dimension because it can differ from video resolution.
                // Frame size is needed so that it will not load from disk during "fast" class assignment.
                Platform.runLater(() -> System.out.println("File Count: " + fileCount + " -> " + (3 + (int) (fileCount * 0.0004))));
                List<Integer> randomSamples = ThreadLocalRandom.current().ints(0, fileCount).distinct()
                        .limit(3 + (int) (fileCount * 0.0004))
                        .sorted().boxed()
                        .collect(Collectors.toList());

                for (Path file : stream) {
                    if (cancelImageFolderChecker.get()) {
                        updateMessage("Task Canceled!");
                        return null;
                    }

                    // check that folder only contains images
                    String contentType = Files.probeContentType(file);
                    if (contentType == null || !contentType.startsWith("image/"))
                        throw new FrameSizeCheckException("Found a non-image file!");

                    // check if filename is a number => throws exception if not
                    String[] filename = file.getFileName().toString().split("\\.");
                    Integer.parseInt(filename[0]);

                    // check image size if count was sampled
                    if (randomSamples.size() > 0 && randomSamples.get(0) == count) {
                        int[] imageSize = readImageSize(file);
                        if (imageWidth == -1 || imageHeight == -1) {
                            imageWidth = imageSize[0];
                            imageHeight = imageSize[1];
                        } else if (imageSize[0] != imageWidth || imageSize[1] != imageHeight)
                            throw new FrameSizeCheckException("Frames got different dimensions");
                        randomSamples.remove(0);
                    }

                    updateProgress(++count, fileCount);
                }
                updateMessage("Finished with no errors!");
                return new FrameSize(imageWidth, imageHeight);

            } catch (NumberFormatException e) {
                throw new FrameSizeCheckException("Found a non-numbered filename!");
            } catch (IOException e) {
                updateMessage("I/O Error!");
                return null;
            }
        }

        private int[] readImageSize(final Path image) throws FrameSizeCheckException {
            try (ImageInputStream in = ImageIO.createImageInputStream(Files.newInputStream(image))) {
                final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
                if (readers.hasNext()) {
                    ImageReader reader = readers.next();
                    try {
                        reader.setInput(in);
                        return new int[]{reader.getWidth(0), reader.getHeight(0)};
                    } finally {
                        reader.dispose();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            throw new FrameSizeCheckException("Could not read dimensions for image: " +
                    image.toAbsolutePath().normalize().toString());
        }
    }

    /**
     * Loads the remaining data when OK button is clicked.
     */
    private static class LoadDataTask extends Task<LoadedReviewData> {

        private final Path jsonFile;
        private final Path videoFile;
        private final Path imageFolder;
        private final VideoInfo2 videoInfo;
        private final FrameSize frameSize;
        private final List<Integer> superframeNumbers;
        private final VRJsonWrapper jsonWrapper;

        private LoadDataTask(Path videoFile, Path imageFolder, VideoInfo2 videoInfo, FrameSize frameSize,
                             Path jsonFile, VRJsonWrapper jsonWrapper) {
            this(videoFile, imageFolder, videoInfo, frameSize, jsonFile, null, jsonWrapper);
        }

        private LoadDataTask(Path videoFile, Path imageFolder, VideoInfo2 videoInfo, FrameSize frameSize,
                             List<Integer> superframeNumbers) {
            this(videoFile, imageFolder, videoInfo, frameSize, null, superframeNumbers, null);
        }

        private LoadDataTask(Path videoFile, Path imageFolder, VideoInfo2 videoInfo, FrameSize frameSize,
                             Path jsonFile, List<Integer> superframeNumbers, VRJsonWrapper jsonWrapper) {
            this.videoFile = videoFile;
            this.imageFolder = imageFolder;
            this.videoInfo = videoInfo;
            this.frameSize = frameSize;
            this.jsonFile = jsonFile;
            this.superframeNumbers = superframeNumbers;
            this.jsonWrapper = jsonWrapper;
        }

        @Override
        protected LoadedReviewData call() {
            // load frame files for frame loader
            String[] allFrames;
            try {
                updateMessage("Loading frames...");
                allFrames = Files.list(imageFolder)
                        .parallel()
                        .filter(path -> !path.getFileName().toString().startsWith("."))
                        .map(path -> path.getFileName().toString())
                        .sorted(Comparator.comparingInt(value -> Integer.parseInt(value.substring(0, value.lastIndexOf('.')))))
                        .toArray(String[]::new);
            } catch (IOException e) {
                updateMessage("I/O Error!");
                return null;
            }

            // load superframes
            if (jsonWrapper == null) {
                updateMessage("Load Superframes");
                List<SuperframeData> superframes = superframeNumbers.stream()
                        .sorted(Integer::compareTo)
                        .map(SuperframeData::new)
                        .collect(Collectors.toList());
                updateMessage("Done!");
                return new LoadedReviewData(jsonFile, videoFile, imageFolder, videoInfo, frameSize.frameWidth, frameSize.frameHeight, allFrames, superframes);
            }

            updateMessage("Done!");
            return new LoadedReviewData(jsonFile, videoFile, imageFolder, videoInfo, frameSize.frameWidth, frameSize.frameHeight, allFrames, jsonWrapper);
        }
    }


    /**
     * Wrapper for frame size checking.
     */
    private static class FrameSize {
        private final int frameWidth;
        private final int frameHeight;

        private FrameSize(int width, int height) {
            frameWidth = width;
            frameHeight = height;
        }
    }
}
