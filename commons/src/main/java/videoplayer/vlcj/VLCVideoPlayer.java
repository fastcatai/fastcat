package videoplayer.vlcj;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import videoplayer.VideoInfo2;
import videoplayer.listeners.MediaChangeListener;
import videoplayer.VideoInfo;
import videoplayer.VideoPlayer;
import videoplayer.listeners.VideoPauseListener;
import videoplayer.listeners.VideoPlayListener;
import videoplayer.listeners.VideoStopListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Parts of this code are from <a href="https://github.com/caprica/vlcj-javafx">vlcj-javafx</a>
 * <br>
 * Modifications are from this <a href="https://github.com/caprica/vlcj/issues/883#issuecomment-523197593">comment</a>
 */
public class VLCVideoPlayer extends DirectRendering implements VideoPlayer {

    private final DoubleProperty videoLengthMillisProperty;
    private final DoubleProperty currentTimeProperty;
    private double msPerFrame;
    private VideoInfo2 videoInfo;

    // listeners
    private final List<MediaChangeListener> mediaChangeListeners;
    private final List<VideoPlayListener> videoPlayListeners;
    private final List<VideoPauseListener> videoPauseListeners;
    private final List<VideoStopListener> videoStopListeners;

    // frequently reads the current time from the video
    private Timer currentVideoTimeTimer;
    // used when slider is slided with the mouse
    private final AtomicBoolean slideTracking;

    private final Slider timelineSlider;

    public VLCVideoPlayer(Slider timelineSlider) {
        this.timelineSlider = timelineSlider;

        mediaChangeListeners = new ArrayList<>();
        videoPlayListeners = new ArrayList<>();
        videoPauseListeners = new ArrayList<>();
        videoStopListeners = new ArrayList<>();

        slideTracking = new AtomicBoolean();

        currentTimeProperty = new SimpleDoubleProperty();
        videoLengthMillisProperty = new SimpleDoubleProperty();

        getEmbeddedMediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEvents(this));
        // Click events on slider
        timelineSlider.setOnMousePressed(mouseEvent -> beginTracking());
        timelineSlider.setOnMouseReleased(mouseEvent -> endTracking());
        // Frames will be rendered during dragging/sliding
        timelineSlider.valueProperty().addListener((observable, oldValue, newValue) -> updateMediaPlayerPosition(newValue.floatValue() / 100));
    }

    void startPlaying() {
        if (currentVideoTimeTimer != null)
            return;

        try {
            currentVideoTimeTimer = new Timer("Current Time Scheduler");
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        try {
                            currentTimeProperty.set(getEmbeddedMediaPlayer().status().time());
                        } catch (Error e) {
                            // Sometimes a 'Invalid memory error' is thrown.
                            // Probably caused when timer is canceled during time() call.
                            System.err.println("Error thrown in current video time task.");
                        }
                    });
                }
            };
            currentVideoTimeTimer.schedule(timerTask, 0, 100); // controls the frequency of displaying the current time
        } catch (IllegalStateException e) { // TODO: Throws Timer already cancelled. When pause/stop and play
            e.printStackTrace();
        }
    }

    void stopPlaying() {
        if (currentVideoTimeTimer != null) {
            currentVideoTimeTimer.cancel();
            currentVideoTimeTimer.purge();
            currentVideoTimeTimer = null;
        }
    }

    //<editor-fold desc="Slider methods">

    private synchronized void updateMediaPlayerPosition(float newValue) {
        if (slideTracking.get()) {
            getEmbeddedMediaPlayer().controls().setPosition(newValue);
        }
    }

    private synchronized void beginTracking() {
        slideTracking.set(true);
    }

    private synchronized void endTracking() {
        slideTracking.set(false);
        // If an absolute click in the timeline was made rather than a drag
        getEmbeddedMediaPlayer().controls().setPosition((float) timelineSlider.getValue() / 100);
    }

    synchronized void updateSliderPosition(float newValue) {
        if (!slideTracking.get()) {
            timelineSlider.setValue(newValue * 100);
        }
    }

    //</editor-fold>

    void setCurrentTimeProperty(double currentTime) {
        this.currentTimeProperty.set(currentTime);
    }

    void setVideoLength(double videoLength) {
        this.videoLengthMillisProperty.set(videoLength);
    }

    void setVideoInfo(VideoInfo2 videoInfo) {
        videoInfo.getFPS().ifPresent(fps ->  msPerFrame = 1000.0 / fps);
        this.videoInfo = videoInfo;
    }

    List<MediaChangeListener> getMediaChangeListeners() {
        return mediaChangeListeners;
    }

    List<VideoPlayListener> getVideoPlayListeners() {
        return videoPlayListeners;
    }

    List<VideoPauseListener> getVideoPauseListeners() {
        return videoPauseListeners;
    }

    //<editor-fold desc="Implementations from VideoPlayer">

    @Override
    public boolean loadVideo(String source) {
        return getEmbeddedMediaPlayer().media().prepare(source);
    }

    @Override
    public void play() {
        getEmbeddedMediaPlayer().controls().play();
    }

    @Override
    public void pause() {
        getEmbeddedMediaPlayer().controls().pause();
    }

    @Override
    public void stop() {
        getEmbeddedMediaPlayer().controls().stop();
        for (VideoStopListener l : videoStopListeners) {
            l.onVideoStop();
        }
    }

    @Override
    public void setPlaybackSpeed(float rate) {
        if (rate >= 0.1 && rate <= 3.0)
            getEmbeddedMediaPlayer().controls().setRate(rate);
    }

    @Override
    public void skipForwards(double milliseconds) {
        getEmbeddedMediaPlayer().controls().skipTime(((long) milliseconds));
    }

    @Override
    public void skipBackwards(double milliseconds) {
        getEmbeddedMediaPlayer().controls().skipTime(-((long) milliseconds));
    }

    @Override
    public void jumpToTimestamp(double milliseconds) {
        getEmbeddedMediaPlayer().controls().setTime((long) milliseconds);
    }

    @Override
    public void nextFrame() {
        getEmbeddedMediaPlayer().controls().nextFrame();
    }

    @Override
    public void previousFrame() {
        getEmbeddedMediaPlayer().controls().pause();
        getEmbeddedMediaPlayer().controls().skipTime(-((long) msPerFrame));
    }

    @Override
    public boolean isPlaying() {
        return getEmbeddedMediaPlayer().status().isPlaying();
    }

    @Override
    public ReadOnlyDoubleProperty currentTimeMillisProperty() {
        return currentTimeProperty;
    }

    @Override
    public ReadOnlyDoubleProperty videoLengthMillisProperty() {
        return videoLengthMillisProperty;
    }

    @Override
    public VideoInfo2 getVideoInfo() {
        return videoInfo;
    }

    @Override
    public ImageView getVideoPane() {
        return this;
    }

    @Override
    public void release() {
        stopPlaying();
        dispose();
    }

    @Override
    public void setOnMediaChanged(MediaChangeListener listener) {
        mediaChangeListeners.add(listener);
    }

    @Override
    public void setOnVideoPlay(VideoPlayListener listener) {
        videoPlayListeners.add(listener);
    }

    @Override
    public void setOnVideoPause(VideoPauseListener listener) {
        videoPauseListeners.add(listener);
    }

    @Override
    public void setOnVideoStop(VideoStopListener listener) {
        videoStopListeners.add(listener);
    }

    //</editor-fold>
}
