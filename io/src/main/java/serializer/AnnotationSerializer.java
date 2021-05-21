package serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import modelV3.Annotation;

import java.io.IOException;

public class AnnotationSerializer extends StdSerializer<Annotation> {

    public AnnotationSerializer(Class<Annotation> t) {
        super(t);
    }

    @Override
    public void serialize(Annotation value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        provider.defaultSerializeValue(value, gen);
    }
}
