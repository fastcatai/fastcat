package keymap.transfer.exporter;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import keymap.Program;
import keymap.model.Action;
import keymap.model.KeyAction;
import keymap.model.ReadOnlyKeyAction;
import ui.Dialogs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ExportIAVersion1 implements ExportKeymap {

    @Override
    public void export(Map<Action, ReadOnlyKeyAction> changes, Path file) {
        try {
//            Map<Action, KeyAction> changes = ExportUtils.getMapChanges(mappings, ExportUtils.loadDefaultJSON(Program.IMAGE_ANNOTATION));
            Map<String, List<String>> savable = ExportUtils.convertToSavableMap(changes);
            if (!savable.isEmpty()) {
                Files.createDirectories(file.getParent());
                ObjectMapper mapper = new ObjectMapper();
                JsonNode shortcuts = mapper.valueToTree(savable);
                ObjectNode keymapChanges = JsonNodeFactory.instance.objectNode();
                keymapChanges.put("version", 1);
                keymapChanges.set("shortcuts", shortcuts);
                ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
                writer.writeValue(Files.newOutputStream(file), keymapChanges);
            }
        } catch (IOException e) {
            Dialogs.showError("Error", null, e.getMessage());
        }
    }
}
