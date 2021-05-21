package keymap.transfer.importer;

import keymap.model.KeyActionClass;

import java.nio.file.Path;
import java.util.Map;

public interface ImportClassMappings {
    Map<String, KeyActionClass> importClassMappings(final Path keymapFile) throws Exception;
}
