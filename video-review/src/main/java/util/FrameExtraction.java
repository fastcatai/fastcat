package util;

import javafx.concurrent.Task;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class FrameExtraction {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static Task<List<Integer>> getFrameExtractionTask(final Path videoFile, final Path frameFolder,
                                                             final boolean withFreezeDetection) {
        return new Task<>() {
            @Override
            protected List<Integer> call() {
                avgIntensity = new ArrayList<>();
                frames = new ArrayList<>();
                flowNorms = new ArrayList<>();
                prevFrame = null;
                detected = false;
                preFreeze = false;
                List<Integer> superframes = new ArrayList<>();

                VideoCapture cap = new VideoCapture();
                Mat frame = new Mat();
                if (cap.open(videoFile.toString())) {
                    int totalFrames = (int) cap.get(Videoio.CAP_PROP_FRAME_COUNT);
                    int frameNumber = 0;
                    while (cap.read(frame) && !Thread.currentThread().isInterrupted()) {
                        String framePath = frameFolder.resolve(frameNumber + ".png").toString();
                        Imgcodecs.imwrite(framePath, frame);
                        frameNumber++;

                        if (withFreezeDetection && freezeOnlyColor(frame, frameNumber)) {
                            superframes.add(frameNumber);
                        }

                        updateProgress(frameNumber, totalFrames);
                    }
                }

                if (withFreezeDetection)
                    return superframes;
                return null;
            }
        };
    }


    public static Task<List<Integer>> getFreezeDetectionTask(Path frameFolder) {
        return new Task<>() {
            @Override
            protected List<Integer> call() {
                avgIntensity = new ArrayList<>();
                frames = new ArrayList<>();
                flowNorms = new ArrayList<>();
                prevFrame = null;
                detected = false;
                preFreeze = false;
                List<Integer> superframes = new ArrayList<>();

                try (Stream<Path> stream = Files.list(frameFolder)) {
                    long totalFrames = Files.list(frameFolder).count();

                    Iterator<Path> paths = stream.sorted(Comparator.comparingInt(value -> {
                        String filename = value.getFileName().toString();
                        return Integer.parseInt(filename.substring(0, filename.lastIndexOf('.')));
                    })).iterator();

                    // go through each frame
                    while (paths.hasNext() && !Thread.currentThread().isInterrupted()) {
                        Path framePath = paths.next();
                        String filename = framePath.getFileName().toString();
                        int frameNumber = Integer.parseInt(filename.substring(0, filename.lastIndexOf('.')));

                        // read image with BGR channel order
                        Mat frame = Imgcodecs.imread(framePath.toString());

                        if (freezeOnlyColor(frame, frameNumber)) {
                            superframes.add(frameNumber);
                        }
                        updateProgress(frameNumber, totalFrames);
                    }

                } catch (IOException e) {
                    return null;
                }

                return superframes;
            }
        };

    }

    private static List<Double> avgIntensity;
    private static List<Mat> frames;
    private static List<Double> flowNorms;
    private static Mat prevFrame = null;
    private static boolean detected = false;
    private static boolean preFreeze = false;
    private static final int windowSize = 10;


    /**
     * Searches for freezes by looking ate the difference between frame and previous frame.
     * If difference is nearly a black image then we look if movement occurred. Is need because
     * the spatial information gets lost by looking only at the colors. If both images are nearly
     * red then the resulting value could still be very low which could be misinterpreted as a freeze.
     *
     * @param frame       current frame
     * @param frameNumber current frame number
     */
    private static boolean freezeWithOF(Mat frame, int frameNumber) {
        boolean startFreeze = false;

        // get absolute difference between frame and previous frames
        Mat diff = new Mat();
        if (prevFrame != null)
            Core.absdiff(frame, prevFrame, diff);
        else Core.absdiff(frame, Scalar.all(0), diff);

        // Convert matrix of differences into HSV color space.
        // If two frames are nearly the same the the difference is a black image
        // and black images have a very low V-values
        Mat diffHSV = new Mat();
        Imgproc.cvtColor(diff, diffHSV, Imgproc.COLOR_BGR2HSV);

        // Sum up all values, average them and take the norm
        // of S-/V-Values and add it to the list
        Scalar summed = Core.sumElems(diffHSV);
        double avgS = summed.val[1] / (frame.cols() * frame.rows());
        double avgV = summed.val[2] / (frame.cols() * frame.rows());
        double avgI = Math.sqrt(Math.pow(avgS, 2) + Math.pow(avgV, 2));
        avgIntensity.add(avgI);
        frames.add(frame.clone());

        // memory clean up
        diff.release();
        diffHSV.release();

        // Look at a window of averages, which means that within that
        // window all difference should have a small V-value average.
        // To make sure that not only one frame was stuck. A real freeze
        // is defined as a series of frames that do not change.
        if (prevFrame != null && avgIntensity.size() >= windowSize && frames.size() >= windowSize) {
            double winAvg = avgIntensity.stream().reduce(0.0, Double::sum) / avgIntensity.size();

            // Looking on the basis of the color for potential freezes.
            // If the average intensity of the window is below a threshold,
            // then check for spatial movement with optical flow. If the flow
            // is also below a threshold then freeze has started, otherwise we start
            // a pre-freeze phase which means that we wait until no movement occurs.
            if (winAvg <= 80 && !preFreeze && !detected) {
                flowNorms = calcFlowNorms(frames);
                double avgNormFlow = flowNorms.stream().reduce(0.0, Double::sum) / flowNorms.size();

                if (avgNormFlow < 300) {
                    startFreeze = true;
                    detected = true;
                } else preFreeze = true;
            }

            // Pre-Freeze phase: We go into this phase if color threshold is low
            // but we still detect movement. We track movement until it goes below threshold.
            else if (winAvg <= 75 && preFreeze && !detected) {
                double normFlow = flow(prevFrame, frame);
                flowNorms.add(normFlow);
                double avgNormFlow = flowNorms.stream().reduce(0.0, Double::sum) / flowNorms.size();

                if (avgNormFlow < 300) {
                    startFreeze = true;
                    detected = true;
                }
            }

            // Detection phase: When a freeze started we need to track how long the freeze
            // holds so the we can reset the state.
            else if (detected) {
                double normFlow = flow(prevFrame, frame);
                flowNorms.add(normFlow);
                double avgNormFlow = flowNorms.stream().reduce(0.0, Double::sum) / flowNorms.size();

                if (avgNormFlow > 300) {
                    detected = false;
                }
            }

            // Ignore everything else and reset state.
            // When average intensity gets bigger during
            // or before pre-freeze phase.
            else {
                preFreeze = false;
                flowNorms.clear();
            }

            // Drop the last value, since it
            // is not needed in the future.
            avgIntensity.remove(0);
            frames.remove(0).release();
            if (!flowNorms.isEmpty() && flowNorms.size() >= 10)
                flowNorms.remove(0);
        }

        // save prev frame and clean up memory
        if (prevFrame != null)
            prevFrame.release();
        prevFrame = frame.clone();
        frame.release();

        return startFreeze;
    }

    private static boolean freezeOnlyColor(Mat frame, int frameNumber) {
        boolean startFreeze = false;

        // get absolute difference between frame and previous frames
        Mat diff = new Mat();
        if (prevFrame != null)
            Core.absdiff(frame, prevFrame, diff);
        else Core.absdiff(frame, Scalar.all(0), diff);

        // Convert matrix of differences into HSV color space.
        // If two frames are nearly the same the the difference is a black image
        // and black images have a very low V-values
        Mat diffHSV = new Mat();
        Imgproc.cvtColor(diff, diffHSV, Imgproc.COLOR_BGR2HSV);

        // Sum up all s-/V-values, take the average,
        // calc norm and add it to the list
        Scalar summed = Core.sumElems(diffHSV);
        double avgS = summed.val[1] / (frame.cols() * frame.rows());
        double avgV = summed.val[2] / (frame.cols() * frame.rows());
        double avg = Math.sqrt(Math.pow(avgS, 2) + Math.pow(avgV, 2));
        avgIntensity.add(avg);

        // memory clean up
        diff.release();
        diffHSV.release();

        if (prevFrame != null && avgIntensity.size() >= windowSize) {
            double winAvg = avgIntensity.stream().reduce(0.0, Double::sum) / avgIntensity.size();

            // Freeze detected
            if (winAvg <= 50 && !detected) {
                startFreeze = true;
                detected = true;
            }

            // Freeze phase ended
            if (winAvg > 75 && detected) {
                detected = false;
            }

            // Drop the last value, since it
            // is not needed in the future.
            avgIntensity.remove(0);
        }

        // save prev frame and clean up memory
        if (prevFrame != null)
            prevFrame.release();
        prevFrame = frame.clone();
        frame.release();

        return startFreeze;
    }

    private static double flow(Mat prevFrame, Mat frame) {
        Mat flow = new Mat();
        Mat frameGray = new Mat();
        Mat prevFrameGray = new Mat();
        Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(prevFrame, prevFrameGray, Imgproc.COLOR_BGR2GRAY);
        Video.calcOpticalFlowFarneback(prevFrameGray, frameGray, flow, 0.5, 3, 15, 3, 5, 1.2, 0);
        double normFlow = Core.norm(flow);
        flow.release();
        frameGray.release();
        prevFrameGray.release();
        return normFlow;
    }

    private static List<Double> calcFlowNorms(List<Mat> frames) {
        List<Double> norms = new ArrayList<>();
        for (int i = 1; i < frames.size(); i++) {
            norms.add(flow(frames.get(i - 1), frames.get(i)));
        }
        return norms;
    }
}
