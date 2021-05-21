package annotation;

import events.AnnotationChange;
import events.AnnotationMoved;
import events.AnnotationResized;
import events.CreationCanceled;
import events.CreationFinished;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.paint.Paint;
import javafx.scene.shape.StrokeType;
import modelV3.Annotation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import settings.SettingsProperties;

import java.util.ArrayList;
import java.util.List;

public abstract class UIAnnotation<A extends Annotation> {
    private static final Logger logger = LogManager.getLogger(UIAnnotation.class);
    public static final String NO_LABEL = "Undefined";

    private final A annotationModel;
    protected final AnnotationLabel annotationLabel;


    private final int originalImageWidth;
    private final int originalImageHeight;

    // TODO: Implement a display mode where no changes are possible possible

    public UIAnnotation(A annotationModel, final int originalImageWidth, final int originalImageHeight) {
        this.annotationModel = annotationModel;
        this.originalImageWidth = originalImageWidth;
        this.originalImageHeight = originalImageHeight;
        annotationLabel = new AnnotationLabel(this);

        // set new binding if one of theses attributes change (selected, verified, auto created) or hover
        ChangeListener<Boolean> attributeChanged = (observable, oldValue, newValue) -> bindNewAttributes();
        selected.addListener(attributeChanged);
        annotationModel.verifiedProperty().addListener(attributeChanged);
        annotationModel.autoCreatedProperty().addListener(attributeChanged);

        bindNewAttributes();
    }

    /**
     * Gets a UIAttribute class which is appropriate the to this annotation and switches the bindings to new attributes.
     * Priority is as follows: selected > verified > autoCreated > default
     */
    private void bindNewAttributes() {
        UIAttributes attributes;
        if (selected.get()) attributes = selectedAttributes;
        else if (annotationModel.isVerified()) attributes = verifiedAttributes;
        else if (annotationModel.isAutoCreated()) attributes = autoCreatedAttributes;
        else attributes = defaultAttributes;

        fill.unbind();
        fillHover.unbind();
        strokeColor.unbind();
        strokeColorHover.unbind();
        strokeWidth.unbind();
        strokeWidthHover.unbind();

        fill.bind(attributes.fill);
        fillHover.bind(attributes.fillHover);
        strokeColor.bind(attributes.strokeColor);
        strokeColorHover.bind(attributes.strokeColorHover);
        strokeWidth.bind(attributes.strokeWidth);
        strokeWidthHover.bind(attributes.strokeWidthHover);
    }

    public abstract void addToPane();

    public abstract void removeFromPane();

    /**
     * Will be called when object is deserialized.
     * Does the same steps that are necessary for finalizing creation.
     */
    public void imported() {
        // bind label visibility
        ReadOnlyBooleanProperty labelVisibility = UIAttributesSettings.getBooleanProperty(SettingsProperties.ANNOTATION_LABEL_VISIBLE);
        annotationLabel.visibleProperty().bind(Bindings.when(labelVisibility.and(visibleProperty())).then(true).otherwise(false));
    }

    public abstract UIAnnotation<?> copy(int originalImageWidth, int originalImageHeight);

    public UIAnnotation<?> copy() {
        return copy(originalImageWidth, originalImageHeight);
    }

    public int getOriginalImageWidth() {
        return originalImageWidth;
    }

    public int getOriginalImageHeight() {
        return originalImageHeight;
    }

    /**
     * The {@link SelectionGroup} to which this {@code UIAnnotation} belongs.
     * A {@code UIAnnotation} can only be in one group at any time.
     * If the group is changed, then the button is removed from the old group prior to being added to the new group.
     */
    private ObjectProperty<SelectionGroup> selectionGroup;

    public SelectionGroup getSelectionGroup() {
        return selectionGroup == null ? null : selectionGroup.get();
    }

