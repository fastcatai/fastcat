package detector;

import annotation.UIAnnotation;
import boundingbox.SimpleBoundingBoxV3;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import model.ImageData;
import modelV3.BoundingBox;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import settings.SettingsProperties;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DetectionServer {

    private final CloseableHttpClient client;

    public DetectionServer() {
        client = HttpClients.createDefault();
    }

    // TODO: test
    private void showServerUrlDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("No Server URL");
        alert.setHeaderText(null);
        alert.setContentText("No server URL was found or the URL has not the right format. Go to settings and check your detection server URL.");
        alert.showAndWait();
    }

    private void showNoConnectionDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("No Server Connection");
        alert.setHeaderText(null);
        alert.setContentText("Could not connect to the server. Check if server is running.");
        alert.showAndWait();
    }

    public HttpPost createHttpPostRequest() {
        String uri = SettingsProperties.getProperty(SettingsProperties.IMAGE_ANNOTATION_DETECTION_SERVER);
        if (uri != null) {
            try {
                return new HttpPost(uri);
            } catch (IllegalArgumentException e) {
                showServerUrlDialog();
                return null;
            }
        } else {
            showServerUrlDialog();
            return null;
        }
    }

    public String send(Path image) {
        return send(image, createHttpPostRequest());
    }

    public String send(Path image, HttpPost request) {
        if (request != null) {
            try {
                HttpEntity entity = MultipartEntityBuilder.create()
                        .addPart("image", new FileBody(image.toFile()))
                        .build();
                request.setEntity(entity);
                HttpResponse response = client.execute(request);
                return new String(response.getEntity().getContent().readAllBytes());
            } catch (IOException e) {
                showNoConnectionDialog();
            }
        }
        return null;
    }

    public List<SimpleBoundingBoxV3> sendImageAndPredictBoundingBoxes(ImageData imageData, Pane annotationPane) {
        List<SimpleBoundingBoxV3> listBoxes = new ArrayList<>();
        String bboxJson = send(imageData.getFile());
        if (bboxJson == null)
            return listBoxes;

        // parse JSON and create bounding boxes
        try {
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode boxes = (ArrayNode) mapper.readTree(bboxJson);
            for (JsonNode boxObj : boxes) {
                int x = boxObj.get("xmin").intValue();
                int y = boxObj.get("ymin").intValue();
                int width = boxObj.get("xmax").intValue() - x;
                int height = boxObj.get("ymax").intValue() - y;
                String label = boxObj.get("label").asText();
                double score = boxObj.get("score").asDouble();

                BoundingBox boundingBox = new BoundingBox(x, y, width, height, label, false, true);
//                BoundingBoxUI.convertToRelativeValues(boundingBox, annotationPane.getWidth(), annotationPane.getHeight(), imageData.getWidth(), imageData.getHeight());
                SimpleBoundingBoxV3 uiBoundingBox = new SimpleBoundingBoxV3(annotationPane, boundingBox, true, imageData.getWidth(), imageData.getHeight());
                uiBoundingBox.imported();
                listBoxes.add(uiBoundingBox);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return listBoxes;
    }

    public void singleDetection(ImageData imageData, ReadOnlyObjectProperty<ImageData> currentSelectedImageData,
                                Pane annotationPane) {
        Task<List<SimpleBoundingBoxV3>> singleDetectionTask = new Task<>() {
            @Override
            protected List<SimpleBoundingBoxV3> call() {
                return sendImageAndPredictBoundingBoxes(imageData, annotationPane);
            }
        };

        singleDetectionTask.setOnSucceeded(event -> {
            List<SimpleBoundingBoxV3> boxes = singleDetectionTask.getValue();
            for (SimpleBoundingBoxV3 box : boxes) {
                imageData.addAnnotation(box);

//                box.selectedProperty().addListener((observable, oldIsSelected, newIsSelected) -> {
//                    if (newIsSelected) currentSelectedAnnotation.set(box);
//                });
                if (currentSelectedImageData.get() == imageData) {
//                    box.imported();
                    box.addToPane();
                    box.setVisible(true);
                }
            }
        });

        singleDetectionTask.setOnCancelled(event -> System.err.println("Single detection task canceled"));
        singleDetectionTask.setOnFailed(event -> System.err.println("Single detection task failed"));

        // start task
        Thread t = new Thread(singleDetectionTask);
        t.setName("Single Detection Task");
        t.start();
    }

    //<editor-fold desc="Background Detection">

    private BackgroundDetection detectionTask;

    public ReadOnlyDoubleProperty startDetection(List<ImageData> images, ReadOnlyObjectProperty<ImageData> currentSelectedImageData,
                                                 ObjectProperty<UIAnnotation<?>> currentSelectedAnnotation, Pane annotationPane, BooleanProperty backgroundDetection) {
        detectionTask = new BackgroundDetection(images, currentSelectedImageData, currentSelectedAnnotation, annotationPane, backgroundDetection);
        Thread detectionThread = new Thread(detectionTask);
        detectionThread.setName("Background Detection Task");
        detectionThread.start();
        return detectionTask.progressProperty();
    }

    public void stopDetection() {
        if (detectionTask != null) {
            detectionTask.stopTask();
            detectionTask = null;
        }
    }

    private class BackgroundDetection extends Task<Void> {

        private final List<ImageData> images;
        private final ReadOnlyObjectProperty<ImageData> currentSelectedImageData;
        private final ObjectProperty<UIAnnotation<?>> currentSelectedAnnotation;
        private final Pane annotationPane;

        private final AtomicBoolean isDetectionRunning;

        private BackgroundDetection(List<ImageData> images, ReadOnlyObjectProperty<ImageData> currentSelectedImageData,
                                    ObjectProperty<UIAnnotation<?>> currentSelectedAnnotation, Pane annotationPane, BooleanProperty backgroundDetectionSelected) {
            this.images = images;
            this.currentSelectedImageData = currentSelectedImageData;
            this.currentSelectedAnnotation = currentSelectedAnnotation;
            this.annotationPane = annotationPane;
            isDetectionRunning = new AtomicBoolean(false);

            setOnSucceeded(event -> backgroundDetectionSelected.set(false));
            setOnCancelled(event -> System.err.println("Background detection task canceled"));
            setOnFailed(event -> System.err.println("Background detection task failed"));
        }

        public void stopTask() {
            if (isRunning()) {
                isDetectionRunning.set(false);
            }
        }

        @Override
        protected Void call() {
            isDetectionRunning.set(true);
            int max = images.size();
            for (int i = 0; i < max && isDetectionRunning.get(); i++) {
                ImageData data = images.get(i);
                List<SimpleBoundingBoxV3> boxes = sendImageAndPredictBoundingBoxes(data, annotationPane);
                for (SimpleBoundingBoxV3 box : boxes) {
                    Platform.runLater(() -> {
                        data.addAnnotation(box);
                        if (data == currentSelectedImageData.get()) {
                            box.addToPane();
                            box.setVisible(true);
                        }
                    });
//                    box.selectedProperty().addListener((observable, oldIsSelected, newIsSelected) -> {
//                        if (newIsSelected) currentSelectedAnnotation.set(box);
//                    });

                }
                updateProgress(i + 1, max);
            }
            return null;
        }
    }

    //</editor-fold>
}
