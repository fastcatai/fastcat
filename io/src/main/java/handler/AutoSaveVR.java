package handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import json.VRJsonModule;
import jsonsaver.JsonSaver;
import model.ImageData;
import model.LoadedReviewData;
import model.SuperframeData;
import settings.SettingsProperties;
import wrapper.VRJsonWrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoSaveVR {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new VRJsonModule());
    }

    private static final ObjectProperty<LoadedScheduler> loadedScheduler = new SimpleObjectProperty<>() {
        LoadedScheduler oldScheduler;

        @Override
        protected void invalidated() {
            if (oldScheduler != null)
                stopAutoSaveScheduler(oldScheduler);
            LoadedScheduler scheduler = get();
            oldScheduler = scheduler;
            if (scheduler == null)
                return;
            startAutoSaveScheduler(scheduler);
        }
    };

    private static void writeJson(final Path output, final LoadedScheduler loadedScheduler) {
        VRJsonWrapper jsonWrapper = new VRJsonWrapper(loadedScheduler.superframes, loadedScheduler.images, loadedScheduler.loadedData);
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(Files.newOutputStream(output), jsonWrapper);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startAutoSaveScheduler(final LoadedScheduler loadedScheduler) {
        int autoSaveInterval = Integer.parseInt(SettingsProperties.getProperty(SettingsProperties.GENERAL_AUTO_SAVE_INTERVAL));
        loadedScheduler.autoSaveScheduler.scheduleWithFixedDelay(
                () -> writeJson(loadedScheduler.loadedData.getVideoFile().getParent().resolve(JsonSaver.VR_AUTO_SAVE_FILENAME), loadedScheduler),
                autoSaveInterval,
                autoSaveInterval,
                TimeUnit.SECONDS);
    }


    private static void stopAutoSaveScheduler(final LoadedScheduler loadedScheduler) {
        if (loadedScheduler.autoSaveScheduler != null && !loadedScheduler.autoSaveScheduler.isShutdown() && loadedScheduler.loadedData != null) {
            loadedScheduler.autoSaveScheduler.submit(() -> {
                // save last time before shutdown
                final Path outputFolder = loadedScheduler.loadedData.getVideoFile().getParent();
                final Path outputFile = outputFolder.resolve(JsonSaver.VR_AUTO_SAVE_FILENAME);
                writeJson(outputFile, loadedScheduler);

                // create backup
                try {
                    Path backupFolder = outputFolder.resolve(JsonSaver.BACKUP_FOLDER_NAME);
                    if (!Files.exists(backupFolder))
                        Files.createDirectory(backupFolder);
                    String datetime = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss").format(new Date());
                    Path backupFile = backupFolder.resolve(String.format("%s%s", datetime, JsonSaver.VR_AUTO_SAVE_FILE_EXTENSION));
                    Files.copy(outputFile, backupFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            loadedScheduler.autoSaveScheduler.shutdown();
        }
    }

    public static void startAutoSaveScheduler(List<SuperframeData> superframes, List<ImageData> images, LoadedReviewData loadedData) {
        loadedScheduler.set(new LoadedScheduler(superframes, images, loadedData));
    }

    public static void stopAutoSaveScheduler() {
        loadedScheduler.set(null);
    }

    private static class LoadedScheduler {
        private final ScheduledExecutorService autoSaveScheduler;
        private final List<SuperframeData> superframes;
        private final List<ImageData> images;
        private final LoadedReviewData loadedData;

        private LoadedScheduler(List<SuperframeData> superframes, List<ImageData> images, LoadedReviewData loadedData) {
            this.superframes = superframes;
            this.images = images;
            this.loadedData = loadedData;
            autoSaveScheduler = Executors.newSingleThreadScheduledExecutor();
        }
    }
}
