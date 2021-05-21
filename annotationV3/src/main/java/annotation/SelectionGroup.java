package annotation;

import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class SelectionGroup {

    private final ObservableList<UIAnnotation<?>> annotations = FXCollections.observableArrayList();

    public SelectionGroup() {

        annotations.addListener((ListChangeListener<UIAnnotation<?>>) c -> {
            while (c.next()) {
                // Look through the removed annotations, and if one
                // of them was the one and only selected toggle,
                // then we will clear the selected toggle property.
                for (UIAnnotation<?> a : c.getRemoved()) {
                    if (a.isSelected()) {
                        a.setSelected(false);
                        selectAnnotation(null);
                    }
                }

                // An annotation can only be in one group at any one time.
                // If the group is changed, then the toggle is removed from
                // the old group prior to being added to the new group.
                for (UIAnnotation<?> a : c.getAddedSubList()) {
                    if (!SelectionGroup.this.equals(a.getSelectionGroup())) {
                        if (a.getSelectionGroup() != null)
                            a.getSelectionGroup().getAnnotations().remove(a);
                        a.setSelectionGroup(SelectionGroup.this);
                    }
                }

                // Look through all the added toggles and the very first selected
                // annotation we encounter will become the one we make the selected
                // annotation for this group.
                for (UIAnnotation<?> a : c.getAddedSubList()) {
                    if (a.isSelected()) {
                        selectAnnotation(a);
                        break;
                    }
                }
            }
        });
    }

    public final ObservableList<UIAnnotation<?>> getAnnotations() {
        return annotations;
    }

    private final ReadOnlyObjectWrapper<UIAnnotation<?>> selectedAnnotation = new ReadOnlyObjectWrapper<>() {
        @Override
        public void set(final UIAnnotation<?> newSelectedAnnotation) {
            if (isBound())
                throw new java.lang.RuntimeException("A bound value cannot be set.");

            final UIAnnotation<?> old = get();
            if (old == newSelectedAnnotation)
                return;

            if (setSelected(newSelectedAnnotation, true)
                    || (newSelectedAnnotation != null && newSelectedAnnotation.getSelectionGroup() == SelectionGroup.this)
                    || newSelectedAnnotation == null) {
                if (old == null || old.getSelectionGroup() == SelectionGroup.this) {
                    setSelected(old, false);
                }
                super.set(newSelectedAnnotation);
            }
        }
    };

    public final void selectAnnotation(UIAnnotation<?> annotation) {
        selectedAnnotation.set(annotation);
    }

    public final UIAnnotation<?> getSelectedAnnotation() {
        return selectedAnnotation.get();
    }

    public final ReadOnlyObjectProperty<UIAnnotation<?>> selectedAnnotationProperty() {
        return selectedAnnotation.getReadOnlyProperty();
    }

    private boolean setSelected(UIAnnotation<?> annotation, boolean selected) {
        if (annotation != null
                && annotation.getSelectionGroup() == this
                && !annotation.selectedProperty().isBound()) {
            annotation.setSelected(selected);
            return true;
        }
        return false;
    }
}
