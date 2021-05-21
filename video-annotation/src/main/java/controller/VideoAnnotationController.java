package controller;

import annotation.UIAnnotation;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import ui.AnnotationPaneVA;
import ui.IconButton;
import ui.IconToggleButton;
import util.BaseController;
import util.TimeFormatter;
import util.VideoAnnotationChoice;
import util.VideoAnnotationView;
import videoplayer.VideoInfo2;
import videoplayer.VideoPlayer;
import videoplayer.vlcj.VLCVideoPlayer;

import java.nio.file.Path;

public class VideoAnnotationController implements BaseController {

    @FXML
    private MenuBar menuBar;
    @FXML
    private ChoiceBox<VideoAnnotationChoice> choiceBoxAnnotations;
    @FXML
    private TextField defaultAnnotationLabel;
    @FXML
    private TextField defaultAnnotationInstance;
    @FXML
    private TableView<UIAnnotation<?>> timeTable;
    @FXML
    private TimeTableController timeTableController;
    @FXML
    private Label labelCurrentTime;
    @FXML
    private Slider sliderTimeline;
    @FXML
    private Label labelDuration;
    // video player controls
    @FXML
    private IconToggleButton buttonPlayPause;
    @FXML
    private IconButton buttonJumpBack;
    @FXML
    private IconButton buttonJumpForward;
    @FXML
    private IconButton buttonPreviousFrame;
    @FXML
    private IconButton buttonNextFrame;
    @FXML
    private Spinner<Double> spinnerPlaybackSpeed;
    // info labels
    @FXML
    private Label labelFrameNumberApprox;
    @FXML
    private Label labelInfoFilename;
    @FXML
    private Label labelInfoResolution;
    @FXML
    private Label labelInfoFPS;
    @FXML
    private StackPane stackPaneViewer;

    private AnnotationPaneVA annotationPane;
    private VideoPlayer videoPlayer;
    private final ObjectProperty<Path> currentVideoFile;
    private final VideoAnnotationView videoAnnotationView;

    public VideoAnnotationController(VideoAnnotationView videoAnnotationView) {
        this.videoAnnotationView = videoAnnotationView;
        currentVideoFile = new SimpleObjectProperty<>();
    }

    @FXML
    private void initialize() {
        initPlayerAndPane();
        choiceBoxAnnotations.getItems().setAll(VideoAnnotationChoice.values());
        choiceBoxAnnotations.getSelectionModel().select(VideoAnnotationChoice.POINT);
    }

    private void initPlayerAndPane() {
        videoPlayer = new VLCVideoPlayer(sliderTimeline);
        videoPlayer.videoLengthMillisProperty().addListener((observable, oldLength, newLength) ->
                labelDuration.setText(TimeFormatter.formatTimeDetailed(newLength.doubleValue())));

        videoPlayer.currentTimeMillisProperty().addListener((observable, oldTime, newTime) -> {
            if (newTime == null) return;
            labelCurrentTime.setText(TimeFormatter.formatTimeDetailed(newTime.doubleValue()));
            labelFrameNumberApprox.setText(String.format("%.0f", Math.ceil((newTime.doubleValue() / 1000.0) * videoPlayer.getVideoInfo().getFPS().get())));
        });

        videoPlayer.setOnMediaChanged(mediaPath -> {
            VideoInfo2 videoInfo = videoPlayer.getVideoInfo();
            labelInfoFilename.setText(videoInfo.getVideoFileName());
            labelInfoFilename.getTooltip().setText(videoInfo.getVideoFileName());
            labelInfoFPS.setText(Double.toString(videoInfo.getFPS().get()));
            labelInfoResolution.setText(String.format("%d x %d", videoInfo.getResolutionWidth().get(), videoInfo.getResolutionHeight().get()));
        });

        // create annotation pane
        annotationPane = new AnnotationPaneVA(this);
        annotationPane.setDisable(true); // prevent annotation at the beginning when nothing is loaded
        annotationPane.maxWidthProperty().bind(annotationPane.prefWidthProperty());
        annotationPane.maxHeightProperty().bind(annotationPane.prefHeightProperty());

        videoPlayer.getVideoPane().boundsInParentProperty().addListener((observable, oldBounds, newBounds) ->
                annotationPane.setPrefSize(newBounds.getWidth(), newBounds.getHeight()));

        // append to stack pane
        stackPaneViewer.getChildren().add(videoPlayer.getVideoPane());
        stackPaneViewer.getChildren().add(annotationPane);


        spinnerPlaybackSpeed.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.5, 5.0, 1.0, 0.5));
        spinnerPlaybackSpeed.valueProperty().addListener((observable, oldValue, newValue) -> videoPlayer.setPlaybackSpeed(newValue.floatValue()));
        // won't change value, but will commit editor
        spinnerPlaybackSpeed.focusedProperty().addListener((observable, oldValue, newValue) -> spinnerPlaybackSpeed.increment(0));
    }

    @FXML
    private void playPause() {
        if (videoPlayer.isPlaying()) videoPlayer.pause();
        else videoPlayer.play();
    }

    @FXML
    private void videoSkipForward() {
        videoPlayer.skipForwards(5000);
    }

    @FXML
    private void videoSkipBackwards() {
        videoPlayer.skipBackwards(5000);
    }

    @FXML
    private void videoNextFrame() {
        videoPlayer.nextFrame();
    }

    @FXML
    private void videoPreviousFrame() {
        videoPlayer.previousFrame();
    }

    @Override
    public void close() {
        videoPlayer.release();
        annotationPane.close();
    }

    public VideoAnnotationView getVideoAnnotationView() {
        return videoAnnotationView;
    }

    public void setCurrentVideoFile(Path videoFile) {
        currentVideoFile.set(videoFile);
    }

    public VideoPlayer getVideoPlayer() {
        return videoPlayer;
    }

    public VideoAnnotationChoice getChosenAnnotation() {
        return choiceBoxAnnotations.getValue();
    }

    public String getDefaultAnnotationLabel() {
        return defaultAnnotationLabel.getText().strip();
    }

    public String getDefaultAnnotationInstance() {
        return defaultAnnotationInstance.getText().strip();
    }

    public AnnotationPaneVA getAnnotationPane() {
        return annotationPane;
    }

    public TableView<UIAnnotation<?>> getTimeTable() {
        return timeTable;
    }

    public IconToggleButton getButtonPlayPause() {
        return buttonPlayPause;
    }
}
