package util;

public interface CyclicalNumberChangeListener<T extends Number> {
    void changed(T size, T oldIndex, T newIndex);
}
