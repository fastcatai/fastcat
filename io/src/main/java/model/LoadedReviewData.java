package model;

import videoplayer.VideoInfo2;
import wrapper.VRJsonWrapper;

import java.nio.file.Path;
import java.util.List;

/**
 * Wrapper that will pass all necessary data
 */
public class LoadedReviewData {
    private final Path jsonFile;
    private final Path videoFile;
    private final Path imageFolder;
    private final VideoInfo2 videoInfo;
    private final int frameWidth;
    private final int frameHeight;
    private final List<SuperframeData> superframes;
    private final VRJsonWrapper jsonWrapper;
    private final String[] allFrames;

    public LoadedReviewData(Path jsonFile, Path videoFile, Path imageFolder, VideoInfo2 videoInfo,
                            int frameWidth, int frameHeight, String[] allFrames, VRJsonWrapper jsonWrapper) {
        this(jsonFile, videoFile, imageFolder, videoInfo, frameWidth, frameHeight, allFrames, null, jsonWrapper);
    }

    public LoadedReviewData(Path jsonFile, Path videoFile, Path imageFolder, VideoInfo2 videoInfo,
                            int frameWidth, int frameHeight, String[] allFrames, List<SuperframeData> superframes) {
        this(jsonFile, videoFile, imageFolder, videoInfo, frameWidth, frameHeight, allFrames, superframes, null);
    }

    private LoadedReviewData(Path jsonFile, Path videoFile, Path imageFolder, VideoInfo2 videoInfo,
                             int frameWidth, int frameHeight, String[] allFrames,
                             List<SuperframeData> superframes, VRJsonWrapper jsonWrapper) {
        this.jsonFile = jsonFile;
        this.videoFile = videoFile;
        this.imageFolder = imageFolder;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.videoInfo = videoInfo;
        this.superframes = superframes;
        this.jsonWrapper = jsonWrapper;
        this.allFrames = allFrames;
    }

    public Path getJsonFile() {
        return jsonFile;
    }

    public Path getVideoFile() {
        return videoFile;
    }

    public Path getImageFolder() {
        return imageFolder;
    }

    public VideoInfo2 getVideoInfo() {
        return videoInfo;
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    public List<SuperframeData> getSuperframes() {
        if (jsonWrapper == null)
            return superframes;
        else return jsonWrapper.getSuperframes();
    }

    public VRJsonWrapper getJsonWrapper() {
        return jsonWrapper;
    }

    public String[] getAllFrames() {
        return allFrames;
    }
}
