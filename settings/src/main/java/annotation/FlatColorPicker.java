package annotation;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ColorPickerSkin;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import settings.SettingsProperties;

public class FlatColorPicker extends ColorPicker {
    private static final Logger logger = LogManager.getLogger(FlatColorPicker.class);

    private final StringProperty propertyKey = new SimpleStringProperty();
    private Label displayLabel;

    public FlatColorPicker() {
        getStyleClass().clear();
        setCursor(Cursor.HAND);
        setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        setPadding(new Insets(2));
        setMinWidth(100);
        setPrefWidth(100);

        // add listener so that background equals the picked color
        backgroundProperty().bind(Bindings.createObjectBinding(
                () -> {
                    if (displayLabel != null) displayLabel.setTextFill(getComplementary(getValue()));
                    return new Background(new BackgroundFill(getValue(), CornerRadii.EMPTY, Insets.EMPTY));
                },
                valueProperty())
        );

        propertyKey.addListener((observable, oldKey, newKey) -> setValue(getColorProperty(newKey)));
    }

    private Color getComplementary(Color color) {
        if (color.getOpacity() > 0.25) {
            Color inv = color.invert();
            return new Color(inv.getRed(), inv.getGreen(), inv.getBlue(), 1);
        } else return Color.BLACK;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        ColorPickerSkin skin = (ColorPickerSkin) super.createDefaultSkin();
        displayLabel = (Label) skin.getDisplayNode();
        displayLabel.setText(getValue().toString());
        displayLabel.setTextFill(getComplementary(getValue()));
        displayLabel.setGraphic(null);
        displayLabel.textProperty().addListener((observable, oldValue, newValue) -> displayLabel.setText(getValue().toString()));
        return skin;
    }

    private Color getColorProperty(String property) {
        try {
            return Color.valueOf(SettingsProperties.getProperty(property));
        } catch (NullPointerException e) {
            throw logger.throwing(e);
        }
    }

    public String getPropertyKey() {
        return propertyKey.get();
    }

    public StringProperty propertyKeyProperty() {
        return propertyKey;
    }

    public void setPropertyKey(String key) {
        propertyKey.set(key);
    }
}
