package annotation;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import settings.SettingsProperties;

public class AnnotationLabel extends Group implements EventHandler<MouseEvent> {
    private final ObjectProperty<Paint> fill = new SimpleObjectProperty<>();
    private final ObjectProperty<Paint> fillHover = new SimpleObjectProperty<>();
    private final ObjectProperty<Paint> fontColor = new SimpleObjectProperty<>();
    private final ObjectProperty<Paint> fontColorHover = new SimpleObjectProperty<>();
    private final ObjectProperty<Paint> borderColor = new SimpleObjectProperty<>();
    private final ObjectProperty<BorderStrokeStyle> borderStyle = new SimpleObjectProperty<>();

    private final UIAttributes selectedAttributes = new UIAttributes("selected");
    private final UIAttributes verifiedAttributes = new UIAttributes("verified");
    private final UIAttributes autoCreatedAttributes = new UIAttributes("auto");
    private final UIAttributes defaultAttributes = new UIAttributes("default");

    private final Label label = new Label();
    private final UIAnnotation<?> annotation;

    public AnnotationLabel(UIAnnotation<?> annotation) {
        this.annotation = annotation;
        label.setStyle("-fx-opacity: 1.0;"); // set disabled opacity to 1.0
        label.setPadding(new Insets(1, 2, 1, 2));

        setMouseTransparent(false);
        setCursor(Cursor.HAND);
        getChildren().add(label);
        setVisible(false); // hide label at beginning (during creation)

        // bind and set UI attributes to label
        ChangeListener<Boolean> attributeChanged = (observable, oldValue, newValue) -> bindNewAttributes();
        annotation.selectedProperty().addListener(attributeChanged);
        annotation.getAnnotationModel().verifiedProperty().addListener(attributeChanged);
        annotation.getAnnotationModel().autoCreatedProperty().addListener(attributeChanged);
        bindNewAttributes();

        StringProperty labelProperty = annotation.getAnnotationModel().labelProperty();
        label.textProperty().bind(Bindings.when(labelProperty.isEmpty())
                .then(UIAnnotation.NO_LABEL)
                .otherwise(labelProperty));

        label.textFillProperty().bind(Bindings.when(annotation.hoverProperty().or(label.hoverProperty()))
                .then(fontColorHover)
                .otherwise(fontColor));

        label.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
            Paint bgColor = annotation.isHover() || label.isHover() ? fillHover.get() : fill.get();
            return new Background(new BackgroundFill(bgColor, CornerRadii.EMPTY, Insets.EMPTY));
        }, annotation.hoverProperty(), label.hoverProperty(), fill, fillHover));


        label.borderProperty().bind(Bindings.createObjectBinding(
                () -> new Border(new BorderStroke(borderColor.get(), borderStyle.get(),
                        CornerRadii.EMPTY, BorderWidths.DEFAULT)),
                borderColor, borderStyle)
        );

        setOnMousePressed(this);
        setOnMouseClicked(this);
    }

    /**
     * Gets a UIAttribute class which is appropriate the to this annotation and switches the bindings to new attributes.
     * Priority is as follows: selected > verified > autoCreated > default
     */
    private void bindNewAttributes() {
        UIAttributes attributes;
        if (annotation.selectedProperty().get()) attributes = selectedAttributes;
        else if (annotation.getAnnotationModel().isVerified()) attributes = verifiedAttributes;
        else if (annotation.getAnnotationModel().isAutoCreated()) attributes = autoCreatedAttributes;
        else attributes = defaultAttributes;

        fill.unbind();
        fillHover.unbind();
        fontColor.unbind();
        fontColorHover.unbind();
        borderColor.unbind();
        borderStyle.unbind();

        fill.bind(attributes.fill);
        fillHover.bind(attributes.fillHover);
        fontColor.bind(attributes.fontColor);
        fontColorHover.bind(attributes.fontColorHover);
        borderColor.bind(attributes.borderColor);
        borderStyle.bind(attributes.borderStyle);
    }

    public ReadOnlyDoubleProperty widthProperty() {
        return label.widthProperty();
    }

    public ReadOnlyDoubleProperty heightProperty() {
        return label.heightProperty();
    }

    @Override
    public void handle(MouseEvent event) {
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            event.consume();
        } else if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {
            if (event.getClickCount() != 2 || event.getButton() != MouseButton.PRIMARY)
                return;
            event.consume();

            TextField input = new TextField(annotation.getAnnotationModel().getLabel());
            input.getStyleClass().clear();
            input.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
            label.setVisible(false);

            input.setOnAction(e -> {
                annotation.getAnnotationModel().setLabel(input.getText().strip());
                getChildren().remove(input);
            });

            input.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    getChildren().remove(input);
                    label.setVisible(true);
                }
            });

            getChildren().add(input);
            input.requestFocus();
            input.selectAll();
        }
    }

    private static class UIAttributes {
        private final ObjectProperty<Paint> fill = new SimpleObjectProperty<>();
        private final ObjectProperty<Paint> fillHover = new SimpleObjectProperty<>();
        private final ObjectProperty<Paint> fontColor = new SimpleObjectProperty<>();
        private final ObjectProperty<Paint> fontColorHover = new SimpleObjectProperty<>();
        private final ObjectProperty<Paint> borderColor = new SimpleObjectProperty<>();
        private final ObjectProperty<BorderStrokeStyle> borderStyle = new SimpleObjectProperty<>();

        private UIAttributes(String styleClass) {
            switch (styleClass) {
                case "selected":
                    fill.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_LABEL_FILL_COLOR_SELECTED));
                    fillHover.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_LABEL_FILL_COLOR_HOVER_SELECTED));
                    fontColor.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_LABEL_FONT_COLOR_SELECTED));
                    fontColorHover.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_LABEL_FONT_COLOR_HOVER_SELECTED));
                    borderColor.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_LABEL_BORDER_COLOR_SELECTED));
                    borderStyle.bind(UIAttributesSettings.getBorderStyleProperty(SettingsProperties.ANNOTATION_LABEL_BORDER_STYLE_SELECTED));
                    break;
                case "verified":
                    fill.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_LABEL_FILL_COLOR_VERIFIED));
                    fillHover.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_LABEL_FILL_COLOR_HOVER_VERIFIED));
                    fontColor.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_LABEL_FONT_COLOR_VERIFIED));
                    fontColorHover.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_LABEL_FONT_COLOR_HOVER_VERIFIED));
                    borderColor.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_LABEL_BORDER_COLOR_VERIFIED));
                    borderStyle.bind(UIAttributesSettings.getBorderStyleProperty(SettingsProperties.ANNOTATION_LABEL_BORDER_STYLE_VERIFIED));
                    break;
                case "auto":
                    fill.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_LABEL_FILL_COLOR_AUTO));
                    fillHover.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_LABEL_FILL_COLOR_HOVER_AUTO));
                    fontColor.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_LABEL_FONT_COLOR_AUTO));
                    fontColorHover.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_LABEL_FONT_COLOR_HOVER_AUTO));
                    borderColor.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_LABEL_BORDER_COLOR_AUTO));
                    borderStyle.bind(UIAttributesSettings.getBorderStyleProperty(SettingsProperties.ANNOTATION_LABEL_BORDER_STYLE_AUTO));
                    break;
                case "default":
                    fill.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_LABEL_FILL_COLOR_DEFAULT));
                    fillHover.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_LABEL_FILL_COLOR_HOVER_DEFAULT));
                    fontColor.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_LABEL_FONT_COLOR_DEFAULT));
                    fontColorHover.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_LABEL_FONT_COLOR_HOVER_DEFAULT));
                    borderColor.bind(UIAttributesSettings.getColorProperty(SettingsProperties.ANNOTATION_LABEL_BORDER_COLOR_DEFAULT));
                    borderStyle.bind(UIAttributesSettings.getBorderStyleProperty(SettingsProperties.ANNOTATION_LABEL_BORDER_STYLE_DEFAULT));
                    break;
                default:
            }
        }
    }
}
