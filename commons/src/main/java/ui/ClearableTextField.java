package ui;

import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class ClearableTextField extends TextField {

    public ClearableTextField() {
        getStylesheets().add(ClearableTextField.class.getResource("/css/ClearableTextField.css").toExternalForm());
        getStyleClass().add("clearable-field");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ClearableTextFieldSkin(this);
    }

    private static class ClearableTextFieldSkin extends TextFieldSkin {

        private final StackPane clearButtonPane;

        public ClearableTextFieldSkin(TextField control) {
            super(control);
            Region clearButton = new Region();
            clearButton.getStyleClass().add("graphic");
            clearButtonPane = new StackPane(clearButton);
            clearButtonPane.setManaged(false);
            clearButtonPane.getStyleClass().add("clear-button");
            clearButtonPane.setOnMouseClicked(event -> control.clear());
            getChildren().add(clearButtonPane);
        }

        @Override
        protected void layoutChildren(double x, double y, double w, double h) {
            final double fullHeight = h + snappedTopInset() + snappedBottomInset();
            final double clearButtonWidth = clearButtonPane == null ? 0.0 : snapSizeX(clearButtonPane.prefWidth(fullHeight));

            final double textFieldStartX = snapPositionX(x);
            final double textFieldWidth = w - snapSizeX(x) - snapSizeX(clearButtonWidth);

            super.layoutChildren(textFieldStartX, 0, textFieldWidth, fullHeight);

            if (clearButtonPane != null) {
                final double startButtonX = w - clearButtonWidth + snappedLeftInset();
                clearButtonPane.resizeRelocate(startButtonX, 0, clearButtonWidth, fullHeight);
            }
        }

        @Override
        protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
            final double pw = super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
            final double clearButtonWidth = clearButtonPane == null ? 0.0 : snapSizeX(clearButtonPane.prefWidth(height));
            return pw + clearButtonWidth;
        }

        @Override
        protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
            final double ph = super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
            final double clearButtonHeight = clearButtonPane == null ? 0.0 : snapSizeY(clearButtonPane.prefHeight(-1));
            return Math.max(ph, clearButtonHeight);
        }

        @Override
        protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
            final double mw = super.computeMinWidth(height, topInset, rightInset, bottomInset, leftInset);
            final double clearButtonWidth = clearButtonPane == null ? 0.0 : snapSizeX(clearButtonPane.minWidth(height));
            return mw + clearButtonWidth;
        }

        @Override
        protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
            final double mh = super.computeMinHeight(width, topInset, rightInset, bottomInset, leftInset);
            final double clearButtonHeight = clearButtonPane == null ? 0.0 : snapSizeY(clearButtonPane.minHeight(-1));
            return Math.max(mh, clearButtonHeight);
        }
    }
}
