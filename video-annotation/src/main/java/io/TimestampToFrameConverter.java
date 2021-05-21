package io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TimestampToFrameConverter {

    public static void videoToFrames(JsonNode root, File file) {

        double fps = root.get("fps").doubleValue();
        double duration = root.get("duration").doubleValue();
        int imageWidth = root.get("width").intValue();
        int imageHeight = root.get("height").intValue();
        double msPerFrame = 1000.0 / fps;


        Map<Integer, ArrayNode> imageBoxes = new HashMap<>();

        Iterator<JsonNode> iter = root.get("annotations").elements();
        while (iter.hasNext()) {
            JsonNode annotation = iter.next();
            double startTime = annotation.get("startTime").doubleValue();
            double endTime = annotation.get("endTime").doubleValue();
            int startFrame = (int) (startTime / msPerFrame);
            int endFrame = (int) (endTime / msPerFrame);

            String annotationType = annotation.get("type").asText();
            int x = 0, y = 0, boxWidth = 0, boxHeight = 0;
            if (annotationType.equals("BOX")) {
                x = annotation.get("x").intValue();
                y = annotation.get("y").intValue();
                boxWidth = annotation.get("width").intValue();
                boxHeight = annotation.get("height").intValue();
            } else if (annotationType.equals("POINT")) {
                // TODO: convert point to bounding box via watershed algorithm
            } else {
                System.err.println("Annotation type not known: " +  annotationType);
            }

            String annotationLabel = annotation.get("label").asText();
            String annotationInstance = annotation.get("instance").asText();

            // go through all frames within time interval
            for (int frame = startFrame; frame <= endFrame; frame++) {
                ArrayNode boxes;
                if (imageBoxes.containsKey(frame)) {
                    boxes = imageBoxes.get(frame);
                } else {
                    boxes = JsonNodeFactory.instance.arrayNode();
                    imageBoxes.put(frame, boxes);
                }
                ObjectNode boxObj = boxes.addObject();
                boxObj.set("x", JsonNodeFactory.instance.numberNode(x));
                boxObj.set("y", JsonNodeFactory.instance.numberNode(y));
                boxObj.set("width", JsonNodeFactory.instance.numberNode(boxWidth));
                boxObj.set("height", JsonNodeFactory.instance.numberNode(boxHeight));
                boxObj.set("label", JsonNodeFactory.instance.textNode(annotationLabel));
                boxObj.set("instance", JsonNodeFactory.instance.textNode(annotationInstance));
            }
        }


        ObjectNode imagesRoot = JsonNodeFactory.instance.objectNode();
        ArrayNode images = JsonNodeFactory.instance.arrayNode();
        imagesRoot.set("images", images);

        Integer[] keys = imageBoxes.keySet().stream()
                .sorted(Comparator.comparingInt(value -> value))
                .toArray(Integer[]::new);
        for (int frameNumber : keys) {
            ObjectNode img = images.addObject();
            img.set("filename", JsonNodeFactory.instance.textNode(frameNumber + ".png"));
            img.set("seg_mask", JsonNodeFactory.instance.textNode(""));
            img.set("width", JsonNodeFactory.instance.numberNode(imageWidth));
            img.set("height", JsonNodeFactory.instance.numberNode(imageHeight));
            img.set("boundingBox", imageBoxes.get(frameNumber));
        }

        // create metadata
        ObjectNode metadata = JsonNodeFactory.instance.objectNode();
        metadata.set("boxFormat", JsonNodeFactory.instance.textNode("voc"));
        metadata.set("imageFolder", JsonNodeFactory.instance.textNode("frames"));
        imagesRoot.set("metadata", metadata);


        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, imagesRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        ObjectMapper mapper = new ObjectMapper();
//        JsonNode root = mapper.readTree(new File("D:\\Users\\kevin\\Desktop\\uniklinikum-endo_dl_3.json"));
//        videoToFrames(root);

        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 2);
        map.put(100, 2);
        map.put(111, 2);
        map.put(9, 2);
        map.put(11, 2);
        map.put(10, 2);

        Integer[] keys = map.keySet().stream()
                .sorted(Comparator.comparingInt(value -> value))
                .toArray(Integer[]::new);
        for (Object k : keys) {
            System.out.println(k);
        }
    }
}
