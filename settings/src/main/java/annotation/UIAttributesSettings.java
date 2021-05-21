package annotation;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import settings.SettingsProperties;

import java.util.HashMap;
import java.util.Map;

public class UIAttributesSettings {
    private static final Logger logger = LogManager.getLogger(UIAttributesSettings.class);

    private static final ObjectProperty<Paint> annotationFillSelected = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationFillHoverSelected = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationStrokeColorSelected = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationStrokeColorHoverSelected = new SimpleObjectProperty<>();
    private static final IntegerProperty annotationStrokeWidthSelected = new SimpleIntegerProperty();
    private static final IntegerProperty annotationStrokeWidthHoverSelected = new SimpleIntegerProperty();

    private static final ObjectProperty<Paint> annotationFillVerified = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationFillHoverVerified = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationStrokeColorVerified = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationStrokeColorHoverVerified = new SimpleObjectProperty<>();
    private static final IntegerProperty annotationStrokeWidthVerified = new SimpleIntegerProperty();
    private static final IntegerProperty annotationStrokeWidthHoverVerified = new SimpleIntegerProperty();

    private static final ObjectProperty<Paint> annotationFillAuto = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationFillHoverAuto = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationStrokeColorAuto = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationStrokeColorHoverAuto = new SimpleObjectProperty<>();
    private static final IntegerProperty annotationStrokeWidthAuto = new SimpleIntegerProperty();
    private static final IntegerProperty annotationStrokeWidthHoverAuto = new SimpleIntegerProperty();

    private static final ObjectProperty<Paint> annotationFillDefault = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationFillHoverDefault = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationStrokeColorDefault = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationStrokeColorHoverDefault = new SimpleObjectProperty<>();
    private static final IntegerProperty annotationStrokeWidthDefault = new SimpleIntegerProperty();
    private static final IntegerProperty annotationStrokeWidthHoverDefault = new SimpleIntegerProperty();

    private static final ObjectProperty<Paint> annotationLabelFillSelected = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationLabelFillHoverSelected = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationLabelFontColorSelected = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationLabelFontColorHoverSelected = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationLabelBorderColorSelected = new SimpleObjectProperty<>();
    private static final ObjectProperty<BorderStrokeStyle> annotationLabelBorderStyleSelected = new SimpleObjectProperty<>();

    private static final ObjectProperty<Paint> annotationLabelFillVerified = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationLabelFillHoverVerified = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationLabelFontColorVerified = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationLabelFontColorHoverVerified = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationLabelBorderColorVerified = new SimpleObjectProperty<>();
    private static final ObjectProperty<BorderStrokeStyle> annotationLabelBorderStyleVerified = new SimpleObjectProperty<>();

    private static final ObjectProperty<Paint> annotationLabelFillAuto = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationLabelFillHoverAuto = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationLabelFontColorAuto = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationLabelFontColorHoverAuto = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationLabelBorderColorAuto = new SimpleObjectProperty<>();
    private static final ObjectProperty<BorderStrokeStyle> annotationLabelBorderStyleAuto = new SimpleObjectProperty<>();

    private static final ObjectProperty<Paint> annotationLabelFillDefault = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationLabelFillHoverDefault = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationLabelFontColorDefault = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationLabelFontColorHoverDefault = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationLabelBorderColorDefault = new SimpleObjectProperty<>();
    private static final ObjectProperty<BorderStrokeStyle> annotationLabelBorderStyleDefault = new SimpleObjectProperty<>();

    private static final BooleanProperty annotationLabelVisibility = new SimpleBooleanProperty();

    private static final ObjectProperty<Paint> annotationAnchorFillDefault = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationAnchorFillHover = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationAnchorStrokeColorDefault = new SimpleObjectProperty<>();
    private static final ObjectProperty<Paint> annotationLabelStrokeColorHover = new SimpleObjectProperty<>();
    private static final IntegerProperty annotationAnchorSizeDefault = new SimpleIntegerProperty();
    private static final IntegerProperty annotationAnchorSizeHover = new SimpleIntegerProperty();

    private static final Map<String, ObjectProperty<Paint>> colorAttributeProperties = new HashMap<>();
    private static final Map<String, IntegerProperty> integerAttributeProperties = new HashMap<>();
    private static final Map<String, ObjectProperty<BorderStrokeStyle>> borderStyleAttributeProperties = new HashMap<>();
    private static final Map<String, BooleanProperty> booleanAttributeProperties = new HashMap<>();

