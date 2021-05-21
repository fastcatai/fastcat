package videoreview;

public enum ReviewAnnotationChoice {

    BOX_CLICK_DRAG("Box (Click&Drag)");

    private final String label;

    ReviewAnnotationChoice(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
