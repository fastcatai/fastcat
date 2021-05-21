package modelV3;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collection;

public abstract class Annotation {

    private final StringProperty label;
    private final StringProperty instance;
    private final BooleanProperty autoCreated;
    private final BooleanProperty verified;
    private final ObservableList<String> additionalLabels;

    public Annotation() {
        this("");
    }

    public Annotation(String label) {
        this(label, "");
    }

    public Annotation(String label, String instance) {
        this(label, instance, false, false);
    }

    public Annotation(String label, String instance, boolean verified, boolean autoCreated) {
        this.label = new SimpleStringProperty(label.strip());
        this.instance = new SimpleStringProperty(instance.strip());
        this.autoCreated = new SimpleBooleanProperty(autoCreated);
        this.verified = new SimpleBooleanProperty(verified);
        additionalLabels = FXCollections.observableArrayList();
    }

    public boolean isEmpty() {
        return label.get().isBlank() && instance.get().isBlank() && additionalLabels.isEmpty()
                && !autoCreated.get() && !verified.get();
    }

    public String getLabel() {
        return label.get();
    }

    public StringProperty labelProperty() {
        return label;
    }

    public void setLabel(String label) {
        this.label.set(label);
    }

    public String getInstance() {
        return instance.get();
    }

    public StringProperty instanceProperty() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance.set(instance);
    }

    public boolean isAutoCreated() {
        return autoCreated.get();
    }

    public BooleanProperty autoCreatedProperty() {
        return autoCreated;
    }

    public void setAutoCreated(boolean wasAutoCreated) {
        autoCreated.set(wasAutoCreated);
    }

    public ObservableList<String> getAdditionalLabels() {
        return additionalLabels;
    }

    public void addAdditionalLabel(String label) {
        additionalLabels.add(label.trim());
    }

    public void addAllAdditionalLabels(Collection<String> labels) {
        additionalLabels.addAll(labels);
    }

    public void removeAdditionalLabel(String label) {
        additionalLabels.remove(label);
    }

    public boolean isVerified() {
        return verified.get();
    }

    public BooleanProperty verifiedProperty() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified.set(verified);
    }
}