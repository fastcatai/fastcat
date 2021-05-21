package deserializer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import model.ImageData;
import wrapper.IAJsonWrapper;
import wrapper.IAMetadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IAJsonWrapperDeserializer extends StdDeserializer<IAJsonWrapper> {

    public IAJsonWrapperDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public IAJsonWrapper deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode root = ctxt.readTree(p);

        JsonNode md = root.get("metadata");
        JsonParser metaParser = JsonFactory.builder().build().createParser(md.toString());
        metaParser.setCodec(p.getCodec());
        IAMetadata metadata = ctxt.readValue(metaParser, IAMetadata.class);

        List<ImageData> images = new ArrayList<>();
        for (JsonNode id : root.get("images")) {
            JsonParser idParser = JsonFactory.builder().build().createParser(id.toString());
            idParser.setCodec(p.getCodec());
            idParser.setCurrentValue(metadata.getImageFolder());
            ImageData imageData = ctxt.readValue(idParser, ImageData.class);
            images.add(imageData);
        }

        return new IAJsonWrapper(images, metadata);
    }
}
