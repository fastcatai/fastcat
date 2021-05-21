package deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import wrapper.IAMetadata;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class IAMetadataDeserializer extends StdDeserializer<IAMetadata> {

    public IAMetadataDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public IAMetadata deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode root = ctxt.readTree(p);

        IAMetadata metadata = new IAMetadata();

        metadata.setVersion(root.get("version").intValue());
        metadata.setType(root.get("type").textValue());
        metadata.setImageFolder(root.get("imageFolder").textValue());

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
