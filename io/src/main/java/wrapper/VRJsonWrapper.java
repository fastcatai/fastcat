package wrapper;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import model.ImageData;
import model.LoadedReviewData;
import model.SuperframeData;

import java.util.List;

@JsonPropertyOrder({"superframes", "images", "metadata"})
public class VRJsonWrapper {
    private final List<SuperframeData> superframes;
    private final List<ImageData> images;
    private final VRMetadata metadata;

    public VRJsonWrapper(List<SuperframeData> superframes, List<ImageData> images, VRMetadata metadata) {
        this.superframes = superframes;
        this.images = images;
        this.metadata = metadata;
    }

    public VRJsonWrapper(List<SuperframeData> superframes, List<ImageData> images, LoadedReviewData loadedData) {
        this.superframes = superframes;
        this.images = images;
        metadata = new VRMetadata(superframes, images, loadedData);
    }

    public List<SuperframeData> getSuperframes() {
        return superframes;
    }

    public List<ImageData> getImages() {
        return images;
    }

    public VRMetadata getMetadata() {
        return metadata;
    }
}
