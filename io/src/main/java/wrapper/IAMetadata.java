package wrapper;


import annotation.UIAnnotation;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import model.ImageData;
import model.LoadedAnnotationData;
import modelV3.Annotation;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@JsonPropertyOrder({"version", "type", "imageFolder", "annotatedImages", "annotationCount",
        "imagePrimaryLabels", "imageInstances", "imageSecondaryLabels",
        "annotationPrimaryLabels", "annotationInstances", "annotationSecondaryLabels"})
public class IAMetadata {
    private LoadedAnnotationData loadedData;
    private List<ImageData> imageDataList;
    private List<Annotation> annotations;
    private List<Annotation> imageClassifications;

    /**
     * Deserialization
     */
    public IAMetadata() {
        // none
    }

    /**
     * Serialization constructor
     */
    public IAMetadata(List<ImageData> imageDataList, LoadedAnnotationData loadedData) {
        this.imageDataList = imageDataList;
        this.loadedData = loadedData;

        annotations = imageDataList.stream()
                .map(ImageData::getAnnotations)
                .flatMap(Collection::stream)
                .map(UIAnnotation::getAnnotationModel)
                .collect(Collectors.toList());

        imageClassifications = imageDataList.stream()
                .map(ImageData::getImageClassification)
                .collect(Collectors.toList());
    }

    private Integer version;

    public int getVersion() {
        if (version != null)
            return version;
        return 1;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    private String type;

    public String getType() {
        if (type != null)
            return type;
        return "ImageAnnotation";
    }

    public void setType(String type) {
        this.type = type;
    }

    private String imageFolder;

    public String getImageFolder() {
        if (imageFolder != null)
            return imageFolder;
        if (loadedData == null)
            return "";
        return loadedData.getImageFolder().getFileName().toString();
    }

    public void setImageFolder(String folder) {
        imageFolder = folder;
    }

    private Long annotatedImages;

    public long getAnnotatedImages() {
        if (annotatedImages != null)
            return annotatedImages;
        if (imageDataList == null)
            return -1;
        return imageDataList.stream()
                .map(ImageData::getAnnotations)
                .filter(Predicate.not(List::isEmpty))
                .count();
    }

    public void setAnnotatedImages(long count) {
        annotatedImages = count;
    }

    private Integer annotationCount;

    public int getAnnotationCount() {
        if (annotationCount != null)
            return annotationCount;
        if (annotations == null)
            return -1;
        return annotations.size();
    }

    public void setAnnotationCount(int count) {
        annotationCount = count;
    }

    private Set<String> imagePrimaryLabels;

    public Set<String> getImagePrimaryLabels() {
        if (imagePrimaryLabels != null)
            return imagePrimaryLabels;
        if (imageClassifications == null)
            return Set.of();
        return imageClassifications.stream()
                .map(Annotation::getLabel)
                .filter(Predicate.not(String::isBlank))
                .collect(Collectors.toSet());
    }

    public void setImagePrimaryLabels(Set<String> imagePrimaryLabels) {
        this.imagePrimaryLabels = imagePrimaryLabels;
    }

    private Set<String> imageInstances;

    public Set<String> getImageInstances() {
        if (imageInstances != null)
            return imageInstances;
        if (imageClassifications == null)
            return Set.of();
        return imageClassifications.stream()
                .map(Annotation::getInstance)
                .filter(Predicate.not(String::isBlank))
                .collect(Collectors.toSet());
    }

    public void setImageInstances(Set<String> imageInstances) {
        this.imageInstances = imageInstances;
    }

    private Set<String> imageSecondaryLabels;

    public Set<String> getImageSecondaryLabels() {
        if (imageSecondaryLabels != null)
            return imageSecondaryLabels;
        if (imageClassifications == null)
            return Set.of();
        return imageClassifications.stream()
                .map(Annotation::getAdditionalLabels)
                .flatMap(Collection::stream)
                .filter(Predicate.not(String::isBlank))
                .collect(Collectors.toSet());
    }

    public void setImageSecondaryLabels(Set<String> imageSecondaryLabels) {
        this.imageSecondaryLabels = imageSecondaryLabels;
    }

    private Set<String> annotationPrimaryLabels;

    public Set<String> getAnnotationPrimaryLabels() {
        if (annotationPrimaryLabels != null)
            return annotationPrimaryLabels;
        if (annotations == null)
            return Set.of();
        return annotations.stream()
                .map(Annotation::getLabel)
                .filter(Predicate.not(String::isBlank))
                .collect(Collectors.toSet());
    }

    public void setAnnotationPrimaryLabels(Set<String> annotationPrimaryLabels) {
        this.annotationPrimaryLabels = annotationPrimaryLabels;
    }

    private Set<String> annotationInstances;

    public Set<String> getAnnotationInstances() {
        if (annotationInstances != null)
            return annotationInstances;
        if (annotations == null)
            return Set.of();
        return annotations.stream()
                .map(Annotation::getInstance)
                .filter(Predicate.not(String::isBlank))
                .collect(Collectors.toSet());
    }

    public void setAnnotationInstances(Set<String> annotationInstances) {
        this.annotationInstances = annotationInstances;
    }

    private Set<String> annotationSecondaryLabels;

    public Set<String> getAnnotationSecondaryLabels() {
        if (annotationSecondaryLabels != null)
            return annotationSecondaryLabels;
        if (annotations == null)
            return Set.of();
        return annotations.stream()
                .map(Annotation::getAdditionalLabels)
                .flatMap(Collection::stream)
                .filter(Predicate.not(String::isBlank))
                .collect(Collectors.toSet());
    }

    public void setAnnotationSecondaryLabels(Set<String> annotationSecondaryLabels) {
        this.annotationSecondaryLabels = annotationSecondaryLabels;
    }
}