package wrapper;

import annotation.UIAnnotation;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import modelV3.Annotation;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class is used to create a JSON. It should be used to serialize the data.
 */
@JsonPropertyOrder({"annotations", "metadata"})
public class VAJsonWrapper {
    private final List<UIAnnotation<?>> annotations;
    private final Metadata metadata;

    public VAJsonWrapper(List<UIAnnotation<?>> annotations) {
        this.annotations = annotations;

        List<Annotation> annotationModels = annotations.stream()
                .map(UIAnnotation::getAnnotationModel)
                .collect(Collectors.toList());
        metadata = new Metadata(annotationModels);
    }

    public List<UIAnnotation<?>> getAnnotations() {
        return annotations;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    @JsonPropertyOrder({"version", "type", "annotationCount", "labels", "instances", "additionalLabels"})
    private static class Metadata {
        private final List<Annotation> annotations;

        private Metadata(List<Annotation> annotations) {
            this.annotations = annotations;
        }

        public int getVersion() {
            return 1;
        }

        public String getType() {
            return "VideoAnnotation";
        }

        public int getAnnotationCount() {
            return annotations.size();
        }

        public Set<String> getLabels() {
            return annotations.stream().map(Annotation::getLabel)
                    .filter(Predicate.not(String::isBlank))
                    .collect(Collectors.toSet());
        }

        public Set<String> getInstances() {
            return annotations.stream().map(Annotation::getInstance)
                    .filter(Predicate.not(String::isBlank))
                    .collect(Collectors.toSet());
        }

        public Set<String> getAdditionalLabels() {
            return annotations.stream().map(Annotation::getAdditionalLabels)
                    .flatMap(Collection::stream)
                    .filter(Predicate.not(String::isBlank))
                    .collect(Collectors.toSet());
        }
    }

}
