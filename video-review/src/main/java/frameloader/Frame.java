package frameloader;

import javafx.scene.image.Image;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Frame {

    private final int frameNumber;
    private final Path frame;
    private final Image image;

    public Frame(final Path frame) {
        this.frame = frame;
        image = createImage(frame);
        String filename = frame.getFileName().toString();
        frameNumber = Integer.parseInt(filename.substring(0, filename.lastIndexOf('.')));
    }

    private Image createImage(final Path frame) {
        try {
            return new Image(Files.newInputStream(frame));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public String getFrameNumberString() {
        return Integer.toString(frameNumber);
    }

    public Path getFrame() {
        return frame;
    }

    public Image getImage() {
        return image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Frame f = (Frame) o;
        if (getFrameNumber() != f.getFrameNumber()) return false;
        if (!getFrame().equals(f.getFrame())) return false;
        return getImage().equals(f.getImage());
    }

    @Override
    public int hashCode() {
        int result = getFrameNumber();
        result = 31 * result + getFrame().hashCode();
        result = 31 * result + getImage().hashCode();
        return result;
    }
}
