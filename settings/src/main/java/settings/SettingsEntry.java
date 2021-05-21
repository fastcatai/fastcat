package settings;

import javafx.scene.Node;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignE;
import org.kordamp.ikonli.materialdesign2.MaterialDesignI;
import org.kordamp.ikonli.materialdesign2.MaterialDesignK;
import org.kordamp.ikonli.materialdesign2.MaterialDesignV;

public enum SettingsEntry {
    GENERAL("General", "/fxml/GeneralSettings.fxml", MaterialDesignD.DESKTOP_MAC_DASHBOARD),
    KEYMAP_IA("Keymap Image Annotation", "/fxml/keymapv2/KeymapIA.fxml", MaterialDesignK.KEYBOARD_SETTINGS),
    KEYMAP_VR("Keymap Video Review", "/fxml/keymapv2/KeymapVR.fxml", MaterialDesignK.KEYBOARD_SETTINGS),
    IMAGE_ANNOTATION("Image Annotation", "/fxml/ImageAnnotationSettings.fxml", MaterialDesignI.IMAGE_EDIT),
    VIDEO_REVIEW("Video Review", "/fxml/VideoReviewSettings.fxml", MaterialDesignE.EYE_CHECK_OUTLINE),
    ANNOTATION("Annotations", "/fxml/AnnotationSettings.fxml", MaterialDesignV.VECTOR_POLYGON);

    private final String text;
    private final String fxml;
    private final FontIcon icon;

    SettingsEntry(String text, String fxml, Ikon icon) {
        this.text = text;
        this.fxml = fxml;
        this.icon = new FontIcon(icon);
        this.icon.setIconSize(24);
    }

    public String getText() {
        return text;
    }

    public String getFxml() {
        return fxml;
    }

    public Node getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return text;
    }
}
