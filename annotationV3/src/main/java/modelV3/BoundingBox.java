package modelV3;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class BoundingBox extends Annotation {

    private final DoubleProperty x;
    private final DoubleProperty y;
    private final DoubleProperty width;
    private final DoubleProperty height;

    public BoundingBox(double x, double y, double width, double height) {
        this(x, y, width, height, "");
    }

    public BoundingBox(double x, double y, double width, double height, String label) {
        this(x, y, width, height, label, "");
    }

    public BoundingBox(double x, double y, double width, double height, String label, boolean verified, boolean autoCreated) {
        this(x, y, width, height, label, "", verified, autoCreated);
    }

    public BoundingBox(double x, double y, double width, double height, String label, String instance) {
        this(x, y, width, height, label, instance, false, false);
    }

    public BoundingBox(double x, double y, double width, double height, String label, String instance, boolean verified, boolean autoCreated) {
        super(label, instance, verified, autoCreated);
        this.x = new SimpleDoubleProperty(x);
        this.y = new SimpleDoubleProperty(y);
        this.width = new SimpleDoubleProperty(width);
        this.height = new SimpleDoubleProperty(height);
    }

    public double getX() {
        return x.get();
    }

    public DoubleProperty xProperty() {
        return x;
    }

    public void setX(double x) {
        this.x.set(x);
    }

    public double getY() {
        return y.get();
    }

    public DoubleProperty yProperty() {
        return y;
    }

    public void setY(double y) {
        this.y.set(y);
    }

    public double getWidth() {
        return width.get();
    }

    public DoubleProperty widthProperty() {
        return width;
    }

    public void setWidth(double width) {
        this.width.set(width);
    }

    public double getHeight() {
        return height.get();
    }

    public DoubleProperty heightProperty() {
        return height;
    }

    public void setHeight(double height) {
        this.height.set(height);
    }
}
