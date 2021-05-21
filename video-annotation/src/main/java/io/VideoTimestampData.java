//package ui.videoannotation.io;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ArrayNode;
//import com.fasterxml.jackson.databind.node.JsonNodeFactory;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import javafx.collections.ObservableList;
//import annotation.ui.AnnotationVideoUI;
//import annotation.ui.BoundingBoxVideoUI;
//import annotation.ui.PointUI;
//import annotation.ui.PointVideoUI;
//import org.opencv.core.Core;
//import org.opencv.core.CvType;
//import org.opencv.core.Mat;
//import videoplayer.VideoInfo;
//
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//
//public class VideoTimestampData {
//
//    static {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//    }
//
//    public static void load() {
//
//    }
//
//    public static void save(ObservableList<AnnotationVideoUI> videoAnnotations, double videoDuration, VideoInfo videoInfo, File outputFile) {
//        ObjectNode root = JsonNodeFactory.instance.objectNode();
//        root.set("filename", JsonNodeFactory.instance.textNode(videoInfo.getVideoFileName()));
//        root.set("fps", JsonNodeFactory.instance.numberNode(videoInfo.getFps()));
//        root.set("duration", JsonNodeFactory.instance.numberNode(videoDuration));
//        root.set("width", JsonNodeFactory.instance.numberNode(videoInfo.getResolutionWidth()));
//        root.set("height", JsonNodeFactory.instance.numberNode(videoInfo.getResolutionHeight()));
//
//        ArrayNode boxes = JsonNodeFactory.instance.arrayNode();
//        for (AnnotationVideoUI annotation : videoAnnotations) {
//            ObjectNode box = boxes.addObject();
//            box.set("startTime", JsonNodeFactory.instance.numberNode(annotation.getTimestampStartMillis()));
//            box.set("endTime", JsonNodeFactory.instance.numberNode(annotation.getTimestampEndMillis()));
//
//            if (annotation instanceof BoundingBoxVideoUI) {
//                BoundingBoxVideoUI boxUI = ((BoundingBoxVideoUI) annotation);
//                box.set("type", JsonNodeFactory.instance.textNode("BOX"));
//                box.set("x", JsonNodeFactory.instance.numberNode(boxUI.getTrueX()));
//                box.set("y", JsonNodeFactory.instance.numberNode(boxUI.getTrueY()));
//                box.set("width", JsonNodeFactory.instance.numberNode(boxUI.getTrueWidth()));
//                box.set("height", JsonNodeFactory.instance.numberNode(boxUI.getTrueHeight()));
//                box.set("label", JsonNodeFactory.instance.textNode(boxUI.getLabel()));
//                box.set("instance", JsonNodeFactory.instance.textNode(boxUI.getInstance()));
//            } else if (annotation instanceof PointUI) {
//                PointVideoUI pointUI = ((PointVideoUI) annotation);
////                box.set("type", JsonNodeFactory.instance.textNode("POINT"));
////                box.set("x", JsonNodeFactory.instance.numberNode(pointUI.getTrueX()));
////                box.set("y", JsonNodeFactory.instance.numberNode(pointUI.getTrueY()));
////                box.set("radius", JsonNodeFactory.instance.numberNode(pointUI.getTrueRadius()));
////                box.set("label", JsonNodeFactory.instance.textNode(pointUI.getLabel()));
////                box.set("instance", JsonNodeFactory.instance.textNode(pointUI.getInstance()));
//
////                WritableImage image = pointUI.getImage();
////                Mat img = PointToBoxConverter.convertToMat(image);
////                Imgcodecs.imwrite("D:/Users/kevin/Desktop/mat1.png", img);
////                Rectangle rect = PointToBoxConverter.getBoundingBox(img, (int) pointUI.getX(), (int) pointUI.getY(), (int) pointUI.getRadius());
//
//                box.set("type", JsonNodeFactory.instance.textNode("BOX"));
//                box.set("x", JsonNodeFactory.instance.numberNode(pointUI.getTrueRect().getX()));
//                box.set("y", JsonNodeFactory.instance.numberNode(pointUI.getTrueRect().getY()));
//                box.set("width", JsonNodeFactory.instance.numberNode(pointUI.getTrueRect().getWidth()));
//                box.set("height", JsonNodeFactory.instance.numberNode(pointUI.getTrueRect().getHeight()));
//                box.set("label", JsonNodeFactory.instance.textNode(pointUI.getLabel()));
//                box.set("instance", JsonNodeFactory.instance.textNode(pointUI.getInstance()));
//            }
//        }
//        root.set("annotations", boxes);
//
//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, root);
//            File imageFramesJson = new File(outputFile.getParentFile(),
//                    videoInfo.getVideoFileName().substring(0, videoInfo.getVideoFileName().lastIndexOf('.')) + ".frames.json");
//            TimestampToFrameConverter.videoToFrames(root, imageFramesJson);
//        } catch (
//                IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    private static Mat img2Mat(BufferedImage in) {
//        Mat out;
//        byte[] data;
//        int r, g, b;
//
//        if (in.getType() == BufferedImage.TYPE_INT_RGB) {
//            out = new Mat(in.getHeight(), in.getWidth(), CvType.CV_8UC3);
//            data = new byte[in.getWidth() * in.getHeight() * (int) out.elemSize()];
//            int[] dataBuff = in.getRGB(0, 0, in.getWidth(), in.getHeight(), null, 0, in.getWidth());
//            for (int i = 0; i < dataBuff.length; i++) {
//                data[i * 3] = (byte) ((dataBuff[i] >> 0) & 0xFF);
//                data[i * 3 + 1] = (byte) ((dataBuff[i] >> 8) & 0xFF);
//                data[i * 3 + 2] = (byte) ((dataBuff[i] >> 16) & 0xFF);
//            }
//        } else {
//            out = new Mat(in.getHeight(), in.getWidth(), CvType.CV_8UC1);
//            data = new byte[in.getWidth() * in.getHeight() * (int) out.elemSize()];
//            int[] dataBuff = in.getRGB(0, 0, in.getWidth(), in.getHeight(), null, 0, in.getWidth());
//            for (int i = 0; i < dataBuff.length; i++) {
//                r = (byte) ((dataBuff[i] >> 0) & 0xFF);
//                g = (byte) ((dataBuff[i] >> 8) & 0xFF);
//                b = (byte) ((dataBuff[i] >> 16) & 0xFF);
//                data[i] = (byte) ((0.21 * r) + (0.71 * g) + (0.07 * b));
//            }
//        }
//        out.put(0, 0, data);
//        return out;
//    }
//}
