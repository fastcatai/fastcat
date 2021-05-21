package keymap.transfer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import keymap.Program;
import keymap.transfer.exporter.ExportClassMappings;
import keymap.transfer.exporter.ExportIAVersion1;
import keymap.transfer.exporter.ExportKeymap;
import keymap.transfer.exporter.ExportVRVersion1;
import keymap.transfer.importer.ImportClassMappings;
import keymap.transfer.importer.ImportIANoVersion;
import keymap.transfer.importer.ImportKeymap;
import keymap.transfer.importer.ImportVRNoVersion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class KeymapTransfer {

    public static ImportKeymap getImporter(final Path keymapFile, final Program program)
            throws IllegalArgumentException, IOException {

        int version = getVersion(keymapFile);

        if (program == Program.IMAGE_ANNOTATION)
            return getImageAnnotationImporter(version);
        if (program == Program.VIDEO_REVIEW)
            return getVideoReviewImporter(version);
        if (program == Program.VIDEO_ANNOTATION)
            return getVideoAnnotationImporter(version);
        throw new IllegalArgumentException(String.format("%s is not supported", program.name()));
    }

    private static int getVersion(final Path keymapFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        int version = -1;
        JsonNode root = mapper.readTree(Files.newInputStream(keymapFile));
        if (root.has("version"))
            version = root.get("version").asInt(-1);
        return version;
    }

    public static ExportKeymap getExporter(Program program) {
        if (program == Program.IMAGE_ANNOTATION)
            return getImageAnnotationExporter();
        if (program == Program.VIDEO_REVIEW)
            return getVideoReviewExporter();
        if (program == Program.VIDEO_ANNOTATION)
            return getVideoAnnotationExporter();
        throw new IllegalArgumentException(String.format("%s is not supported", program.name()));
    }

    public static ImportClassMappings getClassMappingsImporter(final Path keymapFile, final Program program)
            throws IOException {

        int version = getVersion(keymapFile);

        if (program != Program.VIDEO_REVIEW)
            throw new IllegalArgumentException(String.format("%s does not support class mapping export", program.name()));

        if (version <= 0)
            return new ImportVRNoVersion();
        if (version == 1)
            throw new IllegalArgumentException("Version 1 not implemented");
        throw new IllegalArgumentException(String.format("Version %d not supported", version));
    }

    public static ExportClassMappings getClassMappingsExporter(Program program) {
        if (program == Program.VIDEO_REVIEW)
            return new ExportVRVersion1();
        throw new IllegalArgumentException(String.format("%s does not support class mapping export", program.name()));
    }

    private static ImportKeymap getImageAnnotationImporter(final int version) {
        if (version <= 0)
            return new ImportIANoVersion();
        if (version == 1)
            throw new IllegalArgumentException("Version 1 not implemented");
        throw new IllegalArgumentException(String.format("Version %d not supported", version));
    }

    private static ExportKeymap getImageAnnotationExporter() {
        return new ExportIAVersion1();
    }

    private static ImportKeymap getVideoReviewImporter(final int version) {
        if (version <= 0)
            return new ImportVRNoVersion();
        if (version == 1)
            throw new IllegalArgumentException("Version 1 not implemented");
        throw new IllegalArgumentException(String.format("Version %d not supported", version));
    }

    private static ExportKeymap getVideoReviewExporter() {
        return new ExportVRVersion1();
    }

    private static ImportKeymap getVideoAnnotationImporter(final int version) {
        throw new IllegalArgumentException("Video Annotation import not implemented");
    }

    private static ExportKeymap getVideoAnnotationExporter() {
        throw new IllegalArgumentException("Video Annotation export not implemented");
    }
}
