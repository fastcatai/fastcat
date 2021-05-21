package util;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OpenCVPolyApprox {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private List<MatOfPoint> contours;
    private double epsilon;

    public void loadImage(String pathToMask) {
        Mat src = Imgcodecs.imread(pathToMask, Imgcodecs.IMREAD_GRAYSCALE);
        contours = this.findContours(src);
        this.calculateEpsilon();
    }

    private void calculateEpsilon() {
        double epsilon = 0;
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(new MatOfPoint2f(contour.toArray()));
            if (area < 10)
                continue;

            double arc = Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true); // contour perimeter
            System.out.println("Arc: " + arc);
            System.out.println("Area: " + area);
            System.out.println();
            double ratio = area / arc;
            double eps = 0.1 * ratio;
            epsilon += eps;
        }
        this.epsilon = Double.parseDouble(String.format(Locale.ROOT, "%.2f", (epsilon / contours.size())));
    }

    public List<List<Double>> detect(double epsilon) {
        List<List<Double>> points = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(new MatOfPoint2f(contour.toArray()));
            if (area < 10)
                continue;

            // Approximate polygon
            MatOfPoint2f approximation = new MatOfPoint2f();
            Imgproc.approxPolyDP(new MatOfPoint2f(contour.toArray()), approximation, epsilon, true);
            // Get approximated points
            Point[] approxPoints = approximation.toArray();
            List<Double> polyPoints = new ArrayList<>(approxPoints.length * 2);
            for (Point p : approxPoints) {
                polyPoints.add(p.x);
                polyPoints.add(p.y);
            }
            points.add(polyPoints);
        }
        return points;
    }

    private List<MatOfPoint> findContours(Mat image) {
        Mat dest = new Mat();
        Imgproc.threshold(image, dest, 200, 255, Imgproc.THRESH_BINARY);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(dest, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;
    }

    public double getEpsilon() {
        return epsilon;
    }
}
