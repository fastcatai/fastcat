package events;

import annotation.UIAnnotation;

public interface CreationCanceled {
    void canceled(UIAnnotation<?> uiAnnotation);
}
