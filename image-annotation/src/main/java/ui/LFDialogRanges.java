package ui;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import controller.ImageAnnotationController;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import json.VRJsonModule;
import model.FrameData;
import model.SuperframeData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wrapper.VRJsonWrapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LFDialogRanges {
    private static final Logger logger = LogManager.getLogger(LFDialogRanges.class);

    @FXML
    private HBox hBoxVRRanges;
    @FXML
    private TableView<String[]> tableViewVRRanges;
    @FXML
    private TableColumn<String[], String> tableColumnClass;
    @FXML
    private TableColumn<String[], String> tableColumnStart;
    @FXML
    private TableColumn<String[], String> tableColumnEnd;

    private final ImageAnnotationController iaController;

    public LFDialogRanges(ImageAnnotationController iaController) {
        this.iaController = iaController;
    }

    @FXML
    private void initialize() {
        tableColumnClass.setReorderable(false);
        tableColumnStart.setReorderable(false);
        tableColumnEnd.setReorderable(false);

        tableColumnClass.setStyle("-fx-alignment: center;");
        tableColumnStart.setStyle("-fx-alignment: center;");
        tableColumnEnd.setStyle("-fx-alignment: center;");

        tableColumnClass.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        tableColumnStart.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        tableColumnEnd.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));
    }

    private final ReadOnlyObjectWrapper<VRJsonWrapper> vrJsonWrapper = new ReadOnlyObjectWrapper<>();

    public VRJsonWrapper getVrJsonWrapper() {
        return vrJsonWrapper.get();
    }

    public ReadOnlyObjectProperty<VRJsonWrapper> vrJsonWrapperProperty() {
        return vrJsonWrapper.getReadOnlyProperty();
    }

    public void loadVRJson(Path vrJsonFile) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new VRJsonModule());
        InjectableValues.Std injectableValues = new InjectableValues.Std();
        injectableValues.addValue("annotationPane", iaController.getAnnotationPane());
        injectableValues.addValue("jsonFilePath", vrJsonFile);
        mapper.setInjectableValues(injectableValues);
        try {
            VRJsonWrapper wrapper = mapper.readValue(vrJsonFile.toFile(), VRJsonWrapper.class);
            vrJsonWrapper.set(wrapper);
        } catch (IOException e) {
            logger.throwing(e);
        }
    }

    private List<SuperframeData> readSuperframes(Path vrJsonFile) {
        if (vrJsonWrapper.get() == null)
            loadVRJson(vrJsonFile);
        return vrJsonWrapper.get().getSuperframes();
    }

    /**
     * Get all between #START and #END
     * 1) Load all starts and ends independently
     * 2) Sort by frame number in ascending order
     * 3) Validate ranges
     * 3) Dump all frame numbers between those ranges into a set, so that overlaps are not loaded multiple times
     *
     * @param vrJsonFile Video Review JSON file
     */
    public Set<Integer> loadVRRanges(Path vrJsonFile) {
        List<SuperframeData> superframes = readSuperframes(vrJsonFile);
        if (superframes == null || superframes.isEmpty())
            return null;

        final String START_TAG = "#START";
        final String END_TAG = "#END";

        // load ranges
//        List<SuperframeData> superframes = loader.loadSuperframes();
        Map<Integer, List<String>> startLabels = new HashMap<>();
        Map<Integer, List<String>> endLabels = new HashMap<>();
        for (SuperframeData superframe : superframes) {
            for (FrameData child : superframe.getChildFrames()) {
                for (String clazz : child.getClasses()) {
                    if (clazz.endsWith(START_TAG)) {
                        int frameNumber = child.getFrameNumber();
                        String className = clazz.substring(0, clazz.length() - START_TAG.length());
                        List<String> labels = new ArrayList<>();
                        if (startLabels.containsKey(frameNumber))
                            labels = startLabels.get(frameNumber);
                        labels.add(className);
                        startLabels.put(frameNumber, labels);
                    } else if (clazz.endsWith(END_TAG)) {
                        int frameNumber = child.getFrameNumber();
                        String className = clazz.substring(0, clazz.length() - END_TAG.length());
                        List<String> labels = new ArrayList<>();
                        if (startLabels.containsKey(frameNumber))
                            labels = startLabels.get(frameNumber);
                        labels.add(className);
                        endLabels.put(frameNumber, labels);
                    }
                }
            }
        }

        // sort by frame number
        List<Integer> sortedStartFrames = new ArrayList<>(startLabels.keySet());
        List<Integer> sortedEndFrames = new ArrayList<>(endLabels.keySet());
        sortedStartFrames.sort(Integer::compareTo);
        sortedEndFrames.sort(Integer::compareTo);

        // stop here if there one list is empty
        if (sortedStartFrames.isEmpty() || sortedEndFrames.isEmpty())
            return Collections.emptySet();

        // check
        List<String[]> ranges = new ArrayList<>();
        Map<String, Integer> lockedStartLabels = new HashMap<>();
        int startNumber = sortedStartFrames.get(0);
        int endNumber = sortedEndFrames.get(sortedEndFrames.size() - 1);

        for (int i = startNumber; i <= endNumber; i++) {
            // If there is an end tag, then remove the lock state of this label.
            // When there is not corresponding locked label, this means that there is a missing start tag and insert
            // it into ranges with -1 for start.
            if (endLabels.containsKey(i)) {
                List<String> endClasses = endLabels.get(i);
                // for every label in the end frame
                for (String clazz : endClasses) {
                    if (lockedStartLabels.containsKey(clazz)) { // label has a start frame
                        Integer startFrame = lockedStartLabels.remove(clazz);
                        ranges.add(new String[]{clazz, String.valueOf(startFrame), String.valueOf(i)});
                    } else { // label does not have a start frame
                        ranges.add(new String[]{clazz, "-1", String.valueOf(i)});
                    }
                }
            }
            // If there is an start tag, then add all label to the lock state.
            // When there is already the same label locked, this means that there is a missing end tag and put the
            // new start tag with its frame number out of lock state (write to ranges) and us the most recent label.
            if (startLabels.containsKey(i)) {
                List<String> startClasses = startLabels.get(i);
                // for every label in start frames
                for (String clazz : startClasses) {
                    if (lockedStartLabels.containsKey(clazz)) { // label is already locked
                        int startFrame = lockedStartLabels.remove(clazz);
                        ranges.add(new String[]{clazz, String.valueOf(startFrame), "-1"});
                        lockedStartLabels.put(clazz, i); // add label with frame number as new locked state
                    } else { // label is not locked
                        lockedStartLabels.put(clazz, i);
                    }
                }
            }
        }

        // dump remaining start tags that are still locked
        if (!lockedStartLabels.isEmpty()) {
            for (String clazz : lockedStartLabels.keySet()) {
                Integer startFrame = lockedStartLabels.get(clazz);
                ranges.add(new String[]{clazz, String.valueOf(startFrame), "-1"});
            }
        }

        // First sort ranges by starting frame
        // If starting frame is equals then sort by end frame
        // if end frame is equals then sort by class label
        ranges.sort((o1, o2) -> {
            int startCompare = Integer.compare(Integer.parseInt(o1[1]), Integer.parseInt(o2[1]));
            if (startCompare == 0) {
                int endCompare = Integer.compare(Integer.parseInt(o1[2]), Integer.parseInt(o2[2]));
                if (endCompare == 0)
                    return o1[0].compareTo(o2[0]);
                return endCompare;
            }
            return startCompare;
        });
        // display ranges
        for (String[] range : ranges) {
            tableViewVRRanges.getItems().add(range);
        }

        // load all frames within those ranges
        Set<Integer> rangeFrameNumbers = new HashSet<>();
        for (String[] range : ranges) {
            int start = Integer.parseInt(range[1]);
            int end = Integer.parseInt(range[2]);
            // add all frame number within this range, if range is valid
            if (start >= 0 && end >= 0) {
                rangeFrameNumbers.addAll(IntStream.rangeClosed(start, end).boxed().collect(Collectors.toSet()));
            }
        }

        return rangeFrameNumbers;
    }

    public TableView<String[]> getTableViewVRRanges() {
        return tableViewVRRanges;
    }
}
