package serializer;

import annotation.UIAnnotation;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import model.ImageData;

import java.io.IOException;


public class ImageDataSerializer extends StdSerializer<ImageData> {

    public ImageDataSerializer(Class<ImageData> t) {
        super(t);
    }

    @Override
    public void serialize(ImageData value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        gen.writeStringField("filename", value.getFilename());
        gen.writeNumberField("width", value.getWidth());
        gen.writeNumberField("height", value.getHeight());
        gen.writeStringField("label", value.getImageClassification().getLabel());
        gen.writeStringField("instance", value.getImageClassification().getInstance());
        provider.defaultSerializeField("additionalLabels", value.getImageClassification().getAdditionalLabels(), gen);
        gen.writeBooleanField("verified", value.getImageClassification().isVerified());
        gen.writeBooleanField("autoCreated", value.getImageClassification().isAutoCreated());
        provider.defaultSerializeField("annotations", value.getAnnotations(), gen);

        gen.writeEndObject();
    }
}
