package videoplayer.vlcj;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

import java.nio.ByteBuffer;

/**
 * Parts of this code are from <a href="https://github.com/caprica/vlcj-javafx">vlcj-javafx</a>
 * and this <a href="https://github.com/caprica/vlcj/issues/883#issuecomment-523197593">comment</a>
 *
 * @implNote Since ImageView is not resizeable we have to override some methods. See
 * <a href="https://stackoverflow.com/a/35202191/1964855">Stack Overflow comment</a> or
 * <a href="https://bugs.openjdk.java.net/browse/JDK-8097162">this comment</a>
 */
public abstract class DirectRendering extends ImageView {

    private PixelBuffer<ByteBuffer> videoPixelBuffer;
    private MediaPlayerFactory mediaPlayerFactory;
    private EmbeddedMediaPlayer embeddedMediaPlayer;

    DirectRendering() {
        setPreserveRatio(true);
        loadPlayer();
    }

    private void loadPlayer() {
        // Set custom discovery strategy to load the internal VLC player
        NativeDiscovery nd = new NativeDiscovery(new InternalDiscovery());
        mediaPlayerFactory = new MediaPlayerFactory(nd);

        embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        embeddedMediaPlayer.videoSurface().set(new CallbackVideoSurface(
                new FXBufferFormatCallback(),
                new FXRenderCallback(),
                true,
                VideoSurfaceAdapters.getVideoSurfaceAdapter()
        ));
    }

    /**
     * Is invoked when the format of the video changes.
     */
    private class FXBufferFormatCallback implements BufferFormatCallback {
        private int bufferWidth;
        private int bufferHeight;

        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            bufferWidth = sourceWidth;
            bufferHeight = sourceHeight;
            return new RV32BufferFormat(sourceWidth, sourceHeight);
        }

        @Override
        public void allocatedBuffers(ByteBuffer[] buffers) {
            assert buffers[0].capacity() == bufferWidth * bufferHeight * 4;
            // With the PixelBuffer the native video buffer is used directly for the image buffer there is no full-frame buffer copy here
            videoPixelBuffer = new PixelBuffer<>(bufferWidth, bufferHeight, buffers[0], PixelFormat.getByteBgraPreInstance());
            setImage(new WritableImage(videoPixelBuffer));
        }
    }

    /**
     * Will be called by VLC with raw video frame data in form of a ByteBuffer backed by native memory.
     */
    private class FXRenderCallback implements RenderCallback {
        @Override
        public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
            Platform.runLater(() -> videoPixelBuffer.updateBuffer(pb -> null));
        }
    }

    public MediaPlayer getEmbeddedMediaPlayer() {
        if (embeddedMediaPlayer == null)
            loadPlayer();
        return embeddedMediaPlayer;
    }

    /**
     * Dispose the media player resources.
     */
    public void dispose() {
        embeddedMediaPlayer.controls().stop();
        embeddedMediaPlayer.release();
        mediaPlayerFactory.release();
        embeddedMediaPlayer = null;
        mediaPlayerFactory = null;
    }

    //<editor-fold desc="Method from Node/ImageView">

    @Override
    public double minWidth(double height) {
        return 100;
    }

    @Override
    public double prefWidth(double height) {
        Image I = getImage();
        if (I == null) return minWidth(height);
        return I.getWidth();
    }

    @Override
    public double maxWidth(double height) {
        return 16384; // maximum texture size?
    }

    @Override
    public double minHeight(double width) {
        return 100;
    }

    @Override
    public double prefHeight(double width) {
        Image I = getImage();
        if (I == null) return minHeight(width);
        return I.getHeight();
    }

    @Override
    public double maxHeight(double width) {
        return 16384; // maximum texture size?
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public void resize(double width, double height) {
        setFitWidth(width);
        setFitHeight(height);
    }

    //</editor-fold>
}
