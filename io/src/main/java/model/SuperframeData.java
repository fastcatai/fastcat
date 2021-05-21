package model;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SuperframeData {

    // The frame number of that superframe
    private final int frameNumber;

    // Holds a frames that were marked with at least one class
    private final ObservableList<FrameData> childFrames;
    private final ObservableList<FrameData> unmodifiableChildFrames;

    // Holds the same frames as the child frames list and will be synchronized.
    // Adding and removing must be done over the child frames list.
    private final Map<Integer, FrameData> mappingNumberToData;

    // A mapping between classes and there total count within the freeze frame
    private final ObservableMap<String, Integer> mappingClassToCount;
    private final ObservableMap<String, Integer> unmodifiedMappingClassToCount;

    public SuperframeData(int frameNumber) {
        this.frameNumber = frameNumber;
        mappingClassToCount = FXCollections.observableHashMap();
        unmodifiedMappingClassToCount = FXCollections.unmodifiableObservableMap(mappingClassToCount);
        mappingNumberToData = new HashMap<>();
        childFrames = FXCollections.observableArrayList();
        unmodifiableChildFrames = FXCollections.unmodifiableObservableList(childFrames);

        // synchronize list with map to have faster access to the frames
        childFrames.addListener((ListChangeListener<? super FrameData>) c -> {
            while (c.next()) {
                if (c.wasRemoved()) {
                    for (FrameData fd : c.getRemoved())
                        mappingNumberToData.remove(fd.getFrameNumber(), fd);
                }
                if (c.wasAdded()) {
                    for (FrameData fd : c.getAddedSubList())
                        mappingNumberToData.put(fd.getFrameNumber(), fd);
                    // sort if a new item was added
                    c.getList().sort(Comparator.comparingInt(FrameData::getFrameNumber));
                }
            }
        });
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public Optional<FrameData> getFrame(int frameNumber) {
        return Optional.ofNullable(mappingNumberToData.get(frameNumber));
    }

    public ObservableList<FrameData> getChildFrames() {
        return unmodifiableChildFrames;
    }

    public void addChildFrame(FrameData frameData) {
        childFrames.add(frameData);
    }

    public void removeChildFrame(FrameData frameData) {
        childFrames.remove(frameData);
    }

    public void setNewChildFrames(ObservableList<FrameData> childFrames) {
        this.childFrames.clear();
        this.childFrames.addAll(childFrames.stream()
                .filter(frameData -> !frameData.getClasses().isEmpty())
                .sorted(Comparator.comparingInt(FrameData::getFrameNumber))
                .collect(Collectors.toList()));
    }

    public ObservableMap<String, Integer> getMappingClassToCount() {
        return unmodifiedMappingClassToCount;
    }

    void addClassToCount(String clazz) {
        if (mappingClassToCount.containsKey(clazz))
            mappingClassToCount.put(clazz, mappingClassToCount.get(clazz) + 1);
        else mappingClassToCount.put(clazz, 1);
    }

    void removeClassFromCount(String clazz) {
        if (mappingClassToCount.containsKey(clazz)) {
            int newCount = mappingClassToCount.get(clazz) - 1;
            if (newCount <= 0) mappingClassToCount.remove(clazz);
            else mappingClassToCount.put(clazz, newCount);
        }
    }

    @Override
    public String toString() {
        return "SuperframeData{FrameNumber=" + frameNumber + '}';
    }
}
