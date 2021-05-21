package json;

import annotation.UIAnnotation;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.util.StdConverter;
import modelV3.Annotation;
import modelV3.BoundingBoxVideo;
import modelV3.PointVideo;
import serializer.AnnotationSerializer;
import serializer.BoundingBoxSerializer;
import serializer.PointSerializer;
import videoplayer.VideoInfo2;

public class VAJsonModule extends SimpleModule {

    private final SimpleSerializers serializers = new SimpleSerializers();
    private final VideoInfo2 videoInfo;
    private final BoundingBoxFormat bbFormat;

    public VAJsonModule() {
        this(null, null);
    }

    public VAJsonModule(BoundingBoxFormat bbFormat) {
        this(bbFormat, null);
    }

    public VAJsonModule(VideoInfo2 videoInfo) {
        this(null, videoInfo);
    }

    public VAJsonModule(BoundingBoxFormat bbFormat, VideoInfo2 videoInfo) {
        this.bbFormat = bbFormat;
        this.videoInfo = videoInfo;
    }

    private final StdConverter<UIAnnotation<?>, Annotation> uiAnnotationConverter = new StdConverter<>() {
        @Override
        public Annotation convert(UIAnnotation<?> value) {
            return value.getAnnotationModel();
        }
    };

    @Override
    public void setupModule(SetupContext context) {
        serializers.addSerializer(UIAnnotation.class, new StdDelegatingSerializer(uiAnnotationConverter));
        serializers.addSerializer(Annotation.class, new AnnotationSerializer(Annotation.class));
        serializers.addSerializer(PointVideo.class, new PointSerializer<>(PointVideo.class, videoInfo));
        serializers.addSerializer(BoundingBoxVideo.class, new BoundingBoxSerializer<>(BoundingBoxVideo.class, bbFormat, videoInfo));
        context.addSerializers(serializers);
    }
}
