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
import java.util.Objects;
import java.util.Optional;

public class ClassShortcutModifierDialog {

    @FXML
    public Scene scene;
    @FXML
    private VBox vBoxRoot;
    @FXML
    private Label labelClassName;
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
    private final String className;

    private final Map<Action, KeyAction> keyActions;
    private final Map<String, KeyActionClass> classKeyActions;

    private final KeyCombination previousClassKC;
    private final KeyCombination previousInversionKC;
    private final ObjectProperty<KeyCombination> classKC;
    private final ObjectProperty<KeyCombination> inversionKC;

    private boolean wasCanceled = false;

    private final HBox infoBox;
    private final HBox errorBox;

    public ClassShortcutModifierDialog(final Stage stage, final String className,
                                       final Map<Action, KeyAction> keyActions, final Map<String, KeyActionClass> classKeyActions) {
        this(stage, className, null, null, keyActions, classKeyActions);
    }

    public ClassShortcutModifierDialog(final Stage stage, final String className,
                                       final KeyCodeCombination classKC, final KeyCodeCombination inversionKC,
                                       final Map<Action, KeyAction> keyActions, final Map<String, KeyActionClass> classKeyActions) {
        this.stage = stage;
        this.className = className;
        this.previousClassKC = classKC;
        this.previousInversionKC = inversionKC;
        this.classKC = new SimpleObjectProperty<>(previousClassKC);
        this.inversionKC = new SimpleObjectProperty<>(previousInversionKC);
        this.keyActions = keyActions;
        this.classKeyActions = classKeyActions;
        infoBox = createInfoBox();
        errorBox = createErrorBox();
    }

    @FXML
    private void initialize() {
        labelClassName.setText(className);

        textFieldKeyComboString.setText(previousClassKC != null ? previousClassKC.getName() : "");
        if (previousInversionKC != null) {
            checkBoxInvert.setSelected(true);
            labelKCInversion.setText(previousInversionKC.getName());
        } else checkBoxInvert.setSelected(false);

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

        scene.setOnKeyPressed(event -> {
            if (!textFieldKeyComboString.isFocused()) {
                event.consume();
                textFieldKeyComboString.fireEvent(event);
            }
        });

        textFieldKeyComboString.setOnKeyPressed(this::onKeyPressedForTextFieldKeyComboString);

        buttonOk.disableProperty().bind(errorBox.visibleProperty()
                .or(classKC.isNull())
                .or(Bindings.or(checkBoxInvert.selectedProperty().not().and(inversionKC.isNotNull()),
                        checkBoxInvert.selectedProperty().and(inversionKC.isNull()))));
        buttonOk.setOnAction(event -> stage.close());
        buttonCancel.setOnAction(event -> {
            wasCanceled = true;
            stage.close();
        });
    }

    private void onKeyPressedForTextFieldKeyComboString(KeyEvent event) {
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
        errorBox.setVisible(false);
        return errorBox;
    }

    public Optional<ClassShortcut> getClassKeyCombo() {
        boolean wasChanged = !Objects.equals(classKC.get(), previousClassKC) || !Objects.equals(inversionKC.get(), previousInversionKC);
        if (wasCanceled || !wasChanged)
            return Optional.empty();
        return Optional.of(new ClassShortcut(classKC.get(), inversionKC.get()));
    }

    public static Optional<ClassShortcut> showAndWait(final String className, final KeyCodeCombination classKC,
                                                      final Map<Action, KeyAction> keyActions, final Map<String, KeyActionClass> classKeyActions) {
        return showAndWait(className, classKC, null, keyActions, classKeyActions);
    }

    public static Optional<ClassShortcut> showAndWait(final String className,
                                                      final KeyCodeCombination classKC, final KeyCodeCombination inversionKC,
                                                      final Map<Action, KeyAction> keyActions, final Map<String, KeyActionClass> classKeyActions) {
        Stage stage = new Stage();
        stage.setTitle("Class Shortcut Modifier");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        InputStream iconIS = Dialogs.class.getClassLoader().getResourceAsStream("logo_polygon.png");
        if (iconIS != null) stage.getIcons().add(new Image(iconIS));

        FXMLLoader loader = new FXMLLoader(ClassShortcutModifierDialog.class.getResource("/fxml/keymapv2/ClassShortcutModifier.fxml"));
        loader.setControllerFactory(param -> new ClassShortcutModifierDialog(stage, className, classKC, inversionKC, keyActions, classKeyActions));

        try {
            stage.setScene(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }

        stage.sizeToScene();
        stage.centerOnScreen();
        stage.showAndWait();

        return ((ClassShortcutModifierDialog) loader.getController()).getClassKeyCombo();
    }

    public static class ClassShortcut {
        public final KeyCombination shortcut;
        public final KeyCombination inversionShortcut;

        public ClassShortcut(final KeyCombination shortcut, final KeyCombination inversionShortcut) {
            this.shortcut = shortcut;
            this.inversionShortcut = inversionShortcut;
        }
    }
}
