package keymap.transfer.importer;

import keymap.model.Action;
import keymap.model.KeyAction;

import java.nio.file.Path;
import java.util.Map;

public interface ImportKeymap {
    Map<Action, KeyAction> loadKeymap(final Path keymapFile) throws Exception;
}
