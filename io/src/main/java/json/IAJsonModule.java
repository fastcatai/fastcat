package json;

import annotation.UIAnnotation;
import boundingbox.SimpleBoundingBoxV3;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.util.StdConverter;
import deserializer.BoundingBoxDeserializer;
import deserializer.IAJsonWrapperDeserializer;
import deserializer.IAMetadataDeserializer;
import deserializer.ImageDataDeserializer;
import model.ImageData;
import modelV3.Annotation;
import modelV3.BoundingBox;
import serializer.AnnotationSerializer;
import serializer.BoundingBoxSerializer;
import serializer.ImageDataSerializer;
import wrapper.IAJsonWrapper;
import wrapper.IAMetadata;

public class IAJsonModule extends SimpleModule {

    private final SimpleSerializers serializers;
    private final SimpleDeserializers deserializers;
    private final BoundingBoxFormat bbFormat;

    public IAJsonModule() {
        this(null);
    }

    public IAJsonModule(BoundingBoxFormat bbFormat) {
        this.bbFormat = bbFormat;
        serializers = new SimpleSerializers();
        deserializers = new SimpleDeserializers();
    }

    private final StdConverter<UIAnnotation<?>, Annotation> uiAnnotationConverter = new StdConverter<>() {
        @Override
        public Annotation convert(UIAnnotation<?> value) {
            return value.getAnnotationModel();
        }
    };

    @Override
    public void setupModule(SetupContext context) {
        serializers.addSerializer(ImageData.class, new ImageDataSerializer(ImageData.class));
        serializers.addSerializer(UIAnnotation.class, new StdDelegatingSerializer(uiAnnotationConverter));
        serializers.addSerializer(Annotation.class, new AnnotationSerializer(Annotation.class));
        serializers.addSerializer(BoundingBox.class, new BoundingBoxSerializer<>(BoundingBox.class, bbFormat));

        deserializers.addDeserializer(IAJsonWrapper.class, new IAJsonWrapperDeserializer(IAJsonWrapper.class));
        deserializers.addDeserializer(ImageData.class, new ImageDataDeserializer(ImageData.class));
        deserializers.addDeserializer(SimpleBoundingBoxV3.class, new BoundingBoxDeserializer(SimpleBoundingBoxV3.class));
        deserializers.addDeserializer(IAMetadata.class, new IAMetadataDeserializer(IAMetadata.class));

        context.addSerializers(serializers);
        context.addDeserializers(deserializers);

    }
}
