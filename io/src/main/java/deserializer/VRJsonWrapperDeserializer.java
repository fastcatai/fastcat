package deserializer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import model.ImageData;
import wrapper.VRJsonWrapper;
import model.SuperframeData;
import wrapper.VRMetadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VRJsonWrapperDeserializer extends StdDeserializer<VRJsonWrapper> {

    public VRJsonWrapperDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public VRJsonWrapper deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode root = ctxt.readTree(p);

        List<SuperframeData> superframes = new ArrayList<>();
        for (JsonNode sf : root.get("superframes")) {
            JsonParser sfParser = JsonFactory.builder().build().createParser(sf.toString());
            sfParser.setCodec(p.getCodec());
            SuperframeData sfData = ctxt.readValue(sfParser,SuperframeData.class);
            superframes.add(sfData);
        }

        JsonNode md = root.get("metadata");
        JsonParser metaParser = JsonFactory.builder().build().createParser(md.toString());
        metaParser.setCodec(p.getCodec());
        VRMetadata metadata = ctxt.readValue(metaParser, VRMetadata.class);

        List<ImageData> images = new ArrayList<>();
        for (JsonNode id : root.get("images")) {
            JsonParser idParser = JsonFactory.builder().build().createParser(id.toString());
            idParser.setCodec(p.getCodec());
            idParser.setCurrentValue(metadata.getImageFolder());
            ImageData imageData = ctxt.readValue(idParser, ImageData.class);
            images.add(imageData);
        }

        return new VRJsonWrapper(superframes, images, metadata);
    }
}
