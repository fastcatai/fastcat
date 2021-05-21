package ui;

import javafx.scene.Node;
import javafx.scene.control.ToggleButton;

public class IconToggleButton extends ToggleButton {

    public IconToggleButton() {
        this(null);
    }

    public IconToggleButton(Node graphic) {
        super(null, graphic);
        getStyleClass().clear();
    }
}
