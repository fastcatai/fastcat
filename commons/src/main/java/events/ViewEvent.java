package events;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.stage.Window;

public class ViewEvent extends Event {

    public static final EventType<ViewEvent> MENU = new EventType<>(Event.ANY, "MENU");
    public static final EventType<ViewEvent> IMAGE_ANNOTATION = new EventType<>(Event.ANY, "IMAGE_ANNOTATION");
    public static final EventType<ViewEvent> VIDEO_REVIEW = new EventType<>(Event.ANY, "VIDEO_REVIEW");
    public static final EventType<ViewEvent> VIDEO_ANNOTATION = new EventType<>(Event.ANY, "VIDEO_ANNOTATION");

    public ViewEvent(final Window source, final EventType<? extends Event> eventType) {
        super(source, source, eventType);
    }
}
