package serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import json.BoundingBoxFormat;
import modelV3.BoundingBox;
import modelV3.BoundingBoxVideo;
import util.Utils;
import videoplayer.VideoInfo2;

import java.io.IOException;
import java.util.List;

public class BoundingBoxSerializer<B extends BoundingBox> extends StdSerializer<B> {

    private final BoundingBoxFormat format;
    private final VideoInfo2 videoInfo;

    public BoundingBoxSerializer(Class<B> t) {
        this(t, null);
    }

    public BoundingBoxSerializer(Class<B> t, BoundingBoxFormat format) {
        this(t, format, null);
    }

    public BoundingBoxSerializer(Class<B> t, BoundingBoxFormat format, VideoInfo2 videoInfo) {
        super(t);
        this.format = format == null ? BoundingBoxFormat.COCO : format;
        this.videoInfo = videoInfo;

    }

    @Override
    public void serialize(B value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        gen.writeStringField("type", "boundingBox");
        gen.writeStringField("label", value.getLabel().strip());
        gen.writeStringField("instance", value.getInstance().strip());

        gen.writeArrayFieldStart("additionalLabels");
        for (String l : value.getAdditionalLabels())
            gen.writeString(l);
        gen.writeEndArray();

        gen.writeBooleanField("verified", value.isVerified());
        gen.writeBooleanField("autoCreated", value.isAutoCreated());

        if (format == BoundingBoxFormat.COCO) genBoxCOCO(value, gen);
        if (format == BoundingBoxFormat.VOC) genBoxVOC(value, gen);

        if (value instanceof BoundingBoxVideo) {
            BoundingBoxVideo bbv = (BoundingBoxVideo) value;
            double startMs = bbv.getTimestampStartMillis();
            double endMs = bbv.getTimestampEndMillis();
            gen.writeNumberField("timestampStart", startMs);
            gen.writeNumberField("timestampEnd", endMs);

            if (videoInfo != null && videoInfo.getFPS().isPresent() && startMs >= endMs) {
                List<Integer> framesNumbers = Utils.generateFramesNumbers(startMs, endMs, videoInfo.getFPS().get());
                provider.defaultSerializeField("frames", framesNumbers, gen);
            }
        }

        gen.writeEndObject();
    }

    private void genBoxCOCO(B value, JsonGenerator gen) throws IOException {
        gen.writeStringField("format", BoundingBoxFormat.COCO.toString());
        gen.writeNumberField("x", (int) value.getX());
        gen.writeNumberField("y", (int) value.getY());
        gen.writeNumberField("width", (int) value.getWidth());
        gen.writeNumberField("height", (int) value.getHeight());
    }

    private void genBoxVOC(B value, JsonGenerator gen) throws IOException {
        gen.writeStringField("format", BoundingBoxFormat.VOC.toString());
        gen.writeNumberField("x", (int) value.getX());
        gen.writeNumberField("y", (int) value.getY());
        gen.writeNumberField("width", (int) value.getX() + value.getWidth());
        gen.writeNumberField("height", (int) value.getY() + value.getHeight());
    }
}
