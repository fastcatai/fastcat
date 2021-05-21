package modelV3;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import ui.UIProperties;

public class Point extends Annotation {

    private final DoubleProperty x;
    private final DoubleProperty y;
    private final DoubleProperty radius;

    public Point(double x, double y, double radius, String label) {
        this(x, y, radius, label, false, false);
    }

    public Point(double x, double y, double radius, String label, boolean verified, boolean autoCreated) {
        this(x, y, radius, label, "", verified, autoCreated);
    }

    public Point(double x, double y, double radius, String label, String instance) {
        this(x, y, radius, label, instance, false, false);
    }

    public Point(double x, double y, double radius, String label, String instance, boolean verified, boolean autoCreated) {
        super(label, instance, verified, autoCreated);
        this.x = new SimpleDoubleProperty(x);
        this.y = new SimpleDoubleProperty(y);
        this.radius = new SimpleDoubleProperty(radius <= 0 ? UIProperties.POINT_RADIUS : radius);
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

    public double getRadius() {
        return radius.get();
    }

    public DoubleProperty radiusProperty() {
        return radius;
    }

    public void setRadius(double r) {
        radius.set(r);
    }
}
