package deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import wrapper.VRMetadata;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class VRMetadataDeserializer extends StdDeserializer<VRMetadata> {

    public VRMetadataDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public VRMetadata deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode root = ctxt.readTree(p);

        VRMetadata metadata = new VRMetadata();

        metadata.setVersion(root.get("version").intValue());
        metadata.setType(root.get("type").textValue());
        metadata.setVideoFile(root.get("videoFile").textValue());
        metadata.setResolutionWidth(root.get("resolutionWidth").intValue());
        metadata.setResolutionHeight(root.get("resolutionHeight").intValue());
        metadata.setFrameWidth(root.get("frameWidth").intValue());
        metadata.setFrameHeight(root.get("frameHeight").intValue());
        metadata.setImageFolder(root.get("imageFolder").textValue());

        Set<Integer> superframeNumbers = new LinkedHashSet<>();
        for (JsonNode s : root.withArray("superframes"))
            superframeNumbers.add(s.intValue());
        metadata.setSuperframeNumbers(superframeNumbers);

        Set<String> classes = new LinkedHashSet<>();
        for (JsonNode c : root.withArray("classes"))
            classes.add(c.textValue());
        metadata.setClasses(classes);

        String annotatedImagesKey = root.has("annotatedImages") ? "annotatedImages" : "imageCountWithAnnotations";
        metadata.setAnnotatedImages(root.get(annotatedImagesKey).intValue());

        metadata.setAnnotationCount(root.get("annotationCount").intValue());

        Set<String> ipl = new LinkedHashSet<>();
        for (JsonNode l : root.withArray("imagePrimaryLabels"))
            ipl.add(l.textValue());
        metadata.setImagePrimaryLabels(ipl);

        Set<String> ii = new LinkedHashSet<>();
        for (JsonNode l : root.withArray("imageInstances"))
            ii.add(l.textValue());
        metadata.setImageInstances(ii);

        Set<String> isl = new LinkedHashSet<>();
        for (JsonNode l : root.withArray("imageSecondaryLabels"))
            isl.add(l.textValue());
        metadata.setImageSecondaryLabels(isl);

        Set<String> apl = new LinkedHashSet<>();
        for (JsonNode l : root.withArray("annotationPrimaryLabels"))
            apl.add(l.textValue());
        metadata.setAnnotationPrimaryLabels(apl);

        Set<String> ai = new LinkedHashSet<>();
        for (JsonNode l : root.withArray("annotationInstances"))
            ai.add(l.textValue());
        metadata.setAnnotationInstances(ai);

        Set<String> asl = new LinkedHashSet<>();
        for (JsonNode l : root.withArray("annotationSecondaryLabels"))
            asl.add(l.textValue());
        metadata.setAnnotationSecondaryLabels(asl);

        return metadata;
    }
}
