package boundingbox;

import annotation.UIAnnotation;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import modelV3.BoundingBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public abstract class UIBoundingBox<B extends BoundingBox> extends UIAnnotation<B> {
    private static final Logger logger = LogManager.getLogger(UIBoundingBox.class);

    public static final double MIN_WIDTH = 15;
    public static final double MIN_HEIGHT = 15;

    public enum CreationType {
        CLICK_DRAG, CLICK_CLICK
    }

    private final Pane pane;
    private final Rectangle box;
    private final List<UIBoxAnchor> anchorPoints = new ArrayList<>(8); // list for all anchor points

    /**
     * Create UI bounding box object.
     *
     * @param pane                pane on which it will be drawn
     * @param boundingBox         bounding box with relative coordinates
     * @param originalImageWidth  original image width
     * @param originalImageHeight original image height
     */
    public UIBoundingBox(Pane pane, B boundingBox, final int originalImageWidth, final int originalImageHeight) {
        this(pane, boundingBox, false, originalImageWidth, originalImageHeight);
    }

    /**
     * Create UI bounding box.
     *
     * @param pane                pane on which it will be drawn
     * @param boundingBox         bounding box with relative coordinates
     * @param absolute            if <code>true</code> then <code>boundingBox</code> contains absolute coordinates,
     *                            otherwise relative
     * @param originalImageWidth  original image width
     * @param originalImageHeight original image height
     */
    public UIBoundingBox(Pane pane, B boundingBox, boolean absolute, final int originalImageWidth, final int originalImageHeight) {
        super(boundingBox, originalImageWidth, originalImageHeight);
        this.pane = pane;

        // convert to relative coordinate values
        if (absolute) {
            boundingBox.setX((boundingBox.getX() / originalImageWidth) * pane.getWidth()); // relative bounding box x
            boundingBox.setY((boundingBox.getY() / originalImageHeight) * pane.getHeight()); // relative bounding box y
            boundingBox.setWidth((boundingBox.getWidth() / originalImageWidth) * pane.getWidth()); // relative bounding box width
            boundingBox.setHeight((boundingBox.getHeight() / originalImageHeight) * pane.getHeight()); // relative bounding box height
        }

        // create rectangle shape
        box = new Rectangle(boundingBox.getX(), boundingBox.getY(), boundingBox.getWidth(), boundingBox.getHeight());
        box.visibleProperty().bind(visibleProperty());
        annotationHoverProperty().bind(box.hoverProperty());
        // bind UI attributes to rectangle
        box.setStrokeType(getStrokeType());
        box.strokeProperty().bind(Bindings.when(hoverProperty()).then(strokeColorHoverProperty()).otherwise(strokeColorProperty()));
        box.strokeWidthProperty().bind(Bindings.when(hoverProperty()).then(strokeWidthHoverProperty()).otherwise(strokeWidthProperty()));
        box.fillProperty().bind(Bindings.when(hoverProperty()).then(fillHoverProperty()).otherwise(fillProperty()));
        box.hoverProperty().addListener((observable, oldValue, newValue) -> { // TODO: bind cursor together with mouse events
            if (newValue) box.setCursor(Cursor.OPEN_HAND);
            else box.setCursor(Cursor.DEFAULT);
        });

        // calculate absolute box dimensions and bind it to BoundingBox [ abs = (rel_box / rel_image) * abs_image ]
        getAnnotationModel().xProperty().bind(Bindings.createDoubleBinding(() -> (box.getX() / pane.getWidth()) * originalImageWidth, box.xProperty()));
        getAnnotationModel().yProperty().bind(Bindings.createDoubleBinding(() -> (box.getY() / pane.getHeight()) * originalImageHeight, box.yProperty()));
        getAnnotationModel().widthProperty().bind(Bindings.createDoubleBinding(() -> (box.getWidth() / pane.getWidth()) * originalImageWidth, box.widthProperty()));
        getAnnotationModel().heightProperty().bind(Bindings.createDoubleBinding(() -> (box.getHeight() / pane.getHeight()) * originalImageHeight, box.heightProperty()));

        // resize rectangle on pane resize
        pane.widthProperty().addListener((observable, oldWidth, newWidth) -> {
            box.setX((box.getX() / oldWidth.doubleValue()) * newWidth.doubleValue());
            box.setWidth((box.getWidth() / oldWidth.doubleValue()) * newWidth.doubleValue());
        });
        pane.heightProperty().addListener((observable, oldHeight, newHeight) -> {
            box.setY((box.getY() / oldHeight.doubleValue()) * newHeight.doubleValue());
            box.setHeight((box.getHeight() / oldHeight.doubleValue()) * newHeight.doubleValue());
        });

        // glue label to bounding box
        annotationLabel.translateXProperty().bind(box.translateXProperty());
        annotationLabel.translateYProperty().bind(box.translateYProperty());
        annotationLabel.layoutXProperty().bind(box.xProperty());
        annotationLabel.layoutYProperty().bind(box.yProperty().subtract(annotationLabel.heightProperty()));
    }

    /**
     * Bind all annotation events after creation.
     */
    private void bindAnnotationEvents() {
        editableProperty().addListener((observable, oldIsEditable, newIsEditable) -> {
            for (UIBoxAnchor anchor : anchorPoints)
                anchor.setEditable(newIsEditable);
        });

        selectedProperty().addListener((observable, oldIsSelected, newIsSelected) -> {
            if (!newIsSelected && isEditable())
                setEditable(false);
        });

        InvalidationListener changedListener = observable -> {
//        ChangeListener<Number> changedListener = (observable, oldValue, newValue) -> {
            String name = ((DoubleProperty) observable).getName();
            if (name.equals("x") || name.equals("y") || name.equals("width") || name.equals("height")) {
                if (name.equals("x") || name.equals("y")) annotationMoved();
                else annotationResized();
                annotationChanged();
            }
        };
        box.xProperty().addListener(changedListener);
        box.yProperty().addListener(changedListener);
        box.widthProperty().addListener(changedListener);
        box.heightProperty().addListener(changedListener);
    }

    private EventHandler<MouseEvent> currentEventHandler;
    private CreationType createdType;

    /**
     * Start annotation creation process with an specific creation type.
     *
     * @param type Specific type of creation
     */
    public void startCreation(CreationType type) {
        addToPane();
        if (type == CreationType.CLICK_DRAG) {
            currentEventHandler = new CreateClickDrag(box.getX(), box.getY());
            ((CreateClickDrag) currentEventHandler).setMouseHandlers();
        } else if (type == CreationType.CLICK_CLICK) {
            EventHandler<? super MouseEvent> defaultPressedHandler = pane.getOnMousePressed();
            currentEventHandler = new CreateClickClick(box.getX(), box.getY(), defaultPressedHandler);
            ((CreateClickClick) currentEventHandler).setMouseHandlers();
        } else {
            throw logger.throwing(new IllegalArgumentException(String.format("Unknown creation type: %s", type.toString())));
        }
        createdType = type;
    }

    private void finalizeCreation() {
        // remove mouse handlers for creation
        if (createdType == CreationType.CLICK_DRAG) {
            ((CreateClickDrag) currentEventHandler).removeMouseHandlers();
            currentEventHandler = null;
        } else if (createdType == CreationType.CLICK_CLICK) {
            ((CreateClickClick) currentEventHandler).removeMouseHandlers();
            currentEventHandler = null;
        }

        // check if minimum size is reached
        if (box.getWidth() >= MIN_WIDTH && box.getHeight() >= MIN_HEIGHT) {
            setVisible(true);
            super.finishCreation();
            bindAnnotationEvents();
            // add idle mouse handler
            currentEventHandler = new IdleMouseHandler();
            ((IdleMouseHandler) currentEventHandler).setMouseHandlers();
        } else { // remove box if not reached required size
            removeFromPane();
            super.cancelCreation();
        }
    }

    @Override
    public void imported() {
        super.imported();
        bindAnnotationEvents();
        // add idle mouse handler
        currentEventHandler = new IdleMouseHandler();
        ((IdleMouseHandler) currentEventHandler).setMouseHandlers();
    }

    @Override
    public void addToPane() {
        pane.getChildren().add(0, box);
        pane.getChildren().add(0, annotationLabel);
        // add all anchor points
        for (UIBoxAnchor.Position pos : UIBoxAnchor.Position.values()) {
            UIBoxAnchor anchor = new UIBoxAnchor(pane, box, pos);
            anchor.setEditable(false);
            anchorPoints.add(anchor);
        }
        // add idle mouse handler
        currentEventHandler = new IdleMouseHandler();
        ((IdleMouseHandler) currentEventHandler).setMouseHandlers();
    }

    @Override
    public void removeFromPane() {
        for (UIBoxAnchor anchor : anchorPoints)
            pane.getChildren().remove(anchor);
        pane.getChildren().remove(annotationLabel);
        pane.getChildren().remove(box);
    }

    @Override
    public String toString() {
        return String.format("UIBoundingBox[%d, %d, %d, %d, %s]",
                (int) box.getX(), (int) box.getY(), (int) box.getWidth(), (int) box.getHeight(),
                getAnnotationModel().getLabel());
    }

    /**
     * Handler that is used after creation
     */
    private class IdleMouseHandler implements EventHandler<MouseEvent> {

        // for dragging
        private double offsetX;
        private double offsetY;

        private void setMouseHandlers() {
            box.setOnMouseClicked(this);
            box.setOnMousePressed(this);
            box.setOnMouseDragged(this);
            box.setOnMouseReleased(this);
        }

        private void removeMouseHandlers() {
            box.setOnMouseClicked(null);
            box.setOnMousePressed(null);
            box.setOnMouseDragged(null);
            box.setOnMouseReleased(null);
        }

        @Override
        public void handle(MouseEvent event) {
            event.consume();

            if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    setEditable(!isEditable());
                }

            } else if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                if (event.isPrimaryButtonDown()) {
                    setSelected(true); // set selection

                    // put all box elements to the very front of the pane
                    box.toFront();
                    annotationLabel.toFront();
                    for (UIBoxAnchor anchor : anchorPoints)
                        anchor.toFront();

                    box.setCursor(Cursor.CLOSED_HAND);
                    // save click offset within box
                    offsetX = event.getX() - box.getX();
                    offsetY = event.getY() - box.getY();
                }

            } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                if (!event.isPrimaryButtonDown())
                    return;
                translate(event.getX() - offsetX, event.getY() - offsetY);

            } else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
                box.setCursor(Cursor.OPEN_HAND);
            }
        }

        /**
         * Translate bounding box with out-of-bound detection
         *
         * @param x position of box in x-direction
         * @param y position of box in y-direction
         */
        private void translate(double x, double y) {
            if (x < 0)
                x = 0;
            else if (x + box.getWidth() > pane.getWidth())
                x = pane.getWidth() - box.getWidth();

            if (y < 0)
                y = 0;
            else if (y + box.getHeight() > pane.getHeight())
                y = pane.getHeight() - box.getHeight();

            box.setX(x);
            box.setY(y);
        }
    }

    /**
     * For Click&Drag creation process
     */
    private class CreateClickDrag implements EventHandler<MouseEvent> {

        private final double startX;
        private final double startY;

        public CreateClickDrag(double startX, double startY) {
            this.startX = startX;
            this.startY = startY;
        }

        private void setMouseHandlers() {
            pane.setOnMouseDragged(this);
            pane.setOnMouseReleased(this);
        }

        private void removeMouseHandlers() {
            pane.setOnMouseDragged(null);
            pane.setOnMouseReleased(null);
        }

        @Override
        public void handle(MouseEvent event) {
            event.consume();
            if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
                finalizeCreation();

            } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                if (!event.isPrimaryButtonDown())
                    return;

                double deltaX = event.getX() - startX;
                double deltaY = event.getY() - startY;

                if (deltaX < 0) {
                    if (event.getX() < 0) {
                        box.setX(0);
                        box.setWidth(startX);
                    } else {
                        box.setX(event.getX());
                        box.setWidth(-deltaX);
                    }
                } else {
                    box.setX(startX);
                    if (event.getX() > pane.getWidth())
                        box.setWidth(pane.getWidth() - startX);
                    else box.setWidth(deltaX);
                }

                if (deltaY < 0) {
                    if (event.getY() < 0) {
                        box.setY(0);
                        box.setHeight(startY);
                    } else {
                        box.setY(event.getY());
                        box.setHeight(-deltaY);
                    }
                } else {
                    box.setY(startY);
                    if (event.getY() > pane.getHeight())
                        box.setHeight(pane.getHeight() - startY);
                    else box.setHeight(deltaY);
                }
            }
        }
    }

    /**
     * For Click&Click creation process
     */
    private class CreateClickClick implements EventHandler<MouseEvent> {

        private final double firstX;
        private final double firstY;

        public CreateClickClick(double firstX, double firstY, EventHandler<? super MouseEvent> defaultPressedHandler) {
            this.firstX = firstX;
            this.firstY = firstY;
        }

        private void setMouseHandlers() {
            pane.setOnMousePressed(this);
        }

        private void removeMouseHandlers() {
            pane.setOnMousePressed(null);
        }

        @Override
        public void handle(MouseEvent event) {
            event.consume();
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                double secondX = event.getX();
                double secondY = event.getY();
                // calculate bounding box
                double width = secondX - firstX;
                double height = secondY - firstY;
                if (width < 0)
                    box.setX(firstX + width);
                else box.setX(firstX);
                if (height < 0)
                    box.setY(firstY + height);
                else box.setY(firstY);
                box.setWidth(Math.abs(width));
                box.setHeight(Math.abs(height));

                // finish notification
                finalizeCreation();
            }
        }
    }
}
