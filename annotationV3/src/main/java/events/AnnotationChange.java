package events;

import annotation.UIAnnotation;

public interface AnnotationChange {
    /**
     * Will be called when the annotation was moved or changed its size.
     *
     * @param uiAnnotation annotation that was changed.
     */
    void changed(UIAnnotation<?> uiAnnotation);
}
