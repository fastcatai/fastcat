package deserializer;

import boundingbox.SimpleBoundingBoxV3;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import model.ImageData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ui.Dialogs;
import wrapper.VRMetadata;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;

public class ImageDataDeserializer extends StdDeserializer<ImageData> {
    private static final Logger logger = LogManager.getLogger(ImageDataDeserializer.class);


    public ImageDataDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ImageData deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode root = ctxt.readTree(p);
        String filename = root.get("filename").textValue();
        int imgWidth = root.get("width").intValue();
        int imgHeight = root.get("height").intValue();

        try {
            // Get frame folder from metadata
            Path jsonFilePath = (Path) ctxt.findInjectableValue("jsonFilePath", null, null);
            Path imageFolder;
            String imageFolderPath = (String) p.getCurrentValue();
            if (imageFolderPath != null && !imageFolderPath.isBlank())
                imageFolder = jsonFilePath.getParent().resolve(imageFolderPath);
            else throw logger.throwing(new RuntimeException("Image folder could not be found"));

            ImageData imageData = new ImageData(imageFolder.resolve(filename), imgWidth, imgHeight);
            imageData.getImageClassification().setLabel(root.get("label").textValue());
            imageData.getImageClassification().setInstance(root.get("instance").textValue());
            for (JsonNode l : root.withArray("additionalLabels"))
                imageData.getImageClassification().addAdditionalLabel(l.textValue());
            imageData.getImageClassification().setVerified(root.get("verified").booleanValue());
            imageData.getImageClassification().setAutoCreated(root.get("autoCreated").booleanValue());

            for (JsonNode a : root.withArray("annotations")) {
                JsonParser parser = JsonFactory.builder().build().createParser(a.toString());
                parser.setCodec(p.getCodec());
                parser.setCurrentValue(imageData);
                if (a.get("type").textValue().equals("boundingBox")) {
                    SimpleBoundingBoxV3 simpleBoundingBox = ctxt.readValue(parser, SimpleBoundingBoxV3.class);
                    imageData.addAnnotation(simpleBoundingBox);
                }
            }

            return imageData;

        } catch (InvalidDefinitionException e) {
            logger.error("Path to the image folder is needed to deserialize ImageData objects.", e);
            return null;
        } catch (UnsupportedOperationException e) {
            logger.throwing(e);
            Dialogs.showExceptionDialog("JSON Loading", e);
            return null;
        }
    }
}
