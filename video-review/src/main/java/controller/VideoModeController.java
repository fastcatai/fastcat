package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import util.TimeFormatter;
import videoplayer.VideoPlayer;
import videoplayer.vlcj.VLCVideoPlayer;
import videoreview.ViewMode;

import java.text.DecimalFormat;

public class VideoModeController {

    @FXML
    private VBox root;
    @FXML
    private StackPane stackPaneViewer;
    @FXML
    private Label labelCurrentTime;
    @FXML
    private Label labelDuration;
    @FXML
    private Slider sliderTimeline;
    @FXML
    private Spinner<Double> spinnerPlaybackSpeed;
    @FXML
    private Label labelButtonSwitchToFrameMode;
    @FXML
    private Label labelJumpBack;
    @FXML
    private Label labelPlayPause;
    @FXML
    private Label labelJumpForward;
    @FXML
    private ImageView buttonIconPlayPause;
    @FXML
    private TextField textFieldJumpToFrameFromVideo;
    @FXML
    private Label labelFrameNumberApprox;


    private VideoPlayer videoPlayer;

    private final VideoReviewController videoReviewController;

    public VideoModeController(VideoReviewController videoReviewController) {
        this.videoReviewController = videoReviewController;
    }

    @FXML
    private void initialize() {
        initVideoPlayer();
        initPlaybackSpeedSpinner();

        // add mouse event to buttons
        labelPlayPause.setOnMouseClicked(this::playPauseStop);
        labelJumpBack.setOnMouseClicked(this::jumpWithinVideo);
        labelJumpForward.setOnMouseClicked(this::jumpWithinVideo);
        labelButtonSwitchToFrameMode.setOnMouseClicked(event -> {
            double currentTimeMillis = videoPlayer.currentTimeMillisProperty().get();
            double framesPerMilliseconds = videoPlayer.getVideoInfo().getFPS().get() / 1000.0;
            videoReviewController.getFrameViewerMode().getFrameLoader().jumpToFrame((int) Math.ceil(currentTimeMillis * framesPerMilliseconds));
            videoReviewController.setCurrentViewMode(ViewMode.FRAMES);
        });
        textFieldJumpToFrameFromVideo.setOnAction(event -> {
            videoReviewController.getFrameViewerMode().getFrameLoader().jumpToFrame(Integer.parseInt(textFieldJumpToFrameFromVideo.getText().strip()));
            videoReviewController.setCurrentViewMode(ViewMode.FRAMES);
//            videoReviewController.switchToImageMode(Integer.parseInt(textFieldJumpToFrameFromVideo.getText()))
        });

        labelButtonSwitchToFrameMode.disableProperty().bind(videoReviewController.getListViewSuperframes().getSelectionModel().selectedItemProperty().isNull());
    }

    //<editor-fold desc="Initialization of controls">

    private void initVideoPlayer() {
        // create video player
        videoPlayer = new VLCVideoPlayer(sliderTimeline);


        // display new total video duration on change
        videoPlayer.videoLengthMillisProperty().addListener((observable, oldLength, newLength) ->
                labelDuration.setText(TimeFormatter.formatTimeDetailed(newLength.doubleValue())));

        // display new current time and an approximation of the frame number
        videoPlayer.currentTimeMillisProperty().addListener((observable, oldTime, newTime) -> {
            if (newTime == null)
                return;
            labelCurrentTime.setText(TimeFormatter.formatTimeDetailed(newTime.doubleValue()));
            double framesPerMilliseconds = videoPlayer.getVideoInfo().getFPS().get() / 1000.0;
//            double framesPerMilliseconds = VideoFolderGateway.get().getVideoData().getFps() / 1000.0;
            labelFrameNumberApprox.setText(String.format("%.0f", Math.ceil(newTime.doubleValue() * framesPerMilliseconds))); // TODO: save FPS and use it here
        });


//        videoPlayer.videoLengthMillisProperty().addListener((observable, oldValue, newValue) ->
//                VideoFolderGateway.get().getVideoData().setVideoLength(getVideoLength()));


        // add player and viewer to stack pane
        stackPaneViewer.getChildren().add(videoPlayer.getVideoPane());
    }

