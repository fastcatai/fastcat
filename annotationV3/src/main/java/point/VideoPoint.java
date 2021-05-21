package point;

import annotation.UIAnnotation;
import javafx.scene.layout.Pane;
import modelV3.PointVideo;

public class VideoPoint extends UIPoint<PointVideo> {
    private final Pane pane;
    private final PointVideo point;

    public VideoPoint(Pane pane, PointVideo point, int originalImageWidth, int originalImageHeight) {
        super(pane, point, originalImageWidth, originalImageHeight);
        this.pane = pane;
        this.point = point;
    }

    @Override
    public UIAnnotation<?> copy(int originalImageWidth, int originalImageHeight) {
        PointVideo pv = new PointVideo(point.getX(), point.getY(), point.getRadius(),
                point.getLabel(), point.getInstance(), point.isVerified(), point.isAutoCreated());
        for (String l : point.getAdditionalLabels())
            pv.addAdditionalLabel(l);
        pv.setTimestampStart(point.getTimestampStartMillis());
        pv.setTimestampEnd(point.getTimestampEndMillis());
        return new VideoPoint(pane, pv, originalImageWidth, originalImageHeight);
    }
}
