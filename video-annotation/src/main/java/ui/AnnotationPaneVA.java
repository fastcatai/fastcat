package ui;

import boundingbox.UIBoundingBox;
import boundingbox.VideoBoundingBox;
import controller.VideoAnnotationController;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import modelV3.Annotation;
import modelV3.BoundingBox;
import modelV3.BoundingBoxVideo;
import modelV3.PointVideo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opencv.core.Mat;
import point.VideoPoint;
import util.PointToBoxConverter;
import util.VideoAnnotationChoice;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AnnotationPaneVA extends Pane implements EventHandler<MouseEvent>, Runnable {
    private static final Logger logger = LogManager.getLogger(AnnotationPaneVA.class);

    private final ScheduledExecutorService executorService;
    private final VideoAnnotationController vaController;

    public AnnotationPaneVA(VideoAnnotationController controller) {
        vaController = controller;

        setOnMousePressed(this);
        setOnMouseMoved(this);

        executorService = Executors.newSingleThreadScheduledExecutor();
        vaController.getVideoPlayer().setOnMediaChanged(mediaPath -> {
            if (!executorService.isShutdown() || !executorService.isTerminated())
                executorService.scheduleAtFixedRate(this, 0, 33, TimeUnit.MILLISECONDS);
        });
    }

    @Override
    public void handle(MouseEvent event) {
        VideoAnnotationChoice annotationChoice = vaController.getChosenAnnotation();
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            if (!event.isPrimaryButtonDown() || event.getX() < 0 || event.getY() < 0 || event.getX() > getMaxWidth() || event.getY() > getMaxHeight())
                return;

            String label = vaController.getDefaultAnnotationLabel();
            String instance = vaController.getDefaultAnnotationInstance();
            int videoWidth = vaController.getVideoPlayer().getVideoInfo().getResolutionWidth().get();
            int videoHeight = vaController.getVideoPlayer().getVideoInfo().getResolutionHeight().get();

            if (annotationChoice == VideoAnnotationChoice.POINT) {
                PointVideo pv = new PointVideo(event.getX(), event.getY(), 10, label, instance);
                pv.setTimestampStart(vaController.getVideoPlayer().currentTimeMillisProperty().get());
                pv.setTimestampEnd(vaController.getVideoPlayer().currentTimeMillisProperty().get());
                VideoPoint uiPoint = new VideoPoint(this, pv, videoWidth, videoHeight);
                uiPoint.addToPane();
                vaController.getTimeTable().getItems().add(uiPoint);
                inferBoundingBox(uiPoint);

            } else if (annotationChoice == VideoAnnotationChoice.BOX_CLICK_DRAG) {
                BoundingBoxVideo bb = new BoundingBoxVideo(event.getX(), event.getY(), 0, 0, label, instance);
                bb.setTimestampStart(vaController.getVideoPlayer().currentTimeMillisProperty().get());
                VideoBoundingBox uiBox = new VideoBoundingBox(this, bb, videoWidth, videoHeight);
                uiBox.setOnCreationFinished(uiAnnotation ->
                        ((BoundingBoxVideo) uiAnnotation.getAnnotationModel()).setTimestampEnd(
                                vaController.getVideoPlayer().currentTimeMillisProperty().get()));
                uiBox.startCreation(UIBoundingBox.CreationType.CLICK_DRAG);
                vaController.getTimeTable().getItems().add(uiBox);

            } else if (annotationChoice == VideoAnnotationChoice.BOX_CLICK_CLICK) {
                BoundingBoxVideo bb = new BoundingBoxVideo(event.getX(), event.getY(), 0, 0, label, instance);
                bb.setTimestampStart(vaController.getVideoPlayer().currentTimeMillisProperty().get());
                VideoBoundingBox uiBox = new VideoBoundingBox(this, bb, videoWidth, videoHeight);
                uiBox.setOnCreationFinished(uiAnnotation ->
                        ((BoundingBoxVideo) uiAnnotation.getAnnotationModel()).setTimestampEnd(
                                vaController.getVideoPlayer().currentTimeMillisProperty().get()));
                uiBox.startCreation(UIBoundingBox.CreationType.CLICK_CLICK);
                vaController.getTimeTable().getItems().add(uiBox);

            } else {
                logger.error(String.format("Choice '%s' is not supported for video annotation", annotationChoice.name()));
            }

        } else if (event.getEventType() == MouseEvent.MOUSE_MOVED) {
            if (event.getX() < 0 || event.getY() < 0 || event.getX() > getMaxWidth() || event.getY() > getMaxHeight())
                setCursor(Cursor.DEFAULT);
            else setCursor(Cursor.CROSSHAIR);
        }
    }

    private BoundingBox inferBoundingBox(VideoPoint point) {
        WritableImage snapshot = vaController.getVideoPlayer().getVideoPane().snapshot(null, null);
        Mat img = PointToBoxConverter.convertToMat(snapshot);
        Rectangle rect = PointToBoxConverter.getBoundingBox(img, (int) point.getX(), (int) point.getY(), (int) point.getRadius());

        BoundingBox inferredBox = null;
        if (rect != null) {
            double absX = (rect.getX() / point.getPane().getWidth()) * point.getOriginalImageWidth();
            double absY = (rect.getY() / point.getPane().getHeight()) * point.getOriginalImageHeight();
            double absWidth = (rect.getWidth() / point.getPane().getWidth()) * point.getOriginalImageWidth();
            double absHeight = (rect.getHeight() / point.getPane().getHeight()) * point.getOriginalImageHeight();
            inferredBox = new BoundingBox(absX, absY, absWidth, absHeight,
                    point.getAnnotationModel().getLabel(), point.getAnnotationModel().getInstance(), false, true);
            point.getAnnotationModel().setInferredBox(inferredBox);
        }
        if (inferredBox != null)
            return new BoundingBox(inferredBox.getX(), inferredBox.getY(), inferredBox.getWidth(), inferredBox.getHeight(),
                    inferredBox.getLabel(), inferredBox.getInstance(), inferredBox.isVerified(), inferredBox.isAutoCreated());
        else return null;
    }

    /**
     * Display bounding boxes during playback.
     */
    @Override
    public void run() {
        // TODO: is playing event to start and wait when playback is paused

        final double time = vaController.getVideoPlayer().currentTimeMillisProperty().get();

        vaController.getTimeTable().getItems()
                .filtered(uiAnnotation -> {
                    Annotation annotation = uiAnnotation.getAnnotationModel();
                    double start, end;
                    if (annotation instanceof BoundingBoxVideo) {
                        BoundingBoxVideo bbv = (BoundingBoxVideo) uiAnnotation.getAnnotationModel();
                        start = bbv.getTimestampStartMillis();
                        end = bbv.getTimestampEndMillis();
                    } else if (annotation instanceof PointVideo) {
                        PointVideo pv = (PointVideo) uiAnnotation.getAnnotationModel();
                        start = pv.getTimestampStartMillis();
                        end = pv.getTimestampEndMillis();
                    } else {
                        throw logger.throwing(new IllegalArgumentException(String.format("%s is not a supported annotation", annotation.getClass())));
                    }
                    return end > 0 && (time < start || time > end);
                })
                .forEach(annotationUI -> annotationUI.setVisible(false));

        vaController.getTimeTable().getItems()
                .filtered(uiAnnotation -> {
                    Annotation annotation = uiAnnotation.getAnnotationModel();
                    double start, end;
                    if (annotation instanceof BoundingBoxVideo) {
                        BoundingBoxVideo bbv = (BoundingBoxVideo) uiAnnotation.getAnnotationModel();
                        start = bbv.getTimestampStartMillis();
                        end = bbv.getTimestampEndMillis();
                    } else if (annotation instanceof PointVideo) {
                        PointVideo pv = (PointVideo) uiAnnotation.getAnnotationModel();
                        start = pv.getTimestampStartMillis();
                        end = pv.getTimestampEndMillis();
                    } else {
                        throw logger.throwing(new IllegalArgumentException(String.format("%s is not a supported annotation", annotation.getClass())));
                    }
                    return time >= start && time <= end;
                })
                .forEach(annotationUI -> annotationUI.setVisible(true));
    }

    public void close() {
        executorService.shutdown();
    }
}
