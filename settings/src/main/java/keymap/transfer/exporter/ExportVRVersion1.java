package keymap.transfer.exporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import keymap.model.Action;
import keymap.model.ReadOnlyKeyAction;
import keymap.model.ReadOnlyKeyActionClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ExportVRVersion1 implements ExportKeymap, ExportClassMappings {

    private static final Logger logger = LogManager.getLogger(ExportVRVersion1.class);

    /**
     * Exports the changes to the given file.
     * If the file already exists, then the changes will be appended and the file is not overwritten.
     *
     * @param changes the mapping with all actions and changes
     * @param file    file path, including the file itself
     */
    @Override
    public void export(Map<Action, ReadOnlyKeyAction> changes, Path file) throws Exception {
        // TODO: User jackson annotations for direct deserialization
        Map<String, List<String>> savable = ExportUtils.convertToSavableMap(changes);
        // get file content if file exists
        Optional<ObjectNode> optJson = getFileContent(file);
        // get content or create empty file
        ObjectNode json = optJson.orElse(createEmptyJson(file));
        // writer mappings
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode shortcuts = mapper.valueToTree(savable);
        json.set("shortcuts", shortcuts);
        mapper.writerWithDefaultPrettyPrinter().writeValue(Files.newOutputStream(file), json);

        if (savable.isEmpty()) {
            // delete user JSON file if file exists, if classes are empty and no changes exists (this branch)
            boolean delete = optJson.map(jsonNodes -> !jsonNodes.has("classes") || jsonNodes.get("classes").isEmpty()).orElse(true);
            if (delete) Files.deleteIfExists(file);
        }
    }

    @Override
    public void exportClassMappings(Map<String, ReadOnlyKeyActionClass> classMappingChanges, Path file) throws IOException {
        // convert to savable map
        Map<String, List<String>> savable = ExportUtils.convertToSavableClassMap(classMappingChanges);
        // get file content if file exists
        Optional<ObjectNode> optJson = getFileContent(file);
        // get content or create empty file
        ObjectNode json = optJson.orElse(createEmptyJson(file));
        // writer mapping
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonClassMappings = mapper.valueToTree(savable);
        json.set("classes", jsonClassMappings);
        mapper.writerWithDefaultPrettyPrinter().writeValue(Files.newOutputStream(file), json);
        if (savable.isEmpty()) {
            // delete user JSON file if file exists, if shortcuts are empty and not changes exists (this branch)
            boolean delete = optJson.map(jsonNodes -> !jsonNodes.has("shortcuts") || jsonNodes.get("shortcuts").isEmpty()).orElse(true);
            if (delete) Files.deleteIfExists(file);
        }
    }

    private ObjectNode createEmptyJson(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        root.put("version", 1);
        root.set("shortcuts", JsonNodeFactory.instance.objectNode());
        root.set("classes", JsonNodeFactory.instance.objectNode());
        return root;
    }

    private Optional<ObjectNode> getFileContent(Path file) {
        if (!Files.exists(file))
            return Optional.empty();

        ObjectMapper mapper = new ObjectMapper();
        try {
            return Optional.of((ObjectNode) mapper.readTree(Files.newInputStream(file)));
        } catch (IOException e) {
            logger.throwing(e);
            return Optional.empty();
        }
    }
}
