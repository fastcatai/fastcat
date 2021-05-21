package i18n;

// As a future project: https://riptutorial.com/javafx/example/23068/switching-language-dynamically-when-the-application-is-running

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18N {
    private static final ResourceBundle resourceBundle;

    static {
        resourceBundle = ResourceBundle.getBundle("i18n.AppBundle");
    }

    public static ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public static String getString(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }
}
