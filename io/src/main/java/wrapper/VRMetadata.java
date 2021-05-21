package wrapper;


import annotation.UIAnnotation;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import model.ImageData;
import model.LoadedReviewData;
import model.SuperframeData;
import modelV3.Annotation;
import videoplayer.VideoInfo2;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@JsonPropertyOrder({"version", "type", "videoFile", "resolutionWidth", "resolutionHeight", "frameWidth", "frameHeight",
        "superframes", "classes", "imageFolder", "annotatedImages", "annotationCount",
        "imagePrimaryLabels", "imageInstances", "imageSecondaryLabels",
        "annotationPrimaryLabels", "annotationInstances", "annotationSecondaryLabels"})
public class VRMetadata {
    private List<SuperframeData> superframes;
    private List<ImageData> imageDataList;
    private LoadedReviewData loadedData;
    private List<Annotation> annotations;
    private List<Annotation> imageClassifications;

    /**
     * Deserialization constructor
     */
    public VRMetadata() {
        // none
    }

    /**
     * Serialization constructor
     */
    public VRMetadata(List<SuperframeData> superframes, List<ImageData> imageDataList, LoadedReviewData loadedData) {
        this.superframes = superframes;
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
        return 2;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    private String type;

    public String getType() {
        if (type != null)
            return type;
        return "VideoReview";
    }

    public void setType(String type) {
        this.type = type;
    }

    private String videoFile;

    public String getVideoFile() {
        if (videoFile != null)
            return videoFile;
        if (loadedData == null)
            return "";
        return loadedData.getVideoFile().getFileName().toString();
    }

    public void setVideoFile(String videoFile) {
        this.videoFile = videoFile;
    }

    private Integer resolutionWidth;

    public int getResolutionWidth() {
        if (resolutionWidth != null)
            return resolutionWidth;
        if (loadedData == null)
            return -1;
        return loadedData.getVideoInfo().getResolutionWidth().orElse(-1);
    }

    public void setResolutionWidth(int resolutionWidth) {
        this.resolutionWidth = resolutionWidth;
    }

    private Integer resolutionHeight;

    public int getResolutionHeight() {
        if (resolutionHeight != null)
            return resolutionHeight;
        if (loadedData == null)
            return -1;
        return loadedData.getVideoInfo().getResolutionHeight().orElse(-1);
    }

    public void setResolutionHeight(int resolutionHeight) {
        this.resolutionHeight = resolutionHeight;
    }

    private Integer frameWidth;

    public int getFrameWidth() {
        if (frameWidth != null)
            return frameWidth;
        if (loadedData == null)
            return -1;
        return loadedData.getFrameWidth();
    }

    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
    }

    private Integer frameHeight;

    public int getFrameHeight() {
        if (frameHeight != null)
            return frameHeight;
        if (loadedData == null)
            return -1;
        return loadedData.getFrameHeight();
    }

    public void setFrameHeight(int frameHeight) {
        this.frameHeight = frameHeight;
    }

    private String imageFolder;

    public String getImageFolder() {
        if (imageFolder != null)
            return imageFolder;
        if (loadedData == null)
            return "";
        return loadedData.getImageFolder().getFileName().toString();
    }

    public void setImageFolder(String imageFolder) {
        this.imageFolder = imageFolder;
    }

    private Set<Integer> superframeNumbers;

    public Set<Integer> getSuperframes() {
        if (superframeNumbers != null)
            return superframeNumbers;
        if (superframes == null)
            return Set.of(-1);
        return superframes.stream()
                .mapToInt(SuperframeData::getFrameNumber)
                .boxed()
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void setSuperframeNumbers(Set<Integer> superframeNumbers) {
        this.superframeNumbers = superframeNumbers;
    }

    private Set<String> classes;

    public Set<String> getClasses() {
        if (classes != null)
            return classes;
        if (superframes == null)
            return Set.of();
        return superframes.stream()
                .flatMap(superframeData -> superframeData.getChildFrames().stream())
                .flatMap(frameData -> frameData.getClasses().stream())
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void setClasses(Set<String> classes) {
        this.classes = classes;
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

    public void setAnnotationCount(int annotationCount) {
        this.annotationCount = annotationCount;
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