package boundingbox;

import annotation.UIAttributesSettings;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import settings.SettingsProperties;

public class UIBoxAnchor extends Circle implements EventHandler<MouseEvent> {

    private final Pane pane;
    private final Rectangle box;
    private final Position position;

    private double offsetX;
    private double offsetY;

    public UIBoxAnchor(Pane pane, Rectangle box, Position position) {
        this.pane = pane;
        this.box = box;
        this.position = position;

        // bind anchor to rectangle edges
        bindToPosition();

        // get all anchor UI attributes
        ReadOnlyObjectProperty<Paint> fillColor = UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_ANCHOR_FILL_COLOR_DEFAULT);
        ReadOnlyObjectProperty<Paint> fillColorHover = UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_ANCHOR_FILL_COLOR_HOVER);
        ReadOnlyObjectProperty<Paint> strokeColor = UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_ANCHOR_STROKE_COLOR_DEFAULT);
        ReadOnlyObjectProperty<Paint> strokeColorHover = UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_ANCHOR_STROKE_COLOR_HOVER);
        ReadOnlyIntegerProperty size = UIAttributesSettings.getIntegerProperty(SettingsProperties.ANNOTATION_ANCHOR_SIZE_DEFAULT);
        ReadOnlyIntegerProperty sizeHover = UIAttributesSettings.getIntegerProperty(SettingsProperties.ANNOTATION_ANCHOR_SIZE_HOVER);

        // bind UI attributes
        fillProperty().bind(Bindings.when(hoverProperty()).then(fillColorHover).otherwise(fillColor));
        strokeProperty().bind(Bindings.when(hoverProperty()).then(strokeColorHover).otherwise(strokeColor));
        radiusProperty().bind(Bindings.when(hoverProperty()).then(sizeHover).otherwise(size));

        // add anchor to pane
        pane.getChildren().add(0, this);

        // add mouse handlers
        setOnMouseEntered(this);
        setOnMouseExited(this);
        setOnMousePressed(this);
        setOnMouseDragged(this);
    }

    private void bindToPosition() {
        if (position == Position.TOP_LEFT) {
            centerXProperty().bind(box.xProperty());
            centerYProperty().bind(box.yProperty());
        } else if (position == Position.TOP_RIGHT) {
            centerXProperty().bind(box.xProperty().add(box.widthProperty()));
            centerYProperty().bind(box.yProperty());
        } else if (position == Position.BOTTOM_LEFT) {
            centerXProperty().bind(box.xProperty());
            centerYProperty().bind(box.yProperty().add(box.heightProperty()));
        } else if (position == Position.BOTTOM_RIGHT) {
            centerXProperty().bind(box.xProperty().add(box.widthProperty()));
            centerYProperty().bind(box.yProperty().add(box.heightProperty()));
        } else if (position == Position.TOP) {
            centerXProperty().bind(box.xProperty().add(box.widthProperty().divide(2)));
            centerYProperty().bind(box.yProperty());
        } else if (position == Position.RIGHT) {
            centerXProperty().bind(box.xProperty().add(box.widthProperty()));
            centerYProperty().bind(box.yProperty().add(box.heightProperty().divide(2)));
        } else if (position == Position.BOTTOM) {
            centerXProperty().bind(box.xProperty().add(box.widthProperty().divide(2)));
            centerYProperty().bind(box.yProperty().add(box.heightProperty()));
        } else if (position == Position.LEFT) {
            centerXProperty().bind(box.xProperty());
            centerYProperty().bind(box.yProperty().add(box.heightProperty().divide(2)));
        }

        translateXProperty().bind(box.translateXProperty());
        translateYProperty().bind(box.translateYProperty());
    }

    public void setEditable(boolean isEditable) {
        setDisable(!isEditable);
        setVisible(isEditable);
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public void handle(MouseEvent event) {
        event.consume();

        if (event.getEventType() == MouseEvent.MOUSE_ENTERED) {
            if (!event.isPrimaryButtonDown())
                setCursor(position.getCursor());

        } else if (event.getEventType() == MouseEvent.MOUSE_EXITED) {
            if (!event.isPrimaryButtonDown())
                setCursor(Cursor.DEFAULT);

        } else if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            if (event.isPrimaryButtonDown()) {
                offsetX = getCenterX() - event.getX();
                offsetY = getCenterY() - event.getY();
            }

        } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            if (!event.isPrimaryButtonDown())
                return;

            double xLeft, xRight, yTop, yBottom;

            // x position
            if (position == Position.TOP_LEFT || position == Position.BOTTOM_LEFT || position == Position.LEFT) {
                xLeft = event.getX() - offsetX;
                xRight = box.getX() + box.getWidth();
                if (xLeft < 0)
                    xLeft = 0;
                else if (xLeft > xRight - UIBoundingBox.MIN_WIDTH)
                    xLeft = xRight - UIBoundingBox.MIN_WIDTH;
            } else if (position == Position.TOP_RIGHT || position == Position.BOTTOM_RIGHT || position == Position.RIGHT) {
                xLeft = box.getX();
                xRight = event.getX() - offsetX;
                if (xRight > pane.getWidth())
                    xRight = pane.getWidth();
                else if (xRight < xLeft + UIBoundingBox.MIN_WIDTH)
                    xRight = xLeft + UIBoundingBox.MIN_WIDTH;
            } else { // Position.TOP and Position.BOTTOM
                xLeft = box.getX();
                xRight = box.getX() + box.getWidth();
            }

            // y position
            if (position == Position.TOP_LEFT || position == Position.TOP_RIGHT || position == Position.TOP) {
                yTop = event.getY() - offsetY;
                yBottom = box.getY() + box.getHeight();
                if (yTop < 0)
                    yTop = 0;
                else if (yTop > yBottom - UIBoundingBox.MIN_HEIGHT)
                    yTop = yBottom - UIBoundingBox.MIN_HEIGHT;
            } else if (position == Position.BOTTOM_LEFT || position == Position.BOTTOM_RIGHT || position == Position.BOTTOM) {
                yTop = box.getY();
                yBottom = event.getY() - offsetY;
                if (yBottom > pane.getHeight())
                    yBottom = pane.getHeight();
                else if (yBottom < yTop + UIBoundingBox.MIN_HEIGHT)
                    yBottom = yTop + UIBoundingBox.MIN_HEIGHT;
            } else { // Position.LEFT and Position.RIGHT
                yTop = box.getY();
                yBottom = box.getY() + box.getHeight();
            }

            box.setX(xLeft);
            box.setY(yTop);
            box.setWidth(xRight - xLeft);
            box.setHeight(yBottom - yTop);

        }
    }

    public enum Position {
        TOP_LEFT(Cursor.NW_RESIZE),
        TOP(Cursor.N_RESIZE),
        TOP_RIGHT(Cursor.NE_RESIZE),
        RIGHT(Cursor.E_RESIZE),
        BOTTOM_RIGHT(Cursor.SE_RESIZE),
        BOTTOM(Cursor.S_RESIZE),
        BOTTOM_LEFT(Cursor.SW_RESIZE),
        LEFT(Cursor.W_RESIZE);

        private final Cursor cursor;

        Position(Cursor cursor) {
            this.cursor = cursor;
        }

        public Cursor getCursor() {
            return cursor;
        }
    }
}
