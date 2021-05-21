package util;

public class CyclicalIntegerProperty {

    private Integer size;
    private int value;
    private int initValue;
    private CyclicalNumberChangeListener<Integer> listener;

    /**
     * Create a property with not size set. When a value is set the modulo operation is applied
     * before saving, like (<code>value = newValue % size</code>).
     * A size must be set with {@link #setSize(int)} before {@link #set(int)} can be used.
     */
    public CyclicalIntegerProperty() {
        this(0);
    }

    public CyclicalIntegerProperty(int initValue) {
        this.initValue = initValue;
        size = null;
    }

    public void addListener(CyclicalNumberChangeListener<Integer> listener) {
        this.listener = listener;
    }

    /**
     * Set size of the cycle. Internal value is set back to <code>initValue</code> (default: 0).
     * @param size cycle size
     */
    public void setSize(int size) {
        this.size = size;
        value = initValue;
    }

    public void set(int newValue) {
        if (size == null)
            throw new NullPointerException("The size was not set");

        if (value == newValue)
            return;

        newValue %= size;
        if (newValue < 0)
            newValue = size + newValue;

        listener.changed(size, value, newValue);
        value = newValue;
    }

    public int get() {
        return value;
    }

    public void increment() {
        set(get() + 1);
    }

    public void decrement() {
        set(get() - 1);
    }
}
