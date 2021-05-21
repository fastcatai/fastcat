package annotation;

import controller.SettingsBaseController;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import settings.SettingsProperties;

import java.util.HashMap;
import java.util.Map;

public class AnnotationSettingsControllerV2 implements SettingsBaseController {
    private final Logger logger = LogManager.getLogger(AnnotationSettingsControllerV2.class);

    @FXML
    private VBox root;
    @FXML
    private Spinner<Integer> annotationStrokeWidthSelected;
    @FXML
    private Spinner<Integer> annotationStrokeWidthHoverSelected;
    @FXML
    private Spinner<Integer> annotationStrokeWidthVerified;
    @FXML
    private Spinner<Integer> annotationStrokeWidthHoverVerified;
    @FXML
    private Spinner<Integer> annotationStrokeWidthAuto;
    @FXML
    private Spinner<Integer> annotationStrokeWidthHoverAuto;
    @FXML
    private Spinner<Integer> annotationStrokeWidthDefault;
    @FXML
    private Spinner<Integer> annotationStrokeWidthHoverDefault;
    @FXML
    private ChoiceBox<String> annotationBorderStyleSelected;
    @FXML
    private ChoiceBox<String> annotationBorderStyleVerified;
    @FXML
    private ChoiceBox<String> annotationBorderStyleAuto;
    @FXML
    private ChoiceBox<String> annotationBorderStyleDefault;
    @FXML
    private Spinner<Integer> annotationAnchorSize;
    @FXML
    private Spinner<Integer> annotationAnchorSizeHover;
    @FXML
    public CheckBox annotationLabelVisible;

    private final Map<String, String> changes = new HashMap<>();

    @FXML
    private void initialize() {
        // init annotation stroke width spinners
        setSpinnerInitValue(annotationStrokeWidthSelected);
        setSpinnerInitValue(annotationStrokeWidthHoverSelected);
        setSpinnerInitValue(annotationStrokeWidthVerified);
        setSpinnerInitValue(annotationStrokeWidthHoverVerified);
        setSpinnerInitValue(annotationStrokeWidthAuto);
        setSpinnerInitValue(annotationStrokeWidthHoverAuto);
        setSpinnerInitValue(annotationStrokeWidthDefault);
        setSpinnerInitValue(annotationStrokeWidthHoverDefault);
        setSpinnerInitValue(annotationAnchorSize);
        setSpinnerInitValue(annotationAnchorSizeHover);

        // track annotation stroke width changes
        ChangeListener<Integer> spinnerChanged = (observable, oldValue, newValue) -> {
            String id = ((Spinner<?>) ((ObjectProperty<?>) observable).getBean()).getId();
            changes.put(id, newValue.toString());
        };
        annotationStrokeWidthSelected.valueProperty().addListener(spinnerChanged);
        annotationStrokeWidthHoverSelected.valueProperty().addListener(spinnerChanged);
        annotationStrokeWidthVerified.valueProperty().addListener(spinnerChanged);
        annotationStrokeWidthHoverVerified.valueProperty().addListener(spinnerChanged);
        annotationStrokeWidthAuto.valueProperty().addListener(spinnerChanged);
        annotationStrokeWidthHoverAuto.valueProperty().addListener(spinnerChanged);
        annotationStrokeWidthDefault.valueProperty().addListener(spinnerChanged);
        annotationStrokeWidthHoverDefault.valueProperty().addListener(spinnerChanged);
        annotationAnchorSize.valueProperty().addListener(spinnerChanged);
        annotationAnchorSizeHover.valueProperty().addListener(spinnerChanged);

        // init border style
        setBorderStyleInitValue(annotationBorderStyleSelected);
        setBorderStyleInitValue(annotationBorderStyleVerified);
        setBorderStyleInitValue(annotationBorderStyleAuto);
        setBorderStyleInitValue(annotationBorderStyleDefault);

        // track border style changes
        ChangeListener<String> borderStyleChanges = (observable, oldValue, newValue) -> {
            String id = ((ChoiceBox<?>) ((ObjectProperty<?>) observable).getBean()).getId();
            changes.put(id, newValue);
        };
        annotationBorderStyleSelected.valueProperty().addListener(borderStyleChanges);
        annotationBorderStyleVerified.valueProperty().addListener(borderStyleChanges);
        annotationBorderStyleAuto.valueProperty().addListener(borderStyleChanges);
        annotationBorderStyleDefault.valueProperty().addListener(borderStyleChanges);

        // init annotation label visibility
        annotationLabelVisible.setSelected(Boolean.parseBoolean(SettingsProperties.getProperty(SettingsProperties.ANNOTATION_LABEL_VISIBLE)));
        // track changes
        annotationLabelVisible.selectedProperty().addListener((observable, oldValue, newValue) ->
                changes.put(annotationLabelVisible.getId(), newValue.toString()));
    }

    private void setSpinnerInitValue(Spinner<Integer> spinner) {
        spinner.getValueFactory().setValue(Integer.valueOf(SettingsProperties.getProperty(spinner.getId())));
    }

    private void setBorderStyleInitValue(ChoiceBox<String> comboBox) {
        comboBox.setValue(SettingsProperties.getProperty(comboBox.getId()));
    }

    /**
     * Saves every change that the user makes.
     *
     * @param event action event
     */
    @FXML
    private void onColorPicked(ActionEvent event) {
        Object source = event.getSource();
        String id, value;
        if (source instanceof FlatColorPicker) {
            FlatColorPicker cp = (FlatColorPicker) source;
            id = cp.getPropertyKey();
            value = cp.getValue().toString();
        } else throw logger.throwing(new IllegalCallerException("Event of unknown type"));
        changes.put(id, value);
    }

    @Override
    public void onClickOk() {
        for (String propId : changes.keySet()) {
            SettingsProperties.setProperty(propId, changes.get(propId));
            UIAttributesSettings.setUIAttribute(propId, changes.get(propId));
        }
    }

    @Override
    public void onClickCancel() {
        // nothing
    }
}
