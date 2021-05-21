package keymap.model;

public enum ActionIA implements Action {
    OPEN_LOAD_FOLDER_WINDOW("OpenLoadFolderWindow", "settings.ia.openLoadFolderWindow"),
    SAVE_ANNOTATIONS_DIALOG("SaveAnnotationsDialog", "settings.ia.saveAnnotationsDialog"),
    NEXT_IMAGE("NextImage", "settings.ia.nextImage"),
    PREVIOUS_IMAGE("PreviousImage" , "settings.ia.previousImage"),
    JUMP_TO_NEXT_VERIFIED_ANNOTATION("JumpToNextVerifiedAnnotation", "settings.ia.jumpToNextVerifiedAnnotation"),
    DELETE_ANNOTATION("DeleteAnnotation", "settings.ia.deleteAnnotation"),
    COPY_SELECTED_ANNOTATION("CopySelectedAnnotation", "settings.ia.copySelectedAnnotation"),
    ADD_DEFAULT_IMAGE_LABEL("AddDefaultImageLabel", "settings.ia.addDefaultImageLabel"),
    TRIGGER_SINGLE_DETECTION("TriggerSingleDetection", "settings.ia.triggerSingleDetection");

    private final String actionName;
    private final String resourceKey;

    ActionIA(String actionName, String resourceKey) {
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
        return String.format("IAAction{actionName=%s, resourceKey=%s}", actionName, resourceKey);
    }
}
