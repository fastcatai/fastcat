package serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import modelV3.BoundingBox;
import modelV3.Point;
import modelV3.PointVideo;
import util.Utils;
import videoplayer.VideoInfo2;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PointSerializer<P extends Point> extends StdSerializer<P> {

    private final VideoInfo2 videoInfo;

    public PointSerializer(Class<P> t) {
        this(t, null);
    }

    public PointSerializer(Class<P> t, VideoInfo2 videoInfo) {
        super(t);
        this.videoInfo = videoInfo;
    }

    @Override
    public void serialize(P value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("type", "point");
        gen.writeStringField("label", value.getLabel());
        gen.writeStringField("instance", value.getInstance());

        if (value instanceof PointVideo) {
            PointVideo pv = (PointVideo) value;
            double startMs = pv.getTimestampStartMillis();
            double endMs = pv.getTimestampEndMillis();
            gen.writeNumberField("timestampStart", startMs);
            gen.writeNumberField("timestampEnd", endMs);

            if (videoInfo != null && videoInfo.getFPS().isPresent() && startMs >= endMs) {
                List<Integer> framesNumbers = Utils.generateFramesNumbers(startMs, endMs, videoInfo.getFPS().get());
                provider.defaultSerializeField("frames", framesNumbers, gen);
            }

            BoundingBox inferredBox = pv.getInferredBox();
            gen.writeObjectFieldStart("inferredBox");
            gen.writeNumberField("x", (int) inferredBox.getX());
            gen.writeNumberField("y", (int) inferredBox.getY());
            gen.writeNumberField("width", (int) inferredBox.getWidth());
            gen.writeNumberField("height", (int) inferredBox.getHeight());
            gen.writeEndObject();
        }

        gen.writeEndObject();
    }
}
