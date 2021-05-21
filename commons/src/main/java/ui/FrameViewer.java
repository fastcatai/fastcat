package ui;

import javafx.scene.image.ImageView;

public class FrameViewer extends ImageView {

    public FrameViewer() {
        setPreserveRatio(true);
    }

    //<editor-fold desc="Method from Node/ImageView">
    @Override
    public double prefWidth(double height) {
        if (getImage() == null)
            return minWidth(height);
        return getImage().getWidth();
    }

    @Override
    public double prefHeight(double width) {
        if (getImage() == null)
            return minHeight(width);
        return getImage().getHeight();
    }

    @Override
    public double minWidth(double height) {
        return 100;
    }

    @Override
    public double minHeight(double width) {
        return 100;
    }

    @Override
    public double maxWidth(double height) {
//        return 16384; // maximum texture size?
        return prefWidth(height);
    }

    @Override
    public double maxHeight(double width) {
//        return 16384; // maximum texture size?
        return prefHeight(width);
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public void resize(double width, double height) {
        setFitWidth(width);
        setFitHeight(height);
    }
    //</editor-fold>
}
