package annotation;

public interface Annotation {
    UIAnnotation<?> makeAnnotation();

    default UIAnnotation<?> create() {
        return makeAnnotation();
    }
}
