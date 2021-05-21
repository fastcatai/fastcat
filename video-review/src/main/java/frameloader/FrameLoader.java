package frameloader;

import javafx.beans.property.ReadOnlyObjectProperty;

public interface FrameLoader {

    /**
     * Gets the latest loaded frame.
     *
     * @return current frame
     */
    ReadOnlyObjectProperty<Frame> frameProperty();

    /**
     * Loads the next frame
     */
    void nextFrame();

    /**
     * Loads the previous frame
     */
    void previousFrame();

    /**
     * Jump to a arbitrary frame number
     *
     * @param frameNumber number of the frame
     */
    void jumpToFrame(int frameNumber);

    /**
     * Skip forward or backward by a number of frames.
     * To skip backwards specify a negative delta.
     *
     * @param delta number of frame to be skipped
     */
    void skipFrames(int delta);
}
