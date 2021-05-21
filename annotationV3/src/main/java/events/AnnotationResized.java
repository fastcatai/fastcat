package events;

import annotation.UIAnnotation;

public interface AnnotationResized {
    /**
     * Will be called when the annotation was resized in width or height.
     *
     * @param uiAnnotation annotation that was resized.
     */
    void resized(UIAnnotation<?> uiAnnotation);
}
