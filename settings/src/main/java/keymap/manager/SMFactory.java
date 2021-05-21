package keymap.manager;

import keymap.Program;

import java.util.EnumMap;

public class SMFactory {

    private static final EnumMap<Program, ShortcutManager> enumToClass = new EnumMap<>(Program.class);

    static {
        enumToClass.put(Program.IMAGE_ANNOTATION, new IAShortcutManager());
        enumToClass.put(Program.VIDEO_REVIEW, new VRShortcutManager());
    }

    public static ShortcutManager getShortcutManager(Program manager) {
        return enumToClass.get(manager);
    }
}
