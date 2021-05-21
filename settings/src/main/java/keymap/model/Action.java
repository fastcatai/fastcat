package keymap.model;

public interface Action {
    String getActionName();
    String getResourceKey();

    static Action valueOfActionName(Action[] allActions, String actionName) {
        for (Action a : allActions) {
            if (a.getActionName().equals(actionName))
                return a;
        }
        throw new IllegalArgumentException(String.format("No action with actionName=%s", actionName));
    }
}
