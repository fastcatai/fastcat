package controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ui.Dialogs;
import util.FrameExtraction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExtractionDialog {
    private static final Logger logger = LogManager.getLogger(ExtractionDialog.class);

    @FXML
    private TextField textFieldFolder;
    @FXML
    private RadioButton rbFrameExtraction;
    @FXML
    private RadioButton rbFreezeDetection;
    @FXML
    private RadioButton rbBoth;
    @FXML
    private ToggleGroup toggleGroup;
    @FXML
    private Button buttonStart;
    @FXML
    private ProgressBar progressBar;

    private final Path videoFile;

    public ExtractionDialog(Path videoFile) {
        this.videoFile = videoFile;
    }

    @FXML
    private void initialize() {
        buttonStart.disableProperty().bind(textFieldFolder.textProperty().isEmpty()
                .or(toggleGroup.selectedToggleProperty().isNull()));
    }

    public void onChooseImageFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setInitialDirectory(videoFile.getParent().toFile());
        File folder = chooser.showDialog(textFieldFolder.getScene().getWindow());
        if (folder != null) {
            textFieldFolder.setText(folder.getPath());
        }
    }

    private ExecutorService executorService;

    public void onVideoExtraction() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
            executorService = null;
            buttonStart.setText("Start");
            return;
        }

        if (textFieldFolder.getText().isBlank()
                || toggleGroup.getSelectedToggle() == null)
            return;

        Path folder = Path.of(textFieldFolder.getText());
        final Task<List<Integer>> task;
        if (rbFrameExtraction.isSelected())
            task = FrameExtraction.getFrameExtractionTask(videoFile, folder, false);
        else if (rbFreezeDetection.isSelected())
            task = FrameExtraction.getFreezeDetectionTask(folder);
        else if (rbBoth.isSelected())
            task = FrameExtraction.getFrameExtractionTask(videoFile, folder, true);
        else task = null;

        if (rbFrameExtraction.isSelected() || rbBoth.isSelected()) {
            try {
                if (Files.list(folder).count() != 0) {
                    Dialogs.showWarning("Not Empty", null, "The selected folder is not empty. Use a empty folder for frame extraction.");
                    return;
                }
            } catch (IOException e) {
                return;
            }
        }

        if (task == null)
            return;

        task.setOnRunning(event -> {
            superframes = null;
            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(task.progressProperty());
        });

        task.setOnSucceeded(e -> {
            superframes = task.getValue();
        });

        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(task);

        buttonStart.setText("Stop");
    }

    private List<Integer> superframes = null;

    public Optional<List<Integer>> getSuperframes() {
        return Optional.ofNullable(superframes);
    }

    public static Optional<List<Integer>> showAndWait(Path videoFile) {
        Stage stage = new Stage();
        stage.setTitle("Extraction");
        stage.initModality(Modality.APPLICATION_MODAL);

        InputStream iconIS = LoadingVideoDialog.class.getClassLoader().getResourceAsStream("logo_polygon.png");
        if (iconIS != null)
            stage.getIcons().add(new Image(iconIS));

        FXMLLoader loader;
        try {
            loader = new FXMLLoader(ExtractionDialog.class.getResource("/fxml/ExtractionDialog.fxml"));
            loader.setControllerFactory(param -> new ExtractionDialog(videoFile));
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            logger.error(e);
            return Optional.empty();
        }

        stage.sizeToScene();
        stage.centerOnScreen();
        stage.showAndWait();

        return ((ExtractionDialog) loader.getController()).getSuperframes();
    }
}
