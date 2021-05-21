package videoplayer;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.Node;
import videoplayer.listeners.MediaChangeListener;
import videoplayer.listeners.VideoPauseListener;
import videoplayer.listeners.VideoPlayListener;
import videoplayer.listeners.VideoStopListener;

public interface VideoPlayer {
    // player control methods
    boolean loadVideo(String source);
    void play();
    void pause();
    void stop();
    void setPlaybackSpeed(float rate);
    void skipForwards(double milliseconds);
    void skipBackwards(double milliseconds);
    void jumpToTimestamp(double milliseconds);
    void nextFrame();
    void previousFrame();

    // status methods
    boolean isPlaying();
    ReadOnlyDoubleProperty currentTimeMillisProperty();
    ReadOnlyDoubleProperty videoLengthMillisProperty();
    VideoInfo2 getVideoInfo();

    /**
     * A node where the video frames are displayed.
     * @return a node on which the video frames are painted
     */
    Node getVideoPane();

    /**
     * Method to release video player resources.
     */
    void release();

    // basic video player listeners
    void setOnMediaChanged(MediaChangeListener listener);
    void setOnVideoPlay(VideoPlayListener listener);
    void setOnVideoPause(VideoPauseListener listener);
    void setOnVideoStop(VideoStopListener listener);
}
