package ui;

import javafx.scene.Node;
import javafx.scene.control.RadioButton;

public class IconRadioButton extends RadioButton {

    public IconRadioButton() {
        this(null);
    }

    public IconRadioButton(Node graphic) {
        super(null);
        getStyleClass().clear();
        setGraphic(graphic);
    }
}
