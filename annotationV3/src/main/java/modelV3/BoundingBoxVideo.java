package modelV3;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import util.TimeFormatter;

public class BoundingBoxVideo extends BoundingBox implements AnnotationVideo{

    private final DoubleProperty timestampStartMillis = new SimpleDoubleProperty();
    private final DoubleProperty timestampEndMillis = new SimpleDoubleProperty();
    private final StringProperty shortTimestampString = new SimpleStringProperty();

    public BoundingBoxVideo(double x, double y, double width, double height) {
        this(x, y, width, height, "");
    }

    public BoundingBoxVideo(double x, double y, double width, double height, String label) {
        this(x, y, width, height, label, "");
    }

    public BoundingBoxVideo(double x, double y, double width, double height, String label, boolean verified, boolean autoCreated) {
        this(x, y, width, height, label, "", verified, autoCreated);
    }

    public BoundingBoxVideo(double x, double y, double width, double height, String label, String instance) {
        this(x, y, width, height, label, instance, false, false);
    }

    public BoundingBoxVideo(double x, double y, double width, double height, String label, String instance, boolean verified, boolean autoCreated) {
        super(x, y, width, height, label, instance, verified, autoCreated);

        ChangeListener<Number> timestampChanged = (observable, oldValue, newValue) -> {
            double start = timestampStartMillis == observable ? newValue.doubleValue() : timestampStartMillis.doubleValue();
            double end = timestampEndMillis == observable ? newValue.doubleValue() : timestampEndMillis.doubleValue();
            String startStr = TimeFormatter.formatTimeMiddle(start > 0 ? start : 0);
            String deltaStr = TimeFormatter.formatTimeShort(end > 0 ? end - start : 0);
            shortTimestampString.set(String.format("%s\n+ %s", startStr, deltaStr));
        };

        timestampStartMillis.addListener(timestampChanged);
        timestampEndMillis.addListener(timestampChanged);
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
}
