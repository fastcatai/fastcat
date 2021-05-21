package deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import model.FrameData;
import model.SuperframeData;

import java.io.IOException;

public class FrameDataDeserializer extends StdDeserializer<FrameData> {

    public FrameDataDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public FrameData deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode root = ctxt.readTree(p);
        FrameData frameData = new FrameData((SuperframeData) p.getCurrentValue(),
                root.get("frameNumber").intValue());
        frameData.setComment(root.get("comment").textValue());
        for (JsonNode c : root.withArray("classes"))
            frameData.setNewClasses(c.textValue());
        return frameData;
    }
}