    private void initPlaybackSpeedSpinner() {
        // initialize spinner for playback speed
        StringConverter<Double> faultTolerantDoubleConverter = new StringConverter<>() {
            @Override
            public String toString(Double value) {
                // If the specified value is null, return a zero-length String
                if (value == null)
                    return "";
                return new DecimalFormat("#.#").format(value);
            }

            @Override
            public Double fromString(String value) {
                // If the specified value is null or zero-length, return null
                if (value == null || value.isEmpty())
                    return null;
                value = value.trim();
                if (value.contains(","))
                    value = value.replace(',', '.');
                try {
                    return Double.valueOf(value);
                } catch (NumberFormatException e) {
                    System.out.println("EXP");
                    return 1.0;
                }
            }
        };


        // TODO: Edit wont work if letters are typed in. Wont set to default 1
//        spinnerPlaybackSpeed.setEditable(true);
        SpinnerValueFactory<Double> valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.5, 5.0, 1.0, 0.5);
        spinnerPlaybackSpeed.setValueFactory(valueFactory);
        valueFactory.setConverter(faultTolerantDoubleConverter);
//        valueFactory.setConverter(new DoubleStringConverter());
        spinnerPlaybackSpeed.valueProperty().addListener((observable, oldValue, newValue) -> videoPlayer.setPlaybackSpeed(newValue.floatValue()));

        // won't change value, but will commit editor
        spinnerPlaybackSpeed.focusedProperty().addListener((observable, oldValue, newValue) -> spinnerPlaybackSpeed.increment(0));
    }

    //</editor-fold>

    public VideoPlayer getVideoPlayer() {
        return videoPlayer;
    }

//    public void show() {
//        if (videoReviewController.getCurrentViewMode() != ViewMode.VIDEO) {
//            root.setVisible(true);
//            videoReviewController.setCurrentViewMode(ViewMode.VIDEO);
//        }
//    }
//
//    public void show(int frameNumber) {
//        show();
////        videoReviewController.getFrameLoader().jumpToFrame(frameNumber);
//        videoReviewController.getFrameViewerMode().getFrameLoader().jumpToFrame(frameNumber);
//        double millisecondsPerFrame =  1.0 / (videoPlayer.getVideoInfo().getFps() / 1000.0);
//        videoJumpToTimestamp((long) (frameNumber * millisecondsPerFrame));
//    }
//
//    public void hide() {
//        root.setVisible(false);
//        if (videoIsPlaying())
//            videoPause();
//    }

    private void playPauseStop(MouseEvent event) {
        if (videoReviewController.getCurrentlyLoadedData() == null)
            return;
        if (event.getSource() == labelPlayPause && videoPlayer.isPlaying())
            videoPause();
        else if (event.getSource() == labelPlayPause)
            videoPlay();
    }

    private void jumpWithinVideo(MouseEvent event) {
        if (event.getSource() == labelJumpBack)
            this.videoJumpBack();
        else if (event.getSource() == labelJumpForward)
            this.videoJumpForward();
    }

    //<editor-fold desc="Video Player Methods">
    public boolean videoLoad(String source) {
        return videoPlayer.loadVideo(source);
    }

    public long getVideoLength() {
        return (long) videoPlayer.videoLengthMillisProperty().get();
    }

    public void videoPlay() {
        videoPlayer.play();
        labelPlayPause.getStyleClass().remove("play-button");
        labelPlayPause.getStyleClass().add("pause-button");
    }

    public void videoPause() {
        videoPlayer.pause();
        labelPlayPause.getStyleClass().remove("pause-button");
        labelPlayPause.getStyleClass().add("play-button");
    }

    public void videoStop() {
        videoPlayer.stop();
    }

    public boolean videoIsPlaying() {
        return videoPlayer.isPlaying();
    }

    private void videoJumpBack() {
        videoPlayer.skipBackwards(5000);
    }

    private void videoJumpForward() {
        videoPlayer.skipForwards(5000);
    }

    public void videoJumpToTimestamp(long milliseconds) {
        videoPlayer.jumpToTimestamp(milliseconds);
    }

    public void videoPlaybackSpeed(float rate) {
        videoPlayer.setPlaybackSpeed(rate);
    }

    public void videoPlaybackSpeedIncrement() {
        spinnerPlaybackSpeed.increment();
    }

    public void videoPlaybackSpeedDecrement() {
        spinnerPlaybackSpeed.decrement();
    }
    //</editor-fold>


    public void stop() {
//        hide();
        videoPlayer.release();
    }

}