    public ObjectProperty<SelectionGroup> selectionGroupProperty() {
        if (selectionGroup == null) {
            selectionGroup = new ObjectPropertyBase<>() {
                private SelectionGroup old;

                @Override
                protected void invalidated() {
                    if (old != null)
                        old.getAnnotations().remove(UIAnnotation.this);
                    final SelectionGroup sg = get();
                    if (sg != null && !sg.getAnnotations().contains(UIAnnotation.this))
                        sg.getAnnotations().add(UIAnnotation.this);
                    old = sg;
                }

                @Override
                public Object getBean() {
                    return UIAnnotation.this;
                }

                @Override
                public String getName() {
                    return "selectionGroup";
                }
            };
        }
        return selectionGroup;
    }

    public void setSelectionGroup(SelectionGroup group) {
        selectionGroupProperty().set(group);
    }

    //    public final SelectionGroup getSelectionGroup() {
//        return selectionGroup;
//    }

//    public final void setSelectionGroup(SelectionGroup group) {
//        SelectionGroup oldGroup = selectionGroup;
//        selectionGroup = group;
//        // remove from old group
//        if (oldGroup != null)
//            oldGroup.getAnnotations().remove(this);
//        // add to new group
//        if (group != null)
//            group.getAnnotations().add(this);
//    }

    private BooleanProperty selected;

    public boolean isSelected() {
        return selected.get();
    }

    public BooleanProperty selectedProperty() {
        if (selected == null) {
            selected = new BooleanPropertyBase(false) {
                @Override
                protected void invalidated() {
                    final SelectionGroup sg = getSelectionGroup();
                    if (sg == null)
                        return;
                    final boolean selected = get();
                    if (selected) {
                        sg.selectAnnotation(UIAnnotation.this);
                    } else if (sg.getSelectedAnnotation() == UIAnnotation.this) {
                        // Clear the selected toggle only if there are no other toggles selected.
                        if (!getSelectionGroup().getSelectedAnnotation().isSelected()) {
                            for (UIAnnotation<?> a : getSelectionGroup().getAnnotations()) {
                                if (a.isSelected())
                                    return;
                            }
                        }
                        getSelectionGroup().selectAnnotation(null);
                    }
                }

                @Override
                public Object getBean() {
                    return UIAnnotation.this;
                }

                @Override
                public String getName() {
                    return "selected";
                }
            };
        }
        return selected;
    }

    public void setSelected(boolean selected) {
        // TODO: check with selection Group
        selectedProperty().set(selected);
    }

    private ReadOnlyBooleanWrapper editable;

    public boolean isEditable() {
        return editableProperty().get();
    }

    public ReadOnlyBooleanProperty editableProperty() {
        if (editable == null)
            editable = new ReadOnlyBooleanWrapper(false);
        return editable.getReadOnlyProperty();
    }

    public void setEditable(boolean value) {
        if (editable == null)
            editable = new ReadOnlyBooleanWrapper();
        editable.set(value);
    }

    private List<AnnotationChange> changedListeners;

    protected void annotationChanged() {
        if (changedListeners == null)
            changedListeners = new ArrayList<>();
        for (AnnotationChange c : changedListeners)
            c.changed(this);
    }

    public void addAnnotationChangedListener(AnnotationChange listener) {
        if (changedListeners == null)
            changedListeners = new ArrayList<>();
        changedListeners.add(listener);
    }

    public void removeAnnotationChangedListener(AnnotationChange listener) {
        if (changedListeners == null)
            changedListeners = new ArrayList<>();
        changedListeners.remove(listener);
    }

    private List<AnnotationResized> resizedListeners;

    protected void annotationResized() {
        if (resizedListeners == null)
            resizedListeners = new ArrayList<>();
        for (AnnotationResized r : resizedListeners)
            r.resized(this);
    }

    public void addAnnotationResizedListener(AnnotationResized listener) {
        if (resizedListeners == null)
            resizedListeners = new ArrayList<>();
        resizedListeners.add(listener);
    }

    public void removeAnnotationResizedListener(AnnotationResized listener) {
        if (resizedListeners == null)
            resizedListeners = new ArrayList<>();
        resizedListeners.remove(listener);
    }

