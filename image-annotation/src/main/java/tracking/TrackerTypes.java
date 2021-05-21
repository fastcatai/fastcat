package tracking;

import org.opencv.tracking.Tracker;
import org.opencv.tracking.TrackerBoosting;
import org.opencv.tracking.TrackerCSRT;
import org.opencv.tracking.TrackerKCF;
import org.opencv.tracking.TrackerMIL;
import org.opencv.tracking.TrackerMOSSE;
import org.opencv.tracking.TrackerMedianFlow;
import org.opencv.tracking.TrackerTLD;

public enum TrackerTypes {
    CSRT("CSRT", TrackerCSRT.class),
    MIL("MIL", TrackerMIL.class),
    BOOSTING("Boosting", TrackerBoosting.class),
    KCF("KCF", TrackerKCF.class),
    MEDIAN_FLOW("MedianFlow", TrackerMedianFlow.class),
    MOSSE("MOSSE", TrackerMOSSE.class),
    TLD("TLD", TrackerTLD.class);

    private final String label;
    private final Class<? extends Tracker> trackerClass;

    TrackerTypes(String label, Class<? extends Tracker> trackerClass) {
        this.label = label;
        this.trackerClass = trackerClass;
    }

    public Class<? extends Tracker> getTrackerClass() {
        return trackerClass;
    }

    @Override
    public String toString() {
        return label;
    }
}
