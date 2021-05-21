package controller;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import settings.SettingsProperties;

import java.lang.reflect.Field;

public class AnnotationSettingsController implements SettingsBaseController {
    @FXML
    private ColorPicker colorPickerAnnotationFillDefault;
    @FXML
    private ColorPicker colorPickerAnnotationFillHoverDefault;
    @FXML
    private ColorPicker colorPickerAnnotationStrokeDefault;
    @FXML
    private Spinner<Integer> spinnerAnnotationStrokeWidth;

    @FXML
    private ColorPicker colorPickerAnnotationFillSelected;
    @FXML
    private ColorPicker colorPickerAnnotationFillHoverSelected;
    @FXML
    private ColorPicker colorPickerAnnotationStrokeSelected;

    @FXML
    private ColorPicker colorPickerAnnotationFillVerified;
    @FXML
    private ColorPicker colorPickerAnnotationFillHoverVerified;
    @FXML
    private ColorPicker colorPickerAnnotationStrokeVerified;

    @FXML
    private CheckBox checkBoxLabelVisible;
    @FXML
    private ColorPicker colorPickerLabelFillDefault;
    @FXML
    private ColorPicker colorPickerLabelFontDefault;
    @FXML
    private ColorPicker colorPickerLabelFillVerified;
    @FXML
    private ColorPicker colorPickerLabelFontVerified;
    @FXML
    private ColorPicker colorPickerLabelAutoCreatedStroke;
    @FXML
    private ChoiceBox<String> choiceBoxLabelAutoCreatedStrokeStyle;

    @FXML
    private ColorPicker colorPickerAnchorFillDefault;
    @FXML
    private ColorPicker colorPickerAnchorFillHoverDefault;
    @FXML
    private ColorPicker colorPickerAnchorStrokeDefault;
    @FXML
    private ColorPicker colorPickerAnchorStrokeHoverDefault;
    @FXML
    private Spinner<Integer> spinnerAnchorSizeDefault;

