package json;

import annotation.UIAnnotation;
import boundingbox.SimpleBoundingBoxV3;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.util.StdConverter;
import deserializer.BoundingBoxDeserializer;
import deserializer.FrameDataDeserializer;
import deserializer.ImageDataDeserializer;
import deserializer.SuperframeDataDeserializer;
import deserializer.VRJsonWrapperDeserializer;
import deserializer.VRMetadataDeserializer;
import model.FrameData;
import model.ImageData;
import model.SuperframeData;
import modelV3.Annotation;
import modelV3.BoundingBox;
import serializer.AnnotationSerializer;
import serializer.BoundingBoxSerializer;
import serializer.FrameDataSerializer;
import serializer.ImageDataSerializer;
import serializer.SuperframeDataSerializer;
import wrapper.VRJsonWrapper;
import wrapper.VRMetadata;

public class VRJsonModule extends SimpleModule {

    private final SimpleSerializers serializers;
    private final SimpleDeserializers deserializers;
    private final BoundingBoxFormat bbFormat;

    public VRJsonModule() {
        this(null);
    }

    public VRJsonModule(BoundingBoxFormat bbFormat) {
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
        serializers.addSerializer(SuperframeData.class, new SuperframeDataSerializer(SuperframeData.class));
        serializers.addSerializer(FrameData.class, new FrameDataSerializer(FrameData.class));
        serializers.addSerializer(ImageData.class, new ImageDataSerializer(ImageData.class));
        serializers.addSerializer(UIAnnotation.class, new StdDelegatingSerializer(uiAnnotationConverter));
        serializers.addSerializer(Annotation.class, new AnnotationSerializer(Annotation.class));
        serializers.addSerializer(BoundingBox.class, new BoundingBoxSerializer<>(BoundingBox.class, bbFormat));

        deserializers.addDeserializer(VRJsonWrapper.class, new VRJsonWrapperDeserializer(VRJsonWrapper.class));
        deserializers.addDeserializer(SuperframeData.class, new SuperframeDataDeserializer(SuperframeData.class));
        deserializers.addDeserializer(FrameData.class, new FrameDataDeserializer(FrameData.class));
        deserializers.addDeserializer(ImageData.class, new ImageDataDeserializer(ImageData.class));
        deserializers.addDeserializer(SimpleBoundingBoxV3.class, new BoundingBoxDeserializer(SimpleBoundingBoxV3.class));
        deserializers.addDeserializer(VRMetadata.class, new VRMetadataDeserializer(VRMetadata.class));

        context.addSerializers(serializers);
        context.addDeserializers(deserializers);
    }
}