    private List<AnnotationMoved> movedListeners;


    protected void annotationMoved() {
        if (movedListeners == null)
            movedListeners = new ArrayList<>();
        for (AnnotationMoved m : movedListeners)
            m.moved(this);
    }

    public void addAnnotationMovedListener(AnnotationMoved listener) {
        if (movedListeners == null)
            movedListeners = new ArrayList<>();
        movedListeners.add(listener);
    }

    public void removeAnnotationMovedListener(AnnotationMoved listener) {
        if (movedListeners == null)
            movedListeners = new ArrayList<>();
        movedListeners.remove(listener);
    }

    // one-time listener to notify that creation of annotation is done
    private CreationFinished finishedListener;
    private CreationCanceled canceledListener;


    /**
     * Sets a single one-time listener for a successful creation.
     * Must be added before the creation process started.
     *
     * @param finished listener that gets called after creation
     */
    public void setOnCreationFinished(CreationFinished finished) {
        finishedListener = finished;
    }

    /**
     * Sets a single one-time listener for a canceled creation.
     * Must be added before the creation process started.
     *
     * @param canceled listener that gets called when creation was canceled.
     */
    public void setOnCreatedCanceled(CreationCanceled canceled) {
        canceledListener = canceled;
    }

    /**
     * Notifies finish listeners.
     */
    protected void finishCreation() {
        if (finishedListener != null)
            finishedListener.finished(this);
        finishedListener = null;
        canceledListener = null;
        // bind label visibility
        ReadOnlyBooleanProperty labelVisibility = UIAttributesSettings.getBooleanProperty(SettingsProperties.ANNOTATION_LABEL_VISIBLE);
        annotationLabel.visibleProperty().bind(Bindings.when(labelVisibility.and(visibleProperty())).then(true).otherwise(false));
    }

    /**
     * Notifies cancel listeners
     */
    protected void cancelCreation() {
        if (canceledListener != null)
            canceledListener.canceled(this);
        finishedListener = null;
        canceledListener = null;
    }

    private BooleanProperty annotationVisibility;

    public BooleanProperty visibleProperty() {
        if (annotationVisibility == null)
            annotationVisibility = new SimpleBooleanProperty(true);
        return annotationVisibility;
    }

    public boolean isVisible() {
        return visibleProperty().get();
    }

    public void setVisible(boolean value) {
        visibleProperty().set(value);
    }

    private ReadOnlyBooleanWrapper annotationHover;

    protected ReadOnlyBooleanWrapper annotationHoverProperty() {
        if (annotationHover == null)
            annotationHover = new ReadOnlyBooleanWrapper();
        return annotationHover;
    }

    public ReadOnlyBooleanProperty hoverProperty() {
        if (annotationHover == null)
            annotationHover = new ReadOnlyBooleanWrapper();
        return annotationHover.getReadOnlyProperty();
    }

    public boolean isHover() {
        return hoverProperty().get();
    }

    public A getAnnotationModel() {
        return annotationModel;
    }

    public StrokeType getStrokeType() {
        return StrokeType.INSIDE;
    }

    private final ObjectProperty<Paint> strokeColor = new SimpleObjectProperty<>();

    public ReadOnlyObjectProperty<Paint> strokeColorProperty() {
        return strokeColor;
    }

    private final ObjectProperty<Paint> strokeColorHover = new SimpleObjectProperty<>();

    public ReadOnlyObjectProperty<Paint> strokeColorHoverProperty() {
        return strokeColorHover;
    }

    private final IntegerProperty strokeWidth = new SimpleIntegerProperty();

    public ReadOnlyIntegerProperty strokeWidthProperty() {
        return strokeWidth;
    }

    private final IntegerProperty strokeWidthHover = new SimpleIntegerProperty();

    public ReadOnlyIntegerProperty strokeWidthHoverProperty() {
        return strokeWidthHover;
    }

    private final ObjectProperty<Paint> fill = new SimpleObjectProperty<>();

