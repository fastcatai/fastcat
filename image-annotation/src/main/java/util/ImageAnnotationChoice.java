package util;

public enum ImageAnnotationChoice {

    BOX_CLICK_DRAG("Box (Click&Drag)"),
    BOX_CLICK_CLICK("Box (Click&Click)");

    private final String label;

    ImageAnnotationChoice(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
