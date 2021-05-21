package videoplayer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.MediaInfo;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

public class VideoInfo2 {
    private static final Logger logger = LogManager.getLogger(VideoInfo2.class);

    private final Path videoFilePath;
    private final String videoFileName;
    private final Double fps;
    private final Integer resolutionWidth;
    private final Integer resolutionHeight;
    private final Integer videoDurationMs;
    private final Integer lastFrameNumber;

    public VideoInfo2(String videoFilePath) {
        this(Path.of(videoFilePath));
    }

    public VideoInfo2(Path videoFilePath) {
        this.videoFilePath = videoFilePath;
        MediaInfo mediaInfo = new MediaInfo();

        // We receive a encoded path. See: https://www.w3schools.com/tags/ref_urlencode.ASP
        // Because MediaInfo can not read encoded paths we need to decode the file path.
        String decodedPath = URLDecoder.decode(videoFilePath.toString(), StandardCharsets.UTF_8);

        // extract information
        if (mediaInfo.Open(decodedPath) > 0) { // success
            String fpsStr = mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "FrameRate", MediaInfo.InfoKind.Text);
            String resWidth = mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "Width", MediaInfo.InfoKind.Text);
            String resHeight = mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "Height", MediaInfo.InfoKind.Text);
            String durationMS = mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "Duration", MediaInfo.InfoKind.Text);
            fps = parseFPS(fpsStr);
            resolutionWidth = parseResolutionDimension(resWidth);
            resolutionHeight = parseResolutionDimension(resHeight);
            videoDurationMs = parseVideoDuration(durationMS);
            videoFileName = videoFilePath.getFileName().toString();

            if (fps != null && videoDurationMs != null)
                lastFrameNumber = (int) Math.ceil((videoDurationMs / 1000.0) * fps);
            else lastFrameNumber = null;
        } else {
            throw logger.throwing(new RuntimeException(String.format("%s could not be opened by MediaInfo.", videoFilePath)));
        }
        mediaInfo.Close();
    }

    private Double parseFPS(String fps) {
        if (fps == null || fps.isBlank())
            return null;
        try {
            return Double.parseDouble(fps);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseResolutionDimension(String dim) {
        if (dim == null || dim.isBlank())
            return null;
        try {
            return Integer.parseInt(dim);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseVideoDuration(String ms) {
        if (ms == null || ms.isBlank())
            return null;
        try {
            return (int) Double.parseDouble(ms);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Path getVideoFilePath() {
        return videoFilePath;
    }

    public String getVideoFileName() {
        return videoFileName;
    }

    public Optional<Double> getFPS() {
        return Optional.ofNullable(fps);
    }

    public Optional<Integer> getResolutionWidth() {
        return Optional.ofNullable(resolutionWidth);
    }

    public Optional<Integer> getResolutionHeight() {
        return Optional.ofNullable(resolutionHeight);
    }

    public Optional<Integer> getVideoDuration() {
        return Optional.ofNullable(videoDurationMs);
    }

    public Optional<Integer> getLastFrameNumber() {
        return Optional.ofNullable(lastFrameNumber);
    }
}
