package util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Utils {

    public static int getFrameNumber(String filename) {
        return Integer.parseInt(filename.substring(0, filename.lastIndexOf('.')));
    }

    public static List<Integer> generateFramesNumbers(double startMs, double endMs, double fps) {
        if (fps <= 0 || startMs < endMs)
            return List.of();

        int startFrame = (int) Math.ceil((startMs / 1000.0) * fps);
        int endFrame = (int) Math.ceil((endMs / 1000.0) * fps);
        return IntStream.rangeClosed(startFrame, endFrame).boxed().collect(Collectors.toList());
    }
}
