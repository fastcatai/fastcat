package boundingbox;

import annotation.UIAnnotation;
import javafx.scene.layout.Pane;
import modelV3.BoundingBox;

public class SimpleBoundingBoxV3 extends UIBoundingBox<BoundingBox> {

    private final Pane pane;
    private final BoundingBox boundingBox;

    public SimpleBoundingBoxV3(Pane pane, BoundingBox boundingBox, int originalImageWidth, int originalImageHeight) {
        this(pane, boundingBox, false, originalImageWidth, originalImageHeight);
    }

    public SimpleBoundingBoxV3(Pane pane, BoundingBox boundingBox, boolean absolute, int originalImageWidth, int originalImageHeight) {
        super(pane, boundingBox, absolute, originalImageWidth, originalImageHeight);
        this.pane = pane;
        this.boundingBox = boundingBox;
    }

    @Override
    public UIAnnotation<?> copy(int originalImageWidth, int originalImageHeight) {
        BoundingBox bb = new BoundingBox(
                boundingBox.getX(), boundingBox.getY(), boundingBox.getWidth(), boundingBox.getHeight(),
                boundingBox.getLabel(), boundingBox.getInstance(),
                boundingBox.isVerified(), boundingBox.isAutoCreated()
        );
        for (String l : boundingBox.getAdditionalLabels())
            bb.addAdditionalLabel(l);
        SimpleBoundingBoxV3 uiBox = new SimpleBoundingBoxV3(pane, bb, true, originalImageWidth, originalImageHeight);
        uiBox.imported();
        return uiBox;
    }
}
