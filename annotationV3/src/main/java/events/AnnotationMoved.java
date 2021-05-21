package events;

import annotation.UIAnnotation;

public interface AnnotationMoved {
    /**
     * Will be called when the annotation was moved in x or y direction.
     *
     * @param uiAnnotation annotation that was moved.
     */
    void moved(UIAnnotation<?> uiAnnotation);
}
