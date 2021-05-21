package frameloader;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.image.Image;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class FrameLoaderBase {

    // Path of folder that contains all images
    private Path framesFolder;

    // All frames within folder in an ascending order
    private String[] allFrames;

    // Latest loaded frame
    private final ReadOnlyObjectWrapper<Frame> currentFrame;

    // Points to the current index of frame that is loaded
    private int currentIndex = 0;

    // Mapping from frame number to the index withing array
    private Map<Integer, Integer> mappingFrameNumberToIndex;

    // image property from image view that display the current image
    private final ObjectProperty<Image> imageProperty;

    public FrameLoaderBase(final ObjectProperty<Image> imageProperty) {
        this.imageProperty = imageProperty;
        currentFrame = new ReadOnlyObjectWrapper<>();
    }

    private boolean checkFileProperties(Path file) {
        try {
            return !file.toString().startsWith(".") && !Files.isHidden(file) && Files.isRegularFile(file);
        } catch (IOException e) {
            return false;
        }
    }

    public void loadFolder(final Path framesFolder) {
        try {
            allFrames = Files.list(framesFolder)
                    .filter(this::checkFileProperties)
                    .map(path -> path.getFileName().toString())
                    .sorted(Comparator.comparingInt(value -> Integer.parseInt(value.substring(0, value.lastIndexOf('.')))))
                    .toArray(String[]::new);
            loadFolder(framesFolder, allFrames);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFolder(final Path framesFolder, String[] allFrames) {
        if (Files.isDirectory(framesFolder))
            this.framesFolder = framesFolder;
        else throw new RuntimeException(String.format("'%s' is not a directory", framesFolder.toString()));

        if (allFrames != null && allFrames.length > 0)
            this.allFrames = allFrames;
        else throw new RuntimeException("No frames were passed");

        currentFrame.set(null);
        currentIndex = 0;

        imageProperty.bind(Bindings.createObjectBinding(() -> {
            if (currentFrame.get() == null) return null;
            return currentFrame.get().getImage();
        }, currentFrame.getReadOnlyProperty()));

        // create frame filenames array

//        try {
            // TODO: Pre-load in dialog
//            allFrames = Files.list(framesFolder)
//                    .filter(this::checkFileProperties)
//                    .map(path -> path.getFileName().toString())
//                    .sorted(Comparator.comparingInt(value -> Integer.parseInt(value.substring(0, value.lastIndexOf('.')))))
//                    .toArray(String[]::new);

            // create mapping frame number to array index
            mappingFrameNumberToIndex = IntStream.range(0, allFrames.length).boxed().collect(Collectors.toMap(
                    index -> Integer.parseInt(allFrames[index].substring(0, allFrames[index].lastIndexOf('.'))),
                    Function.identity()));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public ReadOnlyObjectProperty<Frame> currentFrameProperty() {
        return currentFrame.getReadOnlyProperty();
    }

    public Path getFramesFolder() {
        return framesFolder;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int index) {
        currentIndex = index;
    }

    public String[] getFramesArray() {
        return allFrames;
    }

    public int getIndex(int frameNumber) {
        return mappingFrameNumberToIndex.get(frameNumber);
    }

    public void setFrame(int index) {
        currentFrame.set(getFrame(index));
    }

    public void setFrame(Frame frame) {
        if (frame != null)
            currentFrame.set(frame);
    }

    public Frame getFrame(int index) {
        return new Frame(framesFolder.resolve(allFrames[index]));
    }

    /**
     * Gets the latest loaded frame.
     *
     * @return current frame
     */
    public Frame getCurrentFrame() {
        return currentFrame.get();
    }

    /**
     * Loads the next frame
     */
    public void nextFrame() {
        if (currentIndex < allFrames.length)
            currentIndex++;
    }

    /**
     * Loads the previous frame
     */
    public void previousFrame() {
        if (currentIndex > 0)
            currentIndex--;
    }

    /**
     * Jump to a arbitrary frame number
     *
     * @param frameNumber number of the frame
     */
    public void jumpToFrame(int frameNumber) { // TODO: For Queue look if frame is in queue
        // if frame number is '0' but the frame number '0' does not exists then use frame number '1'
        if (frameNumber == 0 && !mappingFrameNumberToIndex.containsKey(frameNumber))
            currentIndex = mappingFrameNumberToIndex.get(1);
        else currentIndex = mappingFrameNumberToIndex.get(frameNumber);
    }

    /**
     * Skip forward or backward by a number of frames.
     * To skip backwards specify a negative delta.
     *
     * @param delta number of frame to be skipped
     */
    public void skipFrames(int delta) {
        int idx = currentIndex + delta;
        if (idx < 0) currentIndex = 0;
        else if (idx >= allFrames.length) currentIndex = allFrames.length - 1;
        else currentIndex = idx;
    }
}
