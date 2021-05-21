package util;

import model.ImageData;

public interface ImageDataCreatedListener {
    /**
     * Will be triggered when a ne ImageData object is created and added.
     *
     * @param imageData object that was created
     */
    void onNewImageData(ImageData imageData);
}
