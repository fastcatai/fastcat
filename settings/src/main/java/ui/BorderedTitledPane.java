package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class BorderedTitledPane extends StackPane {

    private final Label labelTitle;
    private Color borderColor;

    public BorderedTitledPane() {
        setBorderColor(Color.GRAY);
        setPadding(new Insets(20));

        labelTitle = new Label();
        labelTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        labelTitle.setPadding(new Insets(0, 2, 0, 2));
        setAlignment(labelTitle, Pos.TOP_LEFT);
        labelTitle.setTranslateX(-10);
        labelTitle.setTranslateY(-31);
        labelTitle.setStyle("-fx-background-color: -fx-background;");
        getChildren().add(labelTitle);
    }

    public String getTitle() {
        return labelTitle.getText();
    }

    public void setTitle(String title) {
        labelTitle.setText(title);
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color color) {
        borderColor = color;
        setBorder(new Border(new BorderStroke(borderColor, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    }
}
