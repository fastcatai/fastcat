package modelV3;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import util.TimeFormatter;

public class PointVideo extends Point implements AnnotationVideo {

    private final DoubleProperty timestampStartMillis;
    private final DoubleProperty timestampEndMillis;
    private final StringProperty shortTimestampString;

    public PointVideo(double x, double y, double radius, String label) {
        this(x, y, radius, label, false, false);
    }

    public PointVideo(double x, double y, double radius, String label, boolean verified, boolean autoCreated) {
        this(x, y, radius, label, "", verified, autoCreated);
    }

    public PointVideo(double x, double y, double radius, String label, String instance) {
        this(x, y, radius, label, instance, false, false);
    }

    public PointVideo(double x, double y, double radius, String label, String instance, boolean verified, boolean autoCreated) {
        super(x, y, radius, label, instance, verified, autoCreated);

        timestampStartMillis = new SimpleDoubleProperty();
        timestampEndMillis = new SimpleDoubleProperty();
        shortTimestampString = new SimpleStringProperty();

        timestampStartMillis.addListener((observable, oldStartMillis, newStartMillis) ->
                createShortTimestampString(newStartMillis.doubleValue(), timestampEndMillis.get()));
        timestampEndMillis.addListener((observable, oldEndMillis, newEndMillis) ->
                createShortTimestampString(timestampStartMillis.get(), newEndMillis.doubleValue()));
    }

    private void createShortTimestampString(double startTimestamp, double endTimestamp) {
        double start = 0;
        if (startTimestamp > 0)
            start = startTimestamp;
        String startStr = TimeFormatter.formatTimeMiddle(start);
        double delta = 0;
        if (endTimestamp > 0)
            delta = endTimestamp - startTimestamp;
        shortTimestampString.set(String.format("%s\n+ %s", startStr, TimeFormatter.formatTimeShort(delta)));
    }

    @Override
    public double getTimestampStartMillis() {
        return timestampStartMillis.get();
    }

    public void setTimestampStart(double millis) {
        timestampStartMillis.set(millis);
    }

    @Override
    public double getTimestampEndMillis() {
        return timestampEndMillis.get();
    }

    public void setTimestampEnd(double millis) {
        this.timestampEndMillis.set(millis);
    }

    public String getShortTimestampString() {
        return shortTimestampString.get();
    }

    public StringProperty shortTimestampStringProperty() {
        return shortTimestampString;
    }

    private BoundingBox inferredBox;

    public BoundingBox getInferredBox() {
        return inferredBox;
    }

    public void setInferredBox(BoundingBox inferredBox) {
        this.inferredBox = inferredBox;
    }
}
