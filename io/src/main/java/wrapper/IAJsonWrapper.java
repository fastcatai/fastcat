package wrapper;

import model.ImageData;
import model.LoadedAnnotationData;

import java.util.List;

public class IAJsonWrapper {
    private final List<ImageData> images;
    private final IAMetadata metadata;

    public IAJsonWrapper(List<ImageData> images, IAMetadata metadata) {
        this.images = images;
        this.metadata = metadata;
    }

    public IAJsonWrapper(List<ImageData> images, LoadedAnnotationData loadedData) {
        this.images = images;
        metadata = new IAMetadata(images, loadedData);
    }

    public List<ImageData> getImages() {
        return images;
    }

    public IAMetadata getMetadata() {
        return metadata;
    }

}
