package videoreview;

public interface JsonLoadListener {

    /**
     * Will be called before the JSON is parsed and loaded.
     */
    void beforeLoad();

    /**
     * Will be calls after the JSON was parsed and loaded
     */
    void afterLoad();
}
