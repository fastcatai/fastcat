package keymap.dialogs;

import i18n.I18N;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import keymap.model.Action;
import keymap.model.KeyAction;
import org.kordamp.ikonli.javafx.FontIcon;
import settings.KeyEventTransform;
import ui.Dialogs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

public class ShortcutModifierDialog {

    @FXML
    public VBox vBoxRoot;
    @FXML
    private Scene scene;
    @FXML
    private Label labelActionText;
    @FXML
    private TextField textFieldKeyComboString;
    @FXML
    private HBox hBoxError;
    @FXML
    private Button buttonOk;
    @FXML
    private Button buttonCancel;

    private final Stage stage;
    private final String actionText;
    private final Map<Action, KeyAction> keyActions;
    private final KeyCombination keyCombination;

    private final HBox errorBox;

    public ShortcutModifierDialog(final Stage stage, final String actionText, final Map<Action, KeyAction> keyActions) {
        this(stage, actionText, keyActions, null);
    }

    public ShortcutModifierDialog(final Stage stage, final String actionText, final Map<Action, KeyAction> keyActions,
                                  final KeyCombination keyCombination) {
        this.stage = stage;
        this.actionText = actionText;
        this.keyActions = keyActions;
        this.keyCombination = keyCombination;

        errorBox = createErrorBox();
    }

    @FXML
    private void initialize() {
        labelActionText.setText(actionText);

        scene.setOnKeyPressed(event -> {
            if (!textFieldKeyComboString.isFocused()) {
                event.consume();
                textFieldKeyComboString.fireEvent(event);
            }
        });

        textFieldKeyComboString.setText(keyCombination != null ? keyCombination.getName() : "");
        textFieldKeyComboString.setOnKeyPressed(event -> {
            event.consume();
            KeyEventTransform.toString(event).ifPresent(s -> {
                textFieldKeyComboString.setText(s);
                KeyEventTransform.createCombination(event).ifPresent(pressedKC -> {
                    boolean collision = keyActions.values().stream().map(KeyAction::getKeyCombination).anyMatch(kc -> {
                        if (kc != null) return kc.equals(pressedKC);
                        return false;
                    });
                    showError(collision);
                });
            });
        });

        buttonOk.setOnAction(event -> stage.close());
        buttonCancel.setOnAction(event -> {
            textFieldKeyComboString.clear();
            stage.close();
        });
    }

    private HBox createErrorBox() {
        HBox errorBox = new HBox(new FontIcon(), new Label(I18N.getString("settings.shortcutExistsWarning")));
        errorBox.getStyleClass().add("shortcut-error-box");
        errorBox.setVisible(false);
        return errorBox;
    }

    private void showError(boolean show) {
        if (show && !vBoxRoot.getChildren().contains(errorBox))
            vBoxRoot.getChildren().add(2, errorBox);
        if (!show) vBoxRoot.getChildren().remove(errorBox);
        errorBox.setVisible(show);
        stage.sizeToScene();
    }

    public Optional<String> getKeyCombo() {
        String keyCombo = textFieldKeyComboString.getText().trim();
        if (keyCombo.isEmpty() || (keyCombination != null && keyCombo.equals(keyCombination.toString())))
            return Optional.empty();
        return Optional.of(keyCombo);
    }

    public static Optional<String> showAndWait(final String actionText, final Map<Action, KeyAction> keyActions) {
        return showAndWait(actionText, keyActions, null);
    }

    public static Optional<String> showAndWait(final String actionText, final Map<Action, KeyAction> keyActions,
                                               final KeyCombination keyCombination) {
        Stage stage = new Stage();
        stage.setTitle(I18N.getString("settings.shortcutEditorTitle"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        InputStream iconIS = Dialogs.class.getClassLoader().getResourceAsStream("logo_polygon.png");
        if (iconIS != null) stage.getIcons().add(new Image(iconIS));

        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(ShortcutModifierDialog.class.getResource("/fxml/keymapv2/ShortcutModifier.fxml"));
            loader.setControllerFactory(param -> new ShortcutModifierDialog(stage, actionText, keyActions, keyCombination));
            stage.setScene(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }

        stage.sizeToScene();
        stage.centerOnScreen();
        stage.showAndWait();

        return ((ShortcutModifierDialog) loader.getController()).getKeyCombo();
    }
}