    public ReadOnlyObjectProperty<Paint> fillProperty() {
        return fill;
    }

    private final ObjectProperty<Paint> fillHover = new SimpleObjectProperty<>();

    public ReadOnlyObjectProperty<Paint> fillHoverProperty() {
        return fillHover;
    }

    private final UIAttributes selectedAttributes = new UIAttributes("selected");
    private final UIAttributes verifiedAttributes = new UIAttributes("verified");
    private final UIAttributes autoCreatedAttributes = new UIAttributes("auto");
    private final UIAttributes defaultAttributes = new UIAttributes("default");

    private static final class UIAttributes {
        private final ObjectProperty<Paint> fill = new SimpleObjectProperty<>();
        private final ObjectProperty<Paint> fillHover = new SimpleObjectProperty<>();
        private final ObjectProperty<Paint> strokeColor = new SimpleObjectProperty<>();
        private final ObjectProperty<Paint> strokeColorHover = new SimpleObjectProperty<>();
        private final IntegerProperty strokeWidth = new SimpleIntegerProperty();
        private final IntegerProperty strokeWidthHover = new SimpleIntegerProperty();

        private UIAttributes(String styleClass) {
            switch (styleClass) {
                case "selected":
                    fill.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_FILL_COLOR_SELECTED));
                    fillHover.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_FILL_COLOR_HOVER_SELECTED));
                    strokeColor.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_STROKE_COLOR_SELECTED));
                    strokeColorHover.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_STROKE_COLOR_HOVER_SELECTED));
                    strokeWidth.bind(UIAttributesSettings.getIntegerProperty(SettingsProperties.ANNOTATION_STROKE_WIDTH_SELECTED));
                    strokeWidthHover.bind(UIAttributesSettings.getIntegerProperty(SettingsProperties.ANNOTATION_STROKE_WIDTH_HOVER_SELECTED));
                    break;
                case "verified":
                    fill.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_FILL_COLOR_VERIFIED));
                    fillHover.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_FILL_COLOR_HOVER_VERIFIED));
                    strokeColor.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_STROKE_COLOR_VERIFIED));
                    strokeColorHover.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_STROKE_COLOR_HOVER_VERIFIED));
                    strokeWidth.bind(UIAttributesSettings.getIntegerProperty(SettingsProperties.ANNOTATION_STROKE_WIDTH_VERIFIED));
                    strokeWidthHover.bind(UIAttributesSettings.getIntegerProperty(SettingsProperties.ANNOTATION_STROKE_WIDTH_HOVER_VERIFIED));
                    break;
                case "auto":
                    fill.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_FILL_COLOR_AUTO));
                    fillHover.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_FILL_COLOR_HOVER_AUTO));
                    strokeColor.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_STROKE_COLOR_AUTO));
                    strokeColorHover.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_STROKE_COLOR_HOVER_AUTO));
                    strokeWidth.bind(UIAttributesSettings.getIntegerProperty(SettingsProperties.ANNOTATION_STROKE_WIDTH_AUTO));
                    strokeWidthHover.bind(UIAttributesSettings.getIntegerProperty(SettingsProperties.ANNOTATION_STROKE_WIDTH_HOVER_AUTO));
                    break;
                case "default":
                    fill.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_FILL_COLOR_DEFAULT));
                    fillHover.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_FILL_COLOR_HOVER_DEFAULT));
                    strokeColor.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_STROKE_COLOR_DEFAULT));
                    strokeColorHover.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_STROKE_COLOR_HOVER_DEFAULT));
                    strokeWidth.bind(UIAttributesSettings.getIntegerProperty(SettingsProperties.ANNOTATION_STROKE_WIDTH_DEFAULT));
                    strokeWidthHover.bind(UIAttributesSettings.getIntegerProperty(SettingsProperties.ANNOTATION_STROKE_WIDTH_HOVER_DEFAULT));
                    break;
                default:
                    throw logger.throwing(new IllegalArgumentException(String.format("Unknown style class '%s'", styleClass)));
            }
        }
    }
}
