package util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.LookupOp;
import java.awt.image.RenderedImage;
import java.awt.image.ShortLookupTable;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ImageProcessing {
    private static final short[] INVERT = new short[256];

    static {
        for (int i = 0; i < 256; i++)
            INVERT[i] = (short) (255 - i);
    }

    public static BufferedImage frameImage(BufferedImage image) {
        BufferedImage framedImage = new BufferedImage(
                image.getWidth() + 2,
                image.getHeight() + 2,
                image.getType());
        framedImage.createGraphics().drawImage(image, null, 1, 1);
        return framedImage;
    }

    public static BufferedImage invert(BufferedImage image) {
        BufferedImageOp invertOp = new LookupOp(new ShortLookupTable(0, INVERT), null);
        BufferedImage inverted = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        invertOp.filter(image, inverted);
        return inverted;
    }

    public static void drawMask(double width, double height, List<Double> points) {
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.color(1, 1, 1));
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);
        gc.beginPath();
        int countPoints = points.size() / 2;
        double[] x = new double[countPoints];
        double[] y = new double[countPoints];
        for (int i = 0, xIdx = 0, yIdx = 0; i < points.size(); i++) {
            if ((i % 2) == 0)
                x[xIdx++] = points.get(i);
            else y[yIdx++] = points.get(i);
        }
        gc.fillPolygon(x, y, countPoints);
        exportMask(canvas);
    }

    private static void exportMask(Canvas canvas) {
        try {
            WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            SnapshotParameters paras = new SnapshotParameters();
            paras.setFill(Color.BLACK);
            canvas.snapshot(paras, writableImage);
            RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
            ImageIO.write(renderedImage, "png", new File("mask.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
