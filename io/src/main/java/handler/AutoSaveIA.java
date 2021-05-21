package handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import json.IAJsonModule;
import jsonsaver.JsonSaver;
import model.ImageData;
import model.LoadedAnnotationData;
import settings.SettingsProperties;
import wrapper.IAJsonWrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AutoSaveIA {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new IAJsonModule());
    }

    private static void writeJson(final Path output, final LoadedScheduler loadedScheduler) {
        // only get non-empty images
        List<ImageData> annotatedImages = loadedScheduler.images.stream()
                .filter(Predicate.not(ImageData::isEmpty))
                .collect(Collectors.toList());

        IAJsonWrapper jsonWrapper = new IAJsonWrapper(annotatedImages, loadedScheduler.loadedData);
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(Files.newOutputStream(output), jsonWrapper);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final ObjectProperty<LoadedScheduler> loadedScheduler = new SimpleObjectProperty<>() {
        LoadedScheduler oldScheduler;

        @Override
        protected void invalidated() {
            if (oldScheduler != null)
                stopAutoSaveScheduler(oldScheduler);
            LoadedScheduler scheduler = get();
            oldScheduler = scheduler;
            if (scheduler != null)
                startAutoSaveScheduler(scheduler);
        }
    };

    private static void startAutoSaveScheduler(final LoadedScheduler loadedScheduler) {
        int autoSaveInterval = Integer.parseInt(SettingsProperties.getProperty(SettingsProperties.GENERAL_AUTO_SAVE_INTERVAL));
        loadedScheduler.autoSaveScheduler.scheduleWithFixedDelay(
                () -> {
                    Path jsonFile = loadedScheduler.loadedData.getJsonFile();
                    Path outputFolder = jsonFile != null ? jsonFile.getParent() : loadedScheduler.loadedData.getImageFolder().getParent();
                    writeJson(outputFolder.resolve(outputFolder.getFileName().toString() + JsonSaver.IA_AUTO_SAVE_FILE_EXTENSION),
                            loadedScheduler);
                },
                autoSaveInterval,
                autoSaveInterval,
                TimeUnit.SECONDS);
    }

    private static void stopAutoSaveScheduler(final LoadedScheduler loadedScheduler) {
        if (loadedScheduler.autoSaveScheduler != null && !loadedScheduler.autoSaveScheduler.isShutdown() && loadedScheduler.loadedData != null) {
            loadedScheduler.autoSaveScheduler.submit(() -> {
                // save last time before shutdown
                Path jsonFile = loadedScheduler.loadedData.getJsonFile();
                Path outputFolder = jsonFile != null ? jsonFile.getParent() : loadedScheduler.loadedData.getImageFolder().getParent();
                final Path outputFile = outputFolder.resolve(outputFolder.getFileName().toString() + JsonSaver.IA_AUTO_SAVE_FILE_EXTENSION);
                writeJson(outputFile, loadedScheduler);

                // create backup
                try {
                    Path backupFolder = outputFolder.resolve(JsonSaver.BACKUP_FOLDER_NAME);
                    if (!Files.exists(backupFolder))
                        Files.createDirectory(backupFolder);
                    String datetime = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss").format(new Date());
                    Path backupFile = backupFolder.resolve(String.format("%s%s", datetime, JsonSaver.IA_AUTO_SAVE_FILE_EXTENSION));
                    Files.copy(outputFile, backupFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            loadedScheduler.autoSaveScheduler.shutdown();
        }
    }

    public static void startAutoSaveScheduler(List<ImageData> images, LoadedAnnotationData loadedData) {
        loadedScheduler.set(new LoadedScheduler(images, loadedData));
    }

    public static void stopAutoSaveScheduler() {
        loadedScheduler.set(null);
    }

    private static class LoadedScheduler {
        private final ScheduledExecutorService autoSaveScheduler;
        private final List<ImageData> images;
        private final LoadedAnnotationData loadedData;

        private LoadedScheduler(List<ImageData> images, LoadedAnnotationData loadedData) {
            this.images = images;
            this.loadedData = loadedData;
            autoSaveScheduler = Executors.newSingleThreadScheduledExecutor();
        }
    }
}
