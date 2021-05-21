package util;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;

public class SystemProperties {

    // contains user-defined settings, such as keymaps
    public static final String GLOBAL_CONFIG_PATH = "faiv.global.config.path";
    public static final String IA_CONFIG_PATH = "faiv.ia.config.path";
    public static final String VR_CONFIG_PATH = "faiv.vr.config.path";
    public static final String VA_CONFIG_PATH = "faiv.va.config.path";
    // contains log files
    public static final String GLOBAL_LOG_PATH = "faiv.global.log.path";
    public static final String IA_LOG_PATH = "faiv.ia.log.path";
    public static final String VR_LOG_PATH = "faiv.vr.log.path";
    public static final String VA_LOG_PATH = "faiv.va.log.path";

    static {
        setPathProperties();
    }

    private static void setPathProperties() {
        final String rootFolder;
        if (SystemUtils.IS_OS_WINDOWS) {
            rootFolder = System.getenv("APPDATA") + File.separatorChar + "faiv";
        } else if (SystemUtils.IS_OS_LINUX) {
            rootFolder = System.getProperty("user.home") + File.separatorChar + ".config" + File.separatorChar + "faiv";
        } else if (SystemUtils.IS_OS_MAC) {
            rootFolder = System.getProperty("user.home") + File.separatorChar + ".Library" + File.separatorChar
                    + "Application Support" + File.separatorChar + "faiv";
        } else {
            rootFolder = System.getProperty("user.home") + File.separatorChar + "faiv";
        }

        final String versionSpecificFolder = rootFolder + File.separatorChar + "faiv" + AppVersion.majorMinor();
        final String iaFolder = versionSpecificFolder + File.separatorChar + "ImageAnnotation";
        final String vrFolder = versionSpecificFolder + File.separatorChar + "VideoReview";
        final String vaFolder = versionSpecificFolder + File.separatorChar + "VideoAnnotation";

        System.setProperty(GLOBAL_CONFIG_PATH, versionSpecificFolder);
        System.setProperty(IA_CONFIG_PATH, iaFolder);
        System.setProperty(VR_CONFIG_PATH, vrFolder);
        System.setProperty(VA_CONFIG_PATH, vaFolder);

        System.setProperty(GLOBAL_LOG_PATH, versionSpecificFolder + File.separatorChar + "logs");
        System.setProperty(IA_LOG_PATH, iaFolder + File.separatorChar + "logs");
        System.setProperty(VR_LOG_PATH, vrFolder + File.separatorChar + "logs");
        System.setProperty(VA_LOG_PATH, vaFolder + File.separatorChar + "logs");
    }
}
