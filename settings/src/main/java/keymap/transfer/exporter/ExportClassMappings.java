package keymap.transfer.exporter;

import keymap.model.ReadOnlyKeyActionClass;

import java.nio.file.Path;
import java.util.Map;

public interface ExportClassMappings {

    /**
     * Exports the class mappings changes to the file
     * If the file already exists, then the class mappings will be appended and the file is not overwritten.
     *
     * @param classMappingsChanges the mappings with the class changes
     * @param file                 file path, including the file itself
     */
    void exportClassMappings(Map<String, ReadOnlyKeyActionClass> classMappingsChanges, Path file) throws Exception;
}
