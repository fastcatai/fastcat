package keymap.transfer.exporter;

import keymap.model.Action;
import keymap.model.ReadOnlyKeyAction;

import java.nio.file.Path;
import java.util.Map;

public interface ExportKeymap {

    /**
     * Exports the changes to the given file.
     *
     * @param changes the changes mapping
     * @param file    file path, including the file itself
     */
    void export(Map<Action, ReadOnlyKeyAction> changes, Path file) throws Exception;
}
