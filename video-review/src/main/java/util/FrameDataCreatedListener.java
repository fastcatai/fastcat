package util;

import model.FrameData;

public interface FrameDataCreatedListener {
    /**
     * Will be triggered when a ne FrameData object is created and added.
     *
     * @param frameData object that was created
     */
    void onNewFrameData(FrameData frameData);
}
