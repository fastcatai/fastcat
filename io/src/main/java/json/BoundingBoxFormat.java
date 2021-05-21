package json;

public enum BoundingBoxFormat {
    COCO("coco"),
    VOC("voc");

    private final String name;

    BoundingBoxFormat(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
