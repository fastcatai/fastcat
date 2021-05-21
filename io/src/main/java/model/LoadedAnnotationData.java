package model;

import java.nio.file.Path;
import java.util.List;

public class LoadedAnnotationData {
    private final Path jsonFile;
    private final Path imageFolder;
    private final List<ImageData> imageDataList;
    private final Path maskFolder;

    public LoadedAnnotationData(Path jsonFile, Path imageFolder, List<ImageData> imageDataList) {
        this.jsonFile = jsonFile;
        this.imageFolder = imageFolder;
        this.imageDataList = imageDataList;
        this.maskFolder = null;
    }

    public LoadedAnnotationData(Path jsonFile, Path imageFolder, List<ImageData> imageDataList, Path maskFolder) {
        this.jsonFile = jsonFile;
        this.imageFolder = imageFolder;
        this.imageDataList = imageDataList;
        this.maskFolder = maskFolder;
    }

    public Path getJsonFile() {
        return jsonFile;
    }

    public Path getImageFolder() {
        return imageFolder;
    }

    public List<ImageData> getImageDataList() {
        return imageDataList;
    }

    public Path getMaskFolder() {
        return maskFolder;
    }
}
