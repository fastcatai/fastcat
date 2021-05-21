package keymap.model;

public class ActionClass implements Action {

    private final String className;

    public ActionClass(String className) {
        this.className = className;
    }

    @Override
    public String getActionName() {
        return className;
    }

    @Override
    public String getResourceKey() {
        throw new UnsupportedOperationException("Class action does not have a resource key");
    }
}
