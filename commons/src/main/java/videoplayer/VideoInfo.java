package videoplayer;

import util.MediaInfo;

public class VideoInfo {

    private final String videoFilePath;
    private final String videoFileName;
    private final double fps;
    private final int resolutionWidth;
    private final int resolutionHeight;
    private final int videoDurationMs;

    public VideoInfo(String videoFilePath) {
        this.videoFilePath = videoFilePath;
        MediaInfo mediaInfo = new MediaInfo();

        // decode spaces within file names
        String filePath = videoFilePath.replaceAll("%20", " ");

        // extract information
        if (mediaInfo.Open(filePath) > 0) { // success
            String fpsStr = mediaInfo.Get(util.MediaInfo.StreamKind.Video, 0, "FrameRate", util.MediaInfo.InfoKind.Text);
            String resWidth = mediaInfo.Get(util.MediaInfo.StreamKind.Video, 0, "Width", util.MediaInfo.InfoKind.Text);
            String resHeight = mediaInfo.Get(util.MediaInfo.StreamKind.Video, 0, "Height", util.MediaInfo.InfoKind.Text);

            // setting a default FPS of 30 if no data could available
            if (fpsStr.isEmpty()) {
                fps = 30.0;
                // TODO: Ask user to set a FPS value
                System.err.println("No FPS information available for this video file. Setting it to 30 FPS");
            } else fps = Double.parseDouble(fpsStr);
            resolutionWidth = Integer.parseInt(resWidth);
            resolutionHeight = Integer.parseInt(resHeight);
            videoDurationMs = (int) Double.parseDouble(mediaInfo.Get(MediaInfo.StreamKind.Video, 0, "Duration", MediaInfo.InfoKind.Text));

            String videoPath = videoFilePath.replace('\\', '/');
            videoFileName = videoPath.substring(videoPath.lastIndexOf("/") + 1);
        } else {
            throw new RuntimeException(String.format("%s could not be opened by MediaInfo.", videoFilePath));
        }
        mediaInfo.Close();
    }

    public String getVideoFilePath() {
        return videoFilePath;
    }

    public String getVideoFileName() {
        return videoFileName;
    }

    public double getFps() {
        return fps;
    }

    public int getResolutionWidth() {
        return resolutionWidth;
    }

    public int getResolutionHeight() {
        return resolutionHeight;
    }

    public int getVideoDurationMs() {
        return videoDurationMs;
    }
}
