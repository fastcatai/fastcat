package deserializer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import model.FrameData;
import model.SuperframeData;

import java.io.IOException;

public class SuperframeDataDeserializer extends StdDeserializer<SuperframeData> {

    public SuperframeDataDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public SuperframeData deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode root = ctxt.readTree(p);
        int frameNumber = root.get("frameNumber").intValue();
        SuperframeData sfData = new SuperframeData(frameNumber);

        for (JsonNode c : root.get("childFrames")) {
            JsonParser parser = JsonFactory.builder().build().createParser(c.toString());
            parser.setCodec(p.getCodec());
            parser.setCurrentValue(sfData);
            FrameData frameData = ctxt.readValue(parser, FrameData.class);
            sfData.addChildFrame(frameData);
        }

        return sfData;
    }
}
