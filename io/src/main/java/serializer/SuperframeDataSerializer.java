package serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import model.SuperframeData;

import java.io.IOException;

public class SuperframeDataSerializer extends StdSerializer<SuperframeData> {

    public SuperframeDataSerializer(Class<SuperframeData> t) {
        super(t);
    }

    @Override
    public void serialize(SuperframeData value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("frameNumber", value.getFrameNumber());
        provider.defaultSerializeField("childFrames", value.getChildFrames(), gen);
        gen.writeEndObject();
    }
}
