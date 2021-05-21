package serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import model.FrameData;

import java.io.IOException;
import java.util.Objects;

public class FrameDataSerializer extends StdSerializer<FrameData> {

    public FrameDataSerializer(Class<FrameData> t) {
        super(t);
    }

    @Override
    public void serialize(FrameData value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("frameNumber", value.getFrameNumber());
        gen.writeStringField("comment", Objects.toString(value.getComment(), ""));
        provider.defaultSerializeField("classes", value.getClasses(), gen);
        gen.writeEndObject();
    }
}
