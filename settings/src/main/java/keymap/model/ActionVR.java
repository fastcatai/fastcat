package keymap.model;

public enum ActionVR implements Action {
    OPEN_LOAD_VIDEO_DIALOG("OpenLoadVideoDialog", "settings.vr.openLoadVideoDialog"),
    SAVE_DIALOG("SaveDialog", "settings.vr.saveDialog"),
    DELETE_ANNOTATION("DeleteAnnotation", "settings.vr.deleteAnnotation"),
    SKIP_IMAGE_FORWARD_SINGLE("SkipImageForwardSingle", "settings.vr.skipImageForwardSingle"),
    SKIP_IMAGE_BACKWARDS_SINGLE("SkipImageBackwardsSingle", "settings.vr.skipImageBackwardsSingle"),
    SKIP_IMAGE_FORWARD_DOUBLE("SkipImageForwardDouble", "settings.vr.skipImageForwardDouble"),
    SKIP_IMAGE_BACKWARDS_DOUBLE("SkipImageBackwardsDouble", "settings.vr.skipImageBackwardsDouble"),
    SKIP_IMAGE_FORWARD_TRIPLE("SkipImageForwardTriple", "settings.vr.skipImageForwardTriple"),
    SKIP_IMAGE_BACKWARDS_TRIPLE("SkipImageBackwardsTriple", "settings.vr.skipImageBackwardsTriple"),
    SKIP_SUPERFRAME_FORWARD_SINGLE("SkipSuperframeOneForward", "settings.vr.skipSuperframeOneForward"),
    SKIP_SUPERFRAME_BACKWARDS_SINGLE("SkipSuperframeOneBackwards", "settings.vr.skipSuperframeOneBackwards"),
    ADD_START_TAG("AddStartTag", "settings.vr.addStartTag"),
    ADD_END_TAG("AddEndTag", "settings.vr.addEndTag");

    private final String actionName;
    private final String resourceKey;

    ActionVR(String actionName, String resourceKey) {
        this.actionName = actionName;
        this.resourceKey = resourceKey;
    }

    @Override
    public String getActionName() {
        return actionName;
    }

    @Override
    public String getResourceKey() {
        return resourceKey;
    }

    @Override
    public String toString() {
        return String.format("VRAction{actionName=%s, resourceKey=%s}", actionName, resourceKey);
    }
}
