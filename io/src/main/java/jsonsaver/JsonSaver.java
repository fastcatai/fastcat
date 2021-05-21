package jsonsaver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.LoadedAnnotationData;
import model.LoadedReviewData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Deprecated
public class JsonSaver {

    public static final String IA_FILE_EXTENSION = ".annotation.json";
    public static final String IA_AUTO_SAVE_FILE_EXTENSION = ".annotation.auto.json";
    public static final String IA_AUTO_SAVE_FILENAME = "images" + IA_AUTO_SAVE_FILE_EXTENSION;
    public static final String VR_FILE_EXTENSION = ".review.json";
    public static final String VR_AUTO_SAVE_FILE_EXTENSION = ".review.auto.json";
    public static final String VR_AUTO_SAVE_FILENAME = "video" + VR_AUTO_SAVE_FILE_EXTENSION;
    public static final String BACKUP_FOLDER_NAME = ".backup";

//    private static final ObjectMapper mapper = new ObjectMapper();
//
//    public static void saveJsonIA(Path outputJsonFile, LoadedAnnotationData loadedAnnotationData) {
//        // use latest converter
//        Path relImageFolder = outputJsonFile.getParent().relativize(loadedAnnotationData.getImageFolder());
//        JsonNode root = IAToJsonConverterV1.convert(loadedAnnotationData.getImageDataList(), relImageFolder);
//        save(outputJsonFile, root);
//    }
//
//    public static void saveJsonVR(Path outputJsonFile, LoadedReviewData reviewData) {
//        // use latest converter
//        JsonNode root = VRToJsonConverterV2.convert(reviewData, outputJsonFile);
//        save(outputJsonFile, root);
//    }
//
//    private static void save(Path outputJsonFile, JsonNode json) {
//        try {
//            mapper.writerWithDefaultPrettyPrinter().writeValue(Files.newOutputStream(outputJsonFile), json);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
