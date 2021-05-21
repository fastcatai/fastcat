package util;

import java.util.Scanner;

/**
 * This application uses semantic versioning.
 * @see <a href="https://semver.org">Semantic Versioning</a>
 */
public class AppVersion {

    // when you make incompatible API changes
    private static int major = 0;
    // when you add functionality in a backwards compatible manner
    private static int minor = 0;
    // when you make backwards compatible bug fixes
    private static int patch = 0;

    static {
        try(Scanner scanner = new Scanner(AppVersion.class.getResourceAsStream("/version"))) {
            String version = scanner.nextLine().trim();
            String[] versionSegments = version.split("\\.");
            major = Integer.parseInt(versionSegments[0].trim());
            minor = Integer.parseInt(versionSegments[1].trim());
            patch = Integer.parseInt(versionSegments[2].trim());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private AppVersion() {
        // block object creation
    }

    public static int getMajor() {
        return major;
    }

    public static int getMinor() {
        return minor;
    }

    public static int getPatch() {
        return patch;
    }

    public static String asString() {
        return String.format("%d.%d.%d", major, minor, patch);
    }

    public static String majorMinor() {
        return String.format("%d.%d", major, minor);
    }
}
