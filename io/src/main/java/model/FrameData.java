package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.Collection;
import java.util.Objects;

public class FrameData {

    private final SuperframeData superframe;
    private final int frameNumber;
    private final ObservableList<String> classes;


    public FrameData(SuperframeData superframe, int frameNumber) {
        this.superframe = superframe;
        this.frameNumber = frameNumber;
        classes = FXCollections.observableArrayList();

        // keep track of the class counts
        classes.addListener((ListChangeListener<? super String>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (String aClass : c.getAddedSubList())
                        superframe.addClassToCount(aClass);
                }
                if (c.wasRemoved()) {
                    for (String aClass : c.getRemoved())
                        superframe.removeClassFromCount(aClass);
                    if (classes.isEmpty()) // remove itself if no classes
                        superframe.removeChildFrame(this);
                }
            }
        });
    }

    public SuperframeData getSuperframe() {
        return superframe;
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public void toggleClass(String aClass) {
        if (classes.contains(aClass)) classes.remove(aClass);
        else classes.add(aClass);
    }

    public ObservableList<String> getClasses() {
        return classes;
    }

    /**
     * Clears the list and sets new classes.
     *
     * @param classes list of classes
     */
    public void setNewClasses(Collection<String> classes) {
        this.classes.clear();
        this.classes.addAll(classes);
    }

    /**
     * Clears the list and sets new classes.
     *
     * @param classes array of classes
     */
    public void setNewClasses(String... classes) {
        this.classes.clear();
        this.classes.addAll(classes);
    }

    private StringProperty comment;

    public String getComment() {
        return commentProperty().get();
    }

    public StringProperty commentProperty() {
        if (comment == null)
            comment = new SimpleStringProperty("");
        return comment;
    }

    public void setComment(String comment) {
        commentProperty().set(Objects.toString(comment, ""));
    }

    @Override
    public String toString() {
        return "FrameData{FrameNumber=" + frameNumber + '}';
    }
}
