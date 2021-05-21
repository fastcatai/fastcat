package settings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.SystemProperties;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class SettingsProperties {
    private static final Logger logger = LogManager.getLogger(SettingsProperties.class);

    // general settings
    public static final String GENERAL_AUTO_SAVE_INTERVAL = "general.autosave.interval";
    // image annotation specific settings
    public static final String IMAGE_ANNOTATION_DETECTION_SERVER = "imageannotation.detection.server";
    public static final String IMAGE_ANNOTATION_DOCKER_HOST = "imageannotation.docker.host";
    public static final String IMAGE_ANNOTATION_DOCKER_IMAGE = "imageannotation.docker.image";
    public static final String IMAGE_ANNOTATION_DOCKER_CONTAINER = "imageannotation.docker.container";
    public static final String IMAGE_ANNOTATION_EDIT_ON_SELECT = "imageannotation.editOnSelect";
    public static final String IMAGE_ANNOTATION_DEFAULT_LABEL = "imageannotation.defaultLabel";
    public static final String IMAGE_ANNOTATION_TRACKING_CHECKED_OPTION = "imageannotation.tracking.checked.option";
    // video review specific settings
    public static final String VIDEO_REVIEW_DISPLAY_FRAME_PLAYER_DELAY = "videoreview.frameplayer.display.delay";
    // annotation selected style
    public static final String ANNOTATION_FILL_COLOR_SELECTED = "annotation.fill.color.selected";
    public static final String ANNOTATION_FILL_COLOR_HOVER_SELECTED = "annotation.fill.color.hover.selected";
    public static final String ANNOTATION_STROKE_COLOR_SELECTED = "annotation.stroke.color.selected";
    public static final String ANNOTATION_STROKE_COLOR_HOVER_SELECTED = "annotation.stroke.color.hover.selected";
    public static final String ANNOTATION_STROKE_WIDTH_SELECTED = "annotation.stroke.width.selected";
    public static final String ANNOTATION_STROKE_WIDTH_HOVER_SELECTED = "annotation.stroke.width.hover.selected";
    // annotation verified style
    public static final String ANNOTATION_FILL_COLOR_VERIFIED = "annotation.fill.color.verified";
    public static final String ANNOTATION_FILL_COLOR_HOVER_VERIFIED = "annotation.fill.color.hover.verified";
    public static final String ANNOTATION_STROKE_COLOR_VERIFIED = "annotation.stroke.color.verified";
    public static final String ANNOTATION_STROKE_COLOR_HOVER_VERIFIED = "annotation.stroke.color.hover.verified";
    public static final String ANNOTATION_STROKE_WIDTH_VERIFIED = "annotation.stroke.width.verified";
    public static final String ANNOTATION_STROKE_WIDTH_HOVER_VERIFIED = "annotation.stroke.width.hover.verified";
    // annotation auto created style
    public static final String ANNOTATION_FILL_COLOR_AUTO = "annotation.fill.color.auto";
    public static final String ANNOTATION_FILL_COLOR_HOVER_AUTO = "annotation.fill.color.hover.auto";
    public static final String ANNOTATION_STROKE_COLOR_AUTO = "annotation.stroke.color.auto";
    public static final String ANNOTATION_STROKE_COLOR_HOVER_AUTO = "annotation.stroke.color.hover.auto";
    public static final String ANNOTATION_STROKE_WIDTH_AUTO = "annotation.stroke.width.auto";
    public static final String ANNOTATION_STROKE_WIDTH_HOVER_AUTO = "annotation.stroke.width.hover.auto";
    // annotation default style
    public static final String ANNOTATION_FILL_COLOR_DEFAULT = "annotation.fill.color.default";
    public static final String ANNOTATION_FILL_COLOR_HOVER_DEFAULT = "annotation.fill.color.hover.default";
    public static final String ANNOTATION_STROKE_COLOR_DEFAULT = "annotation.stroke.color.default";
    public static final String ANNOTATION_STROKE_COLOR_HOVER_DEFAULT = "annotation.stroke.color.hover.default";
    public static final String ANNOTATION_STROKE_WIDTH_DEFAULT = "annotation.stroke.width.default";
    public static final String ANNOTATION_STROKE_WIDTH_HOVER_DEFAULT = "annotation.stroke.width.hover.default";
    // annotation label selected style
    public static final String ANNOTATION_LABEL_FILL_COLOR_SELECTED = "annotation.label.fill.color.selected";
    public static final String ANNOTATION_LABEL_FILL_COLOR_HOVER_SELECTED = "annotation.label.fill.color.hover.selected";
    public static final String ANNOTATION_LABEL_FONT_COLOR_SELECTED = "annotation.label.font.color.selected";
    public static final String ANNOTATION_LABEL_FONT_COLOR_HOVER_SELECTED = "annotation.label.font.color.hover.selected";
    public static final String ANNOTATION_LABEL_BORDER_COLOR_SELECTED = "annotation.label.border.color.selected";
    public static final String ANNOTATION_LABEL_BORDER_STYLE_SELECTED = "annotation.label.border.style.selected";
    // annotation label verified style
    public static final String ANNOTATION_LABEL_FILL_COLOR_VERIFIED = "annotation.label.fill.color.verified";
    public static final String ANNOTATION_LABEL_FILL_COLOR_HOVER_VERIFIED = "annotation.label.fill.color.hover.verified";
    public static final String ANNOTATION_LABEL_FONT_COLOR_VERIFIED = "annotation.label.font.color.verified";
    public static final String ANNOTATION_LABEL_FONT_COLOR_HOVER_VERIFIED = "annotation.label.font.color.hover.verified";
    public static final String ANNOTATION_LABEL_BORDER_COLOR_VERIFIED = "annotation.label.border.color.verified";
    public static final String ANNOTATION_LABEL_BORDER_STYLE_VERIFIED = "annotation.label.border.style.verified";
    // annotation label auto created style
    public static final String ANNOTATION_LABEL_FILL_COLOR_AUTO = "annotation.label.fill.color.auto";
    public static final String ANNOTATION_LABEL_FILL_COLOR_HOVER_AUTO = "annotation.label.fill.color.hover.auto";
    public static final String ANNOTATION_LABEL_FONT_COLOR_AUTO = "annotation.label.font.color.auto";
    public static final String ANNOTATION_LABEL_FONT_COLOR_HOVER_AUTO = "annotation.label.font.color.hover.auto";
    public static final String ANNOTATION_LABEL_BORDER_COLOR_AUTO = "annotation.label.border.color.auto";
    public static final String ANNOTATION_LABEL_BORDER_STYLE_AUTO = "annotation.label.border.style.auto";
    // annotation label default style
    public static final String ANNOTATION_LABEL_FILL_COLOR_DEFAULT = "annotation.label.fill.color.default";
    public static final String ANNOTATION_LABEL_FILL_COLOR_HOVER_DEFAULT = "annotation.label.fill.color.hover.default";
    public static final String ANNOTATION_LABEL_FONT_COLOR_DEFAULT = "annotation.label.font.color.default";
    public static final String ANNOTATION_LABEL_FONT_COLOR_HOVER_DEFAULT = "annotation.label.font.color.hover.default";
    public static final String ANNOTATION_LABEL_BORDER_COLOR_DEFAULT = "annotation.label.border.color.default";
    public static final String ANNOTATION_LABEL_BORDER_STYLE_DEFAULT = "annotation.label.border.style.default";
    // annotation label
    public static final String ANNOTATION_LABEL_VISIBLE = "annotation.label.visible";
    // annotation anchor style
    public static final String ANNOTATION_ANCHOR_FILL_COLOR_DEFAULT = "annotation.anchor.fill.color.default";
    public static final String ANNOTATION_ANCHOR_FILL_COLOR_HOVER = "annotation.anchor.fill.color.hover";
    public static final String ANNOTATION_ANCHOR_STROKE_COLOR_DEFAULT = "annotation.anchor.stroke.color.default";
    public static final String ANNOTATION_ANCHOR_STROKE_COLOR_HOVER = "annotation.anchor.stroke.color.hover";
    public static final String ANNOTATION_ANCHOR_SIZE_DEFAULT = "annotation.anchor.size.default";
    public static final String ANNOTATION_ANCHOR_SIZE_HOVER = "annotation.anchor.size.hover";

    // stores the default properties
    private static final Properties defaultProperties = new Properties();
    // has all up-to-date key-value pairs
    private static Properties properties;

    static {
        loadProperties();
    }

    /**
     * Loads default properties and overwrite custom settings if setting file in user config folder exists.
     */
    private static void loadProperties() {
        try {
            InputStream inStream = SettingsProperties.class.getClassLoader().getResourceAsStream("DefaultSettings.properties");
            if (inStream != null)
                defaultProperties.load(inStream);
            properties = new Properties(defaultProperties);
            Path customProperties = Path.of(System.getProperty(SystemProperties.GLOBAL_CONFIG_PATH), "settings.properties");
            if (Files.exists(customProperties))
                properties.load(Files.newInputStream(customProperties));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Searches for the property with the specified key in the property list.
     *
     * @param key the property key, some keys are defined as constants in this class: {@link SettingsProperties}
     * @return the value of the property or <code>null</code> if the property is not found.
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Searches for the property with the specified key in the property list.
     *
     * @param key          the property key, some keys are defined as constants in this class: {@link SettingsProperties}
     * @param defaultValue a default value
     * @return the value of the property or the default value if the property is not found.
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Only set a new key if key does not exist in default or the value differs from default.
     *
     * @param key   property key
     * @param value property value
     */
    public static void setProperty(String key, String value) {
        String defaultValue = defaultProperties.getProperty(key);
        if (defaultValue == null || !defaultValue.equals(value))
            properties.setProperty(key, value);
        else properties.remove(key);
    }

    /**
     * Saves changes that the user made into OS-dependent config path.
     * Deletes the properties file (if existent) when no changes are registered.
     * This means that all settings are the default settings.
     */
    public static void saveProperty() {
        try {
            Path customProperties = Path.of(System.getProperty(SystemProperties.GLOBAL_CONFIG_PATH), "settings.properties");

            if (!Files.exists(customProperties.getParent())){
                Files.createDirectories(customProperties.getParent());
            }

            if (properties.size() > 0) {
                properties.store(Files.newOutputStream(customProperties), "Custom Settings");
            } else Files.deleteIfExists(customProperties);
        } catch (IOException e) {
            logger.throwing(e);
        }
    }
}
