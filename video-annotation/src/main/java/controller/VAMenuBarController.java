package controller;

import about.AboutStage;
import com.fasterxml.jackson.databind.ObjectMapper;
import events.ViewEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;
import wrapper.VAJsonWrapper;
import json.VAJsonModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import settings.SettingsStage;
import ui.Dialogs;
import util.BaseController;
import util.BaseControllerInitializable;
import util.SupportedVideoFiles;
import videoplayer.VideoInfo2;

import java.io.File;
import java.io.IOException;

public class VAMenuBarController {
    private static final Logger logger = LogManager.getLogger(VAMenuBarController.class);

    @FXML
    private MenuItem menuItemLoadVideo;
    @FXML
    private MenuItem menItemSave;

    private final VideoAnnotationController vaController;

    public VAMenuBarController(VideoAnnotationController vaController) {
        this.vaController = vaController;
    }

    @FXML
    private void loadVideo() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video Files", SupportedVideoFiles.videoFiles));
        File videoFile = chooser.showOpenDialog(menuItemLoadVideo.getParentPopup().getOwnerWindow());
        if (videoFile != null) {
            vaController.setCurrentVideoFile(videoFile.toPath());
            boolean loaded = vaController.getVideoPlayer().loadVideo(videoFile.getPath());
            if (loaded) {
                vaController.getAnnotationPane().setDisable(false);
                vaController.getVideoPlayer().play();
                vaController.getButtonPlayPause().setSelected(true);
            }
        }
    }

    @FXML
    private void save(ActionEvent event) {
        final VideoInfo2 videoInfo = vaController.getVideoPlayer().getVideoInfo();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Video Annotations");
        fileChooser.setInitialDirectory(videoInfo.getVideoFilePath().getParent().toFile());
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video JSON (*.video.json)", "*.video.json"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON (*.json)", "*.json"));
        File jsonFile = fileChooser.showSaveDialog(menItemSave.getParentPopup().getOwnerWindow());
        if (jsonFile != null) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new VAJsonModule(videoInfo));
            try {
                VAJsonWrapper jsonWrapper = new VAJsonWrapper(vaController.getTimeTable().getItems());
                mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, jsonWrapper);
            } catch (IOException e) {
                logger.error("Serialization to JSON failed!", e);
                Dialogs.showExceptionDialog(e);
            }
        }
    }

    @FXML
    private void openSettings() {
        new SettingsStage().show();
    }

    @FXML
    private void backToMenu() {
        vaController.getVideoAnnotationView().getStage().fireEvent(new ViewEvent(
                vaController.getVideoAnnotationView().getStage(), ViewEvent.MENU));
    }

    @FXML
    private void exitProgram() {
        vaController.getVideoAnnotationView().getStage().fireEvent(new WindowEvent(
                vaController.getVideoAnnotationView().getStage(), WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    @FXML
    private void openAbout() {
        AboutStage.show(vaController.getVideoAnnotationView().getApplication());
    }
}
