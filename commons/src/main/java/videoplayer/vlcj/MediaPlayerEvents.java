package videoplayer.vlcj;

import javafx.application.Platform;
import javafx.concurrent.Task;
import uk.co.caprica.vlcj.media.MediaRef;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import videoplayer.VideoInfo;
import videoplayer.VideoInfo2;
import videoplayer.listeners.MediaChangeListener;
import videoplayer.listeners.VideoPauseListener;
import videoplayer.listeners.VideoPlayListener;

public class MediaPlayerEvents extends MediaPlayerEventAdapter {

    private final VLCVideoPlayer videoPlayer;

    MediaPlayerEvents(VLCVideoPlayer videoPlayer) {
        this.videoPlayer = videoPlayer;
    }

    @Override
    public void playing(MediaPlayer mediaPlayer) {
        videoPlayer.startPlaying();
        for (VideoPlayListener l : videoPlayer.getVideoPlayListeners()) {
            l.onVideoPlay();
        }
    }

    @Override
    public void paused(MediaPlayer mediaPlayer) {
        for (VideoPauseListener l : videoPlayer.getVideoPauseListeners()) {
            l.onVideoPause();
        }
    }

    @Override
    public void stopped(MediaPlayer mediaPlayer) {
        videoPlayer.stopPlaying();
        // display the 0 when the video is stopped
        Platform.runLater(() -> videoPlayer.setCurrentTimeProperty(0));
        // TODO: Black out canvas when stopped
        // TODO: Set slider back to beginning
    }

    @Override
    public void finished(MediaPlayer mediaPlayer) {
        videoPlayer.stopPlaying();
    }

    @Override
    public void error(MediaPlayer mediaPlayer) {
        videoPlayer.stopPlaying();
    }

    @Override
    public void mediaChanged(MediaPlayer mediaPlayer, MediaRef media) {
        String videoFilePath = media.newMedia().info().mrl().substring("file:///".length());
        videoPlayer.setVideoInfo(new VideoInfo2(videoFilePath));

        // notify listeners
        for (MediaChangeListener l : videoPlayer.getMediaChangeListeners()) {
            l.onMediaChange(videoFilePath);
        }
    }

    @Override
    public void lengthChanged(MediaPlayer mediaPlayer, long newLength) {
        Platform.runLater(() -> videoPlayer.setVideoLength(newLength));
    }

    @Override
    public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
        Platform.runLater(() -> videoPlayer.updateSliderPosition(newPosition));
    }

}