    static {
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_FILL_COLOR_SELECTED, annotationFillSelected);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_FILL_COLOR_HOVER_SELECTED, annotationFillHoverSelected);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_STROKE_COLOR_SELECTED, annotationStrokeColorSelected);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_STROKE_COLOR_HOVER_SELECTED, annotationStrokeColorHoverSelected);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_FILL_COLOR_VERIFIED, annotationFillVerified);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_FILL_COLOR_HOVER_VERIFIED, annotationFillHoverVerified);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_STROKE_COLOR_VERIFIED, annotationStrokeColorVerified);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_STROKE_COLOR_HOVER_VERIFIED, annotationStrokeColorHoverVerified);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_FILL_COLOR_AUTO, annotationFillAuto);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_FILL_COLOR_HOVER_AUTO, annotationFillHoverAuto);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_STROKE_COLOR_AUTO, annotationStrokeColorAuto);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_STROKE_COLOR_HOVER_AUTO, annotationStrokeColorHoverAuto);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_FILL_COLOR_DEFAULT, annotationFillDefault);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_FILL_COLOR_HOVER_DEFAULT, annotationFillHoverDefault);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_STROKE_COLOR_DEFAULT, annotationStrokeColorDefault);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_STROKE_COLOR_HOVER_DEFAULT, annotationStrokeColorHoverDefault);

        colorAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_FILL_COLOR_SELECTED, annotationLabelFillSelected);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_FILL_COLOR_HOVER_SELECTED, annotationLabelFillHoverSelected);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_FONT_COLOR_SELECTED, annotationLabelFontColorSelected);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_FONT_COLOR_HOVER_SELECTED, annotationLabelFontColorHoverSelected);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_BORDER_COLOR_SELECTED, annotationLabelBorderColorSelected);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_FILL_COLOR_VERIFIED, annotationLabelFillVerified);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_FILL_COLOR_HOVER_VERIFIED, annotationLabelFillHoverVerified);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_FONT_COLOR_VERIFIED, annotationLabelFontColorVerified);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_FONT_COLOR_HOVER_VERIFIED, annotationLabelFontColorHoverVerified);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_BORDER_COLOR_VERIFIED, annotationLabelBorderColorVerified);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_FILL_COLOR_AUTO, annotationLabelFillAuto);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_FILL_COLOR_HOVER_AUTO, annotationLabelFillHoverAuto);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_FONT_COLOR_AUTO, annotationLabelFontColorAuto);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_FONT_COLOR_HOVER_AUTO, annotationLabelFontColorHoverAuto);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_BORDER_COLOR_AUTO, annotationLabelBorderColorAuto);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_FILL_COLOR_DEFAULT, annotationLabelFillDefault);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_FILL_COLOR_HOVER_DEFAULT, annotationLabelFillHoverDefault);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_FONT_COLOR_DEFAULT, annotationLabelFontColorDefault);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_FONT_COLOR_HOVER_DEFAULT, annotationLabelFontColorHoverDefault);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_BORDER_COLOR_DEFAULT, annotationLabelBorderColorDefault);

        colorAttributeProperties.put(SettingsProperties.ANNOTATION_ANCHOR_FILL_COLOR_DEFAULT, annotationAnchorFillDefault);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_ANCHOR_FILL_COLOR_HOVER, annotationAnchorFillHover);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_ANCHOR_STROKE_COLOR_DEFAULT, annotationAnchorStrokeColorDefault);
        colorAttributeProperties.put(SettingsProperties.ANNOTATION_ANCHOR_STROKE_COLOR_HOVER, annotationLabelStrokeColorHover);

        integerAttributeProperties.put(SettingsProperties.ANNOTATION_STROKE_WIDTH_SELECTED, annotationStrokeWidthSelected);
        integerAttributeProperties.put(SettingsProperties.ANNOTATION_STROKE_WIDTH_HOVER_SELECTED, annotationStrokeWidthHoverSelected);
        integerAttributeProperties.put(SettingsProperties.ANNOTATION_STROKE_WIDTH_VERIFIED, annotationStrokeWidthVerified);
        integerAttributeProperties.put(SettingsProperties.ANNOTATION_STROKE_WIDTH_HOVER_VERIFIED, annotationStrokeWidthHoverVerified);
        integerAttributeProperties.put(SettingsProperties.ANNOTATION_STROKE_WIDTH_AUTO, annotationStrokeWidthAuto);
        integerAttributeProperties.put(SettingsProperties.ANNOTATION_STROKE_WIDTH_HOVER_AUTO, annotationStrokeWidthHoverAuto);
        integerAttributeProperties.put(SettingsProperties.ANNOTATION_STROKE_WIDTH_DEFAULT, annotationStrokeWidthDefault);
        integerAttributeProperties.put(SettingsProperties.ANNOTATION_STROKE_WIDTH_HOVER_DEFAULT, annotationStrokeWidthHoverDefault);
        integerAttributeProperties.put(SettingsProperties.ANNOTATION_ANCHOR_SIZE_DEFAULT, annotationAnchorSizeDefault);
        integerAttributeProperties.put(SettingsProperties.ANNOTATION_ANCHOR_SIZE_HOVER, annotationAnchorSizeHover);

        borderStyleAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_BORDER_STYLE_SELECTED, annotationLabelBorderStyleSelected);
        borderStyleAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_BORDER_STYLE_VERIFIED, annotationLabelBorderStyleVerified);
        borderStyleAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_BORDER_STYLE_AUTO, annotationLabelBorderStyleAuto);
        borderStyleAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_BORDER_STYLE_DEFAULT, annotationLabelBorderStyleDefault);

        booleanAttributeProperties.put(SettingsProperties.ANNOTATION_LABEL_VISIBLE, annotationLabelVisibility);

        // init properties
        for (String propKey : colorAttributeProperties.keySet())
            colorAttributeProperties.get(propKey).set(Color.valueOf(SettingsProperties.getProperty(propKey)));
        for (String propKey : integerAttributeProperties.keySet())
            integerAttributeProperties.get(propKey).set(Integer.parseInt(SettingsProperties.getProperty(propKey)));
        for (String propKey : borderStyleAttributeProperties.keySet())
            borderStyleAttributeProperties.get(propKey).set(getBorderStyle(SettingsProperties.getProperty(propKey)));
        for (String propKey : booleanAttributeProperties.keySet())
            booleanAttributeProperties.get(propKey).set(Boolean.parseBoolean(SettingsProperties.getProperty(propKey)));
    }

    private static BorderStrokeStyle getBorderStyle(String styleName) {
        switch (styleName) {
            case "solid":
                return BorderStrokeStyle.SOLID;
            case "dashed":
                return BorderStrokeStyle.DASHED;
            case "dotted":
                return BorderStrokeStyle.DOTTED;
            default:
                throw logger.throwing(new IllegalArgumentException(String.format("Unknown border style '%s'", styleName)));
        }
    }

    public static ReadOnlyObjectProperty<Paint> getColorProperty(String propertyKey) {
        if (propertyKey != null && colorAttributeProperties.containsKey(propertyKey))
            return colorAttributeProperties.get(propertyKey);
        throw logger.throwing(new RuntimeException(String.format("Color property key '%s' does not exists", propertyKey)));
    }

    public static ReadOnlyIntegerProperty getIntegerProperty(String propertyKey) {
        if (propertyKey != null && integerAttributeProperties.containsKey(propertyKey))
            return integerAttributeProperties.get(propertyKey);
        throw logger.throwing(new RuntimeException(String.format("Integer property key '%s' does not exists", propertyKey)));
    }

    public static ReadOnlyObjectProperty<BorderStrokeStyle> getBorderStyleProperty(String propertyKey) {
        if (propertyKey != null && borderStyleAttributeProperties.containsKey(propertyKey))
            return borderStyleAttributeProperties.get(propertyKey);
        throw logger.throwing(new RuntimeException(String.format("Border style property key '%s' does not exists", propertyKey)));
    }

    public static ReadOnlyBooleanProperty getBooleanProperty(String propertyKey) {
        if (propertyKey != null && booleanAttributeProperties.containsKey(propertyKey))
            return booleanAttributeProperties.get(propertyKey);
        throw logger.throwing(new RuntimeException(String.format("Boolean property key '%s' does not exists", propertyKey)));
    }

    public static void setUIAttribute(String propertyKey, String value) {
        if (propertyKey == null || value == null)
            throw logger.throwing(new NullPointerException("Property key and vallue must not be null"));

        if (colorAttributeProperties.containsKey(propertyKey))
            colorAttributeProperties.get(propertyKey).set(Color.valueOf(value));
        else if (integerAttributeProperties.containsKey(propertyKey))
            integerAttributeProperties.get(propertyKey).set(Integer.parseInt(value));
        else if (borderStyleAttributeProperties.containsKey(propertyKey))
            borderStyleAttributeProperties.get(propertyKey).set(getBorderStyle(value));
        else if (booleanAttributeProperties.containsKey(propertyKey))
            booleanAttributeProperties.get(propertyKey).set(Boolean.parseBoolean(value));
        else throw logger.throwing(new RuntimeException(String.format("Property key '%s' does not exists",
                    propertyKey)));
    }
}
