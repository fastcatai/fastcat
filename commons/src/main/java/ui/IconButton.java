package ui;

import javafx.scene.Node;
import javafx.scene.control.Button;

public class IconButton extends Button {

    public IconButton() {
        this(null);
    }

    public IconButton(Node graphic) {
        super(null, graphic);
        getStyleClass().clear();
    }
}
