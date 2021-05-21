package deserializer;

import boundingbox.SimpleBoundingBoxV3;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import javafx.scene.layout.Pane;
import model.ImageData;
import modelV3.BoundingBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class BoundingBoxDeserializer extends StdDeserializer<SimpleBoundingBoxV3> {
    private static final Logger logger = LogManager.getLogger(BoundingBoxDeserializer.class);

    public BoundingBoxDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public SimpleBoundingBoxV3 deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode root = ctxt.readTree(p);

        BoundingBox boundingBox = new BoundingBox(root.get("x").intValue(), root.get("y").intValue(),
                root.get("width").intValue(), root.get("height").intValue(),
                root.get("label").textValue(), root.get("instance").textValue(),
                root.get("verified").booleanValue(), root.get("autoCreated").booleanValue());

        for (JsonNode l : root.withArray("additionalLabels"))
            boundingBox.addAdditionalLabel(l.textValue());

        ImageData parentImageData = (ImageData) p.getCurrentValue();
        Pane annotationPane = null;

        try {
            annotationPane = (Pane) ctxt.findInjectableValue("annotationPane", null, null);
        } catch (InvalidDefinitionException e) {
            logger.error(e);
        }

        SimpleBoundingBoxV3 uiBox = new SimpleBoundingBoxV3(annotationPane, boundingBox, true,
                parentImageData.getWidth(), parentImageData.getHeight());
        uiBox.imported();
        return uiBox;
    }
}
