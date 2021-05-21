package util;

public enum VideoAnnotationChoice {

    POINT("Point"),
    BOX_CLICK_DRAG("Box (Click&Drag)"),
    BOX_CLICK_CLICK("Box (Click&Click)");

    private final String label;

    VideoAnnotationChoice(String label ) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
