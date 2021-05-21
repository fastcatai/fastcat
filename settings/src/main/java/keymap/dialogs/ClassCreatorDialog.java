package keymap.dialogs;

import i18n.I18N;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import keymap.model.Action;
import keymap.model.KeyAction;
import keymap.model.KeyActionClass;
import org.kordamp.ikonli.javafx.FontIcon;
import settings.KeyEventTransform;
import ui.Dialogs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

public class ClassCreatorDialog {

    @FXML
    public Scene scene;
    @FXML
    private VBox vBoxRoot;
    @FXML
    private TextField textFieldClass;
    @FXML
    private TextField textFieldKeyComboString;
    @FXML
    public CheckBox checkBoxInvert;
    @FXML
    public Label labelKCInversion;
    @FXML
    private Button buttonOk;
    @FXML
    private Button buttonCancel;

    private final Stage stage;
    private final Map<Action, KeyAction> keyActions;
    private final Map<String, KeyActionClass> classKeyActions;

    private final ObjectProperty<KeyCombination> classKC;
    private final ObjectProperty<KeyCombination> inversionKC;

    private final HBox infoBox;
    private final HBox errorBox;

    public ClassCreatorDialog(final Stage stage, final Map<Action, KeyAction> keyActions, final Map<String, KeyActionClass> classKeyActions) {
        this.stage = stage;
        this.keyActions = keyActions;
        this.classKeyActions = classKeyActions;

        classKC = new SimpleObjectProperty<>();
        inversionKC = new SimpleObjectProperty<>();

        infoBox = createInfoBox();
        errorBox = createErrorBox();
    }

