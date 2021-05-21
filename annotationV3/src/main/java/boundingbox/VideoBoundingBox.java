package boundingbox;

import annotation.UIAnnotation;
import javafx.scene.layout.Pane;
import modelV3.BoundingBoxVideo;

public class VideoBoundingBox extends UIBoundingBox<BoundingBoxVideo> {

    private final Pane pane;
    private final BoundingBoxVideo boundingBox;

    public VideoBoundingBox(Pane pane, BoundingBoxVideo boundingBox, int originalImageWidth, int originalImageHeight) {
        super(pane, boundingBox, originalImageWidth, originalImageHeight);
        this.pane = pane;
        this.boundingBox = boundingBox;
    }

    @Override
    public UIAnnotation<?> copy(int originalImageWidth, int originalImageHeight) {
        BoundingBoxVideo bb = new BoundingBoxVideo(
                boundingBox.getX(), boundingBox.getY(), boundingBox.getWidth(), boundingBox.getHeight(),
                boundingBox.getLabel(), boundingBox.getInstance(),
                boundingBox.isVerified(), boundingBox.isAutoCreated()
        );
        for (String l : boundingBox.getAdditionalLabels())
            bb.addAdditionalLabel(l);
        bb.setTimestampStart(boundingBox.getTimestampStartMillis());
        bb.setTimestampEnd(boundingBox.getTimestampEndMillis());
        return new VideoBoundingBox(pane, bb, originalImageWidth, originalImageHeight);
    }
}
