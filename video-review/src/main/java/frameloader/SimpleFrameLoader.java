package frameloader;

import javafx.beans.property.ObjectProperty;
import javafx.scene.image.Image;

public class SimpleFrameLoader extends FrameLoaderBase {

    public SimpleFrameLoader(final ObjectProperty<Image> imageProperty) {
        super(imageProperty);
    }

    @Override
    public void nextFrame() {
        super.nextFrame();
        setFrame(getCurrentIndex());
    }

    @Override
    public void previousFrame() {
        super.previousFrame();
        setFrame(getCurrentIndex());
    }

    @Override
    public void jumpToFrame(int frameNumber) {
        super.jumpToFrame(frameNumber);
        setFrame(getCurrentIndex());
    }

    @Override
    public void skipFrames(int delta) {
        super.skipFrames(delta);
        setFrame(getCurrentIndex());
    }
}
