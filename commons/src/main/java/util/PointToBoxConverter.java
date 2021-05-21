package util;

import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.Rectangle;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PointToBoxConverter {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static final int[] BACKGROUND_VALUE = {1};
    private static final int[] CIRCLE_VALUE = {2};

    public static Rectangle getBoundingBox(String imagePath, int centerX, int centerY, int radius) {
        // read image to Mat
        Mat image = Imgcodecs.imread(imagePath, Imgcodecs.IMREAD_COLOR);
        return getBoundingBox(image, centerX, centerY, radius);
    }

    public static Rectangle getBoundingBox(Mat image, int centerX, int centerY, int radius) {
        // get markers template
        Mat markers = initMarkerMap(image.cols(), image.rows());
        // add the dot to the markers map
        addCircleToMat(centerX, centerY, radius, markers);
        // apply watershed
        Imgproc.watershed(image, markers);
        // convert to different type: -1 is converted to 0
        Mat binaryMask = new Mat();
        markers.convertTo(binaryMask, CvType.CV_8UC1);
        // thresholding: background (incl. border) pixels are '0', segmented pixels are '2'
        Imgproc.threshold(binaryMask, binaryMask, 1, 2, Imgproc.THRESH_BINARY); // convert to binary image
        // find contour of segmented area and only take the external boxes
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binaryMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        if (contours.isEmpty())
            return null;

        // because we only have one point there is usually only one contour in the list
        MatOfPoint contour = contours.get(0); // get the first
        double perimeter  = Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true);
        MatOfPoint2f approximation = new MatOfPoint2f();
        Imgproc.approxPolyDP(new MatOfPoint2f(contour.toArray()), approximation, 0.01 * perimeter, true);
        Rect rect = Imgproc.boundingRect(approximation);
        System.out.println(rect.x + ", " + rect.y + ", " + rect.width + ", " + rect.height);
        return new Rectangle(rect.x, rect.y, rect.width, rect.height);
    }

    private static Mat initMarkerMap(int width, int height) {
        // create matrix with zeros
        Mat markers = new Mat(height, width, CvType.CV_32SC1, Scalar.all(0));

        // add border left and right background markers
        int[] cols = {50, markers.cols() - 51};
        for (int c : cols) {
            for (int r = 0; r < markers.rows(); r++) {
                markers.put(r, c, BACKGROUND_VALUE);
            }
        }

        // add top und bottom background markers
        int[] rows = {50, markers.rows() - 51};
        for (int r : rows) {
            for (int c = 0; c < markers.cols(); c++) {
                markers.put(r, c, BACKGROUND_VALUE);
            }
        }

        return markers;
    }

    private static void addCircleToMat(int centerX, int centerY, int radius, Mat mat) {
        // circle pixels are the set of P={(x,y) | (x-m1)^2 + (y-m2)^2 <= r^2}, where (m1, m2) is circle center
        // TODO: Can be optimized by only iterating over the pixels in the bounding rectangle of the circle
        double radiusSquared = Math.pow(radius, 2);
        for (int r = 0; r < mat.rows(); r++) {
            for (int c = 0; c < mat.cols(); c++) {
                double distanceSquared = Math.pow(c - centerX, 2) + Math.pow(r - centerY, 2);
                if (distanceSquared <= radiusSquared) {
                    mat.put(r, c, CIRCLE_VALUE);
                }
            }
        }
    }

    public static Mat convertToMat(WritableImage writableImage) {
        Mat image = new Mat((int) writableImage.getHeight(), (int) writableImage.getWidth(), CvType.CV_8UC3, Scalar.all(0));
        PixelReader pixelReader = writableImage.getPixelReader();
        for (int y = 0; y < writableImage.getHeight(); y++) {
            for (int x = 0; x < writableImage.getWidth(); x++) {
                int argb = pixelReader.getArgb(x, y);
                byte b = (byte) (argb & 0xFF);
                byte g = (byte) ((argb >> 8) & 0xFF);
                byte r = (byte) ((argb >> 16) & 0xFF);
//                byte a = (byte) ((argb >> 24) & 0xFF);
                image.put(y, x, new byte[]{b, g, r});
            }
        }
//        Imgcodecs.imwrite("D:/Users/kevin/Desktop/mat.png", image);
        return image;
    }

    public static Mat bufferedImage2Mat(BufferedImage in)
    {
        Mat out;
        byte[] data;
        int r, g, b;
        int height = in.getHeight();
        int width = in.getWidth();
        if(in.getType() == BufferedImage.TYPE_INT_RGB || in.getType() == BufferedImage.TYPE_INT_ARGB)
        {
            out = new Mat(height, width, CvType.CV_8UC3);
            data = new byte[height * width * (int)out.elemSize()];
            int[] dataBuff = in.getRGB(0, 0, width, height, null, 0, width);
            for(int i = 0; i < dataBuff.length; i++)
            {
                data[i*3 + 2] = (byte) ((dataBuff[i] >> 16) & 0xFF);
                data[i*3 + 1] = (byte) ((dataBuff[i] >> 8) & 0xFF);
                data[i*3] = (byte) ((dataBuff[i] >> 0) & 0xFF);
            }
        }
        else
        {
            out = new Mat(height, width, CvType.CV_8UC1);
            data = new byte[height * width * (int)out.elemSize()];
            int[] dataBuff = in.getRGB(0, 0, width, height, null, 0, width);
            for(int i = 0; i < dataBuff.length; i++)
            {
                r = (byte) ((dataBuff[i] >> 16) & 0xFF);
                g = (byte) ((dataBuff[i] >> 8) & 0xFF);
                b = (byte) ((dataBuff[i] >> 0) & 0xFF);
                data[i] = (byte)((0.21 * r) + (0.71 * g) + (0.07 * b)); //luminosity
            }
        }
        out.put(0, 0, data);
        return out;
    }

    /**
     * Used for debugging purposes.
     */
    private static void printMat(Mat mat, boolean toFile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("{output/file}"))){
            for (int r = 0; r < mat.rows(); r++) {
                for (int c = 0; c < mat.cols(); c++) {
                    if (toFile) writer.write(Arrays.toString(mat.get(r, c)));
                    else System.out.print(Arrays.toString(mat.get(r, c)));
                }
                if (toFile) System.out.println();
                else writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        PointToBoxConverter.getBoundingBox(
//                "{path/to/image}",
//                550, 500, 7);
//        Image image = new Image("file:D:/Users/kevin/Desktop/uniklinikum/uniklinikum-endo/uniklinikum-endo_dl_3.mkv/sub/18185.png");
//        WritableImage writableImage = new WritableImage(image.getPixelReader(), (int) image.getWidth(), (int) image.getHeight());
//        Mat img = PointToBoxConverter.convertToMat(writableImage);
//        Rectangle rect = PointToBoxConverter.getBoundingBox(img, 1380, 650, 7);
        Rectangle rect = PointToBoxConverter.getBoundingBox(
                "D:/Users/kevin/Desktop/uniklinikum/uniklinikum-endo/uniklinikum-endo_dl_3.mkv/sub/18185.png", 1380, 650, 7);
        System.out.println(rect);
    }
}