    @FXML
    private void initialize() {

        // bind class combo property to text field
        classKC.bind(Bindings.createObjectBinding(() -> {
            String keyCombo = textFieldKeyComboString.getText();
            if (keyCombo.isEmpty()) return null;
            try {
                return KeyCodeCombination.valueOf(keyCombo);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }, textFieldKeyComboString.textProperty()));

        // bind inversion combo property to text field
        inversionKC.bind(Bindings.createObjectBinding(() -> {
            if (!checkBoxInvert.isSelected()) return null;

            String keyCombo = textFieldKeyComboString.getText();
            if (keyCombo.isEmpty()) return null;

            try {
                // create new key combination and turn on Ctrl modifier
                KeyCodeCombination kcc = (KeyCodeCombination) KeyCodeCombination.valueOf(keyCombo);
                return new KeyCodeCombination(kcc.getCode(), kcc.getShift(), KeyCombination.ModifierValue.DOWN,
                        kcc.getAlt(), kcc.getMeta(), kcc.getShortcut());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }, textFieldKeyComboString.textProperty(), checkBoxInvert.selectedProperty()));

        // display key combo
        labelKCInversion.textProperty().bind(Bindings.createStringBinding(() -> {
            if (inversionKC.get() == null) return "";
            return inversionKC.get().getName();
        }, inversionKC));

        // forward event to text field
        scene.setOnKeyPressed(event -> {
            if (!textFieldKeyComboString.isFocused()) {
                event.consume();
                textFieldKeyComboString.fireEvent(event);
            }
        });

        // key pressed event
        textFieldKeyComboString.setOnKeyPressed(this::onKeyPressedForTextFieldKeyComboString);

        buttonOk.disableProperty().bind(textFieldClass.textProperty().isEmpty()
                .or(errorBox.visibleProperty()));
        buttonOk.setOnAction(event -> stage.close());

        buttonCancel.setOnAction(event -> {
            textFieldKeyComboString.clear();
            stage.close();
        });
    }

    private void onKeyPressedForTextFieldKeyComboString(final KeyEvent event) {
        event.consume();
        KeyEventTransform.toStringWithoutControl(event).ifPresent(s -> {
            textFieldKeyComboString.setText(s);
            if (event.getCode().isModifierKey())
                return;

            boolean collision = false;
            boolean classCollision = false;

            if (classKC.get() != null) {
                collision = keyActions.values().stream().map(KeyAction::getKeyCombination).anyMatch(kc ->
                        kc != null && kc.equals(classKC.get()));

                classCollision = classKeyActions.values().stream().anyMatch(kac -> {
                    boolean exists = false;
                    if (kac.getKeyCombination() != null)
                        exists = kac.getKeyCombination().equals(classKC.get());
                    if (kac.getInversionKeyCombination().isPresent())
                        exists |= kac.getInversionKeyCombination().get().equals(classKC.get());
                    return exists;
                });
            }

            if (inversionKC.get() != null) {
                collision |= keyActions.values().stream().map(KeyAction::getKeyCombination).anyMatch(kc ->
                        kc != null && kc.equals(inversionKC.get()));

                classCollision |= classKeyActions.values().stream().anyMatch(kac -> {
                    boolean exists = false;
                    if (kac.getKeyCombination() != null)
                        exists = kac.getKeyCombination().equals(inversionKC.get());
                    if (kac.getInversionKeyCombination().isPresent())
                        exists |= kac.getInversionKeyCombination().get().equals(inversionKC.get());
                    return exists;
                });
            }

            showError(collision || classCollision);
        });
    }

    @FXML
    public void showHideInfo(ActionEvent event) {
        Hyperlink infoLink = ((Hyperlink) event.getSource());
        int index = vBoxRoot.getChildren().contains(errorBox) ? 4 : 3;

        if (vBoxRoot.getChildren().contains(infoBox)) {
            vBoxRoot.getChildren().remove(infoBox);
            infoLink.setText("Show Info");
        } else {
            vBoxRoot.getChildren().add(index, infoBox);
            infoLink.setText("Hide Info");
        }
        stage.sizeToScene();
    }

    private void showError(boolean show) {
        if (show && !vBoxRoot.getChildren().contains(errorBox))
            vBoxRoot.getChildren().add(3, errorBox);
        if (!show) vBoxRoot.getChildren().remove(errorBox);
        errorBox.setVisible(show);
        stage.sizeToScene();
    }

    private HBox createInfoBox() {
        Label info = new Label();
        info.getStyleClass().add("class-creator-info");
        info.setText(I18N.getString("settings.classCreatorInfoText"));
        HBox.setHgrow(info, Priority.ALWAYS);
        return new HBox(info);
    }

    private HBox createErrorBox() {
        HBox errorBox = new HBox(new FontIcon(), new Label(I18N.getString("settings.shortcutExistsWarning")));
        errorBox.getStyleClass().add("shortcut-error-box");
        return errorBox;
    }

    public Optional<CreatedClass> getClassKeyCombo() {
        if (textFieldClass.getText().strip().isBlank() || classKC.get() == null)
            return Optional.empty();
        return Optional.of(new CreatedClass(textFieldClass.getText().strip(), classKC.get(), inversionKC.get()));
    }

    public static Optional<CreatedClass> showAndWait(final Map<Action, KeyAction> keyActions, final Map<String, KeyActionClass> classKeyActions) {
        Stage stage = new Stage();
        stage.setTitle("Class Creator");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        InputStream iconIS = Dialogs.class.getClassLoader().getResourceAsStream("logo_polygon.png");
        if (iconIS != null) stage.getIcons().add(new Image(iconIS));

        FXMLLoader loader = new FXMLLoader(ClassCreatorDialog.class.getResource("/fxml/keymapv2/ClassCreator.fxml"));
        loader.setControllerFactory(param -> new ClassCreatorDialog(stage, keyActions, classKeyActions));

        try {
            stage.setScene(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }

        stage.sizeToScene();
        stage.centerOnScreen();
        stage.showAndWait();

        return ((ClassCreatorDialog) loader.getController()).getClassKeyCombo();
    }

    public static class CreatedClass {
        public final String className;
        public final KeyCombination shortcut;
        public final KeyCombination inversionShortcut;

        public CreatedClass(final String className, final KeyCombination shortcut, final KeyCombination inversionShortcut) {
            this.className = className;
            this.shortcut = shortcut;
            this.inversionShortcut = inversionShortcut;
        }
    }
}
