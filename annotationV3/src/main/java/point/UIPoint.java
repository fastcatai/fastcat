package point;

import annotation.UIAnnotation;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import modelV3.Point;

public abstract class UIPoint<P extends Point> extends UIAnnotation<P> {

    private final Pane pane;
    private final Circle circle;
    private final int originalImageWidth;
    private final int originalImageHeight;

    public UIPoint(Pane pane, P point, int originalImageWidth, int originalImageHeight) {
        super(point, originalImageWidth, originalImageHeight);
        this.pane = pane;
        this.originalImageWidth = originalImageWidth;
        this.originalImageHeight = originalImageHeight;
        circle = new Circle(point.getX(), point.getY(), point.getRadius());
    }

    public double getX() {
        return circle.getCenterX();
    }

    public double getY() {
        return circle.getCenterY();
    }

    public double getRadius() {
        return circle.getRadius();
    }

    public int getOriginalImageWidth() {
        return originalImageWidth;
    }

    public int getOriginalImageHeight() {
        return originalImageHeight;
    }

    public Pane getPane() {
        return pane;
    }

    @Override
    public void addToPane() {
        pane.getChildren().add(circle);
    }

    @Override
    public void removeFromPane() {
        pane.getChildren().remove(circle);
    }
}