    @FXML
    private void initialize() {
        colorPickerAnnotationFillDefault.setValue(getColorProperty(SettingsProperties.ANNOTATION_FILL_COLOR_DEFAULT));
        colorPickerAnnotationFillHoverDefault.setValue(getColorProperty(SettingsProperties.ANNOTATION_FILL_COLOR_HOVER_DEFAULT));
        colorPickerAnnotationStrokeDefault.setValue(getColorProperty(SettingsProperties.ANNOTATION_STROKE_COLOR_DEFAULT));
        int defaultStrokeWidth = Integer.parseInt(SettingsProperties.getProperty(SettingsProperties.ANNOTATION_STROKE_WIDTH_DEFAULT));
        spinnerAnnotationStrokeWidth.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, defaultStrokeWidth, 1));

        colorPickerAnnotationFillSelected.setValue(getColorProperty(SettingsProperties.ANNOTATION_FILL_COLOR_SELECTED));
        colorPickerAnnotationFillHoverSelected.setValue(getColorProperty(SettingsProperties.ANNOTATION_FILL_COLOR_HOVER_SELECTED));
        colorPickerAnnotationStrokeSelected.setValue(getColorProperty(SettingsProperties.ANNOTATION_STROKE_COLOR_SELECTED));

        colorPickerAnnotationFillVerified.setValue(getColorProperty(SettingsProperties.ANNOTATION_FILL_COLOR_VERIFIED));
        colorPickerAnnotationFillHoverVerified.setValue(getColorProperty(SettingsProperties.ANNOTATION_FILL_COLOR_HOVER_VERIFIED));
        colorPickerAnnotationStrokeVerified.setValue(getColorProperty(SettingsProperties.ANNOTATION_STROKE_COLOR_VERIFIED));

        checkBoxLabelVisible.setSelected(Boolean.parseBoolean(SettingsProperties.getProperty(SettingsProperties.ANNOTATION_LABEL_VISIBLE)));
        colorPickerLabelFillDefault.setValue(getColorProperty(SettingsProperties.ANNOTATION_LABEL_FILL_COLOR_DEFAULT));
        colorPickerLabelFontDefault.setValue(getColorProperty(SettingsProperties.ANNOTATION_LABEL_FONT_COLOR_DEFAULT));
        colorPickerLabelFillVerified.setValue(getColorProperty(SettingsProperties.ANNOTATION_LABEL_FILL_COLOR_VERIFIED));
        colorPickerLabelFontVerified.setValue(getColorProperty(SettingsProperties.ANNOTATION_LABEL_FONT_COLOR_VERIFIED));
        colorPickerLabelAutoCreatedStroke.setValue(getColorProperty(SettingsProperties.ANNOTATION_LABEL_BORDER_COLOR_AUTO));
        choiceBoxLabelAutoCreatedStrokeStyle.getItems().addAll("Solid", "Dashed", "Dotted");
        String strokeStyle = SettingsProperties.getProperty(SettingsProperties.ANNOTATION_LABEL_BORDER_STYLE_AUTO);
        choiceBoxLabelAutoCreatedStrokeStyle.setValue(strokeStyle.substring(0, 1).toUpperCase() + strokeStyle.substring(1));

        colorPickerAnchorFillDefault.setValue(getColorProperty(SettingsProperties.ANNOTATION_ANCHOR_FILL_COLOR_DEFAULT));
        colorPickerAnchorFillHoverDefault.setValue(getColorProperty(SettingsProperties.ANNOTATION_ANCHOR_FILL_COLOR_HOVER));
        colorPickerAnchorStrokeDefault.setValue(getColorProperty(SettingsProperties.ANNOTATION_ANCHOR_STROKE_COLOR_DEFAULT));
        colorPickerAnchorStrokeHoverDefault.setValue(getColorProperty(SettingsProperties.ANNOTATION_ANCHOR_STROKE_COLOR_HOVER));
        int defaultAnchorSize = Integer.parseInt(SettingsProperties.getProperty(SettingsProperties.ANNOTATION_ANCHOR_SIZE_DEFAULT));
        spinnerAnchorSizeDefault.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, defaultAnchorSize, 1));

        // init all color picker fields of this class
        Field[] fields = getClass().getDeclaredFields();
        for (Field f : fields) {
            if (f.getType() == ColorPicker.class) {
                try {
                    initColorPicker((ColorPicker) f.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Color getColorProperty(String property) {
        return Color.valueOf(SettingsProperties.getProperty(property));
    }

    private void initColorPicker(ColorPicker colorPicker) {
        colorPicker.backgroundProperty().bind(Bindings.createObjectBinding(() ->
                new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)), colorPicker.valueProperty()));
    }

    @Override
    public void onClickOk() {
        SettingsProperties.setProperty(SettingsProperties.ANNOTATION_FILL_COLOR_DEFAULT, colorPickerAnnotationFillDefault.getValue().toString());
        SettingsProperties.setProperty(SettingsProperties.ANNOTATION_FILL_COLOR_HOVER_DEFAULT, colorPickerAnnotationFillHoverDefault.getValue().toString());
        SettingsProperties.setProperty(SettingsProperties.ANNOTATION_STROKE_COLOR_DEFAULT, colorPickerAnnotationStrokeDefault.getValue().toString());
        SettingsProperties.setProperty(SettingsProperties.ANNOTATION_STROKE_WIDTH_DEFAULT, spinnerAnnotationStrokeWidth.getValue().toString());

        SettingsProperties.setProperty(SettingsProperties.ANNOTATION_FILL_COLOR_SELECTED, colorPickerAnnotationFillSelected.getValue().toString());
        SettingsProperties.setProperty(SettingsProperties.ANNOTATION_FILL_COLOR_HOVER_SELECTED, colorPickerAnnotationFillHoverSelected.getValue().toString());
        SettingsProperties.setProperty(SettingsProperties.ANNOTATION_STROKE_COLOR_SELECTED, colorPickerAnnotationStrokeSelected.getValue().toString());

        SettingsProperties.setProperty(SettingsProperties.ANNOTATION_FILL_COLOR_VERIFIED, colorPickerAnnotationFillVerified.getValue().toString());
        SettingsProperties.setProperty(SettingsProperties.ANNOTATION_FILL_COLOR_HOVER_VERIFIED, colorPickerAnnotationFillHoverVerified.getValue().toString());
        SettingsProperties.setProperty(SettingsProperties.ANNOTATION_STROKE_COLOR_VERIFIED, colorPickerAnnotationStrokeVerified.getValue().toString());

        SettingsProperties.setProperty(SettingsProperties.ANNOTATION_LABEL_VISIBLE, Boolean.toString(checkBoxLabelVisible.isSelected()));
        SettingsProperties.setProperty(SettingsProperties.ANNOTATION_LABEL_FILL_COLOR_DEFAULT, colorPickerLabelFillDefault.getValue().toString());
        SettingsProperties.setProperty(SettingsProperties.ANNOTATION_LABEL_FONT_COLOR_DEFAULT, colorPickerLabelFontDefault.getValue().toString());
        SettingsProperties.setProperty(SettingsProperties.ANNOTATION_LABEL_FILL_COLOR_VERIFIED, colorPickerLabelFillVerified.getValue().toString());
        SettingsProperties.setProperty(SettingsProperties.ANNOTATION_LABEL_FONT_COLOR_VERIFIED, colorPickerLabelFontVerified.getValue().toString());
        SettingsProperties.setProperty(SettingsProperties.ANNOTATION_LABEL_BORDER_COLOR_AUTO, colorPickerLabelAutoCreatedStroke.getValue().toString());
        SettingsProperties.setProperty(SettingsProperties.ANNOTATION_LABEL_BORDER_STYLE_AUTO, choiceBoxLabelAutoCreatedStrokeStyle.getValue().toLowerCase());

        SettingsProperties.setProperty(SettingsProperties.ANNOTATION_ANCHOR_FILL_COLOR_DEFAULT, colorPickerAnchorFillDefault.getValue().toString());
        SettingsProperties.setProperty(SettingsProperties.ANNOTATION_ANCHOR_FILL_COLOR_HOVER, colorPickerAnchorFillHoverDefault.getValue().toString());
        SettingsProperties.setProperty(SettingsProperties.ANNOTATION_ANCHOR_STROKE_COLOR_DEFAULT, colorPickerAnchorStrokeDefault.getValue().toString());
        SettingsProperties.setProperty(SettingsProperties.ANNOTATION_ANCHOR_STROKE_COLOR_HOVER, colorPickerAnchorStrokeHoverDefault.getValue().toString());
        SettingsProperties.setProperty(SettingsProperties.ANNOTATION_ANCHOR_SIZE_DEFAULT, spinnerAnchorSizeDefault.getValue().toString());
    }

    @Override
    public void onClickCancel() {
        // nothing
    }
}
