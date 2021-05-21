package controller;

import annotation.UIAnnotation;
import boundingbox.VideoBoundingBox;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import modelV3.BoundingBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import point.VideoPoint;
import ui.TimeTableCell;
import videoplayer.VideoPlayer;

public class TimeTableController {
    private static final Logger logger = LogManager.getLogger(TimeTableController.class);

    @FXML
    private TableView<UIAnnotation<?>> timeTable;
    @FXML
    private TableColumn<UIAnnotation<?>, String> columnTime;
    @FXML
    private TableColumn<UIAnnotation<?>, String> columnLabel;
    @FXML
    private TableColumn<UIAnnotation<?>, String> columnInstance;

    private final VideoAnnotationController vaController;

    public TimeTableController(VideoAnnotationController vaController) {
        this.vaController = vaController;
    }

    @FXML
    private void initialize() {
        columnTime.setCellFactory(cell -> new TimeTableCell());
        columnTime.setCellValueFactory(cell -> {
            UIAnnotation<?> a = cell.getValue();
            if (a instanceof VideoBoundingBox)
                return ((VideoBoundingBox) cell.getValue()).getAnnotationModel().shortTimestampStringProperty();
            if (a instanceof VideoPoint)
                return ((VideoPoint) cell.getValue()).getAnnotationModel().shortTimestampStringProperty();
            throw logger.throwing(new IllegalArgumentException(String.format("%s is not a supported annotation", a.getClass())));
        });
        columnLabel.setCellFactory(TextFieldTableCell.forTableColumn());
        columnLabel.setCellValueFactory(cell -> cell.getValue().getAnnotationModel().labelProperty());
        columnInstance.setCellFactory(TextFieldTableCell.forTableColumn());
        columnInstance.setCellValueFactory(cell -> cell.getValue().getAnnotationModel().instanceProperty());

        timeTable.getSelectionModel().selectedItemProperty().addListener(onSelectedItemChange);
    }

    private final ChangeListener<UIAnnotation<?>> onSelectedItemChange = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends UIAnnotation<?>> observable, UIAnnotation<?> oldItem, UIAnnotation<?> newItem) {
            VideoPlayer videoPlayer = vaController.getVideoPlayer();
            if (videoPlayer.isPlaying()) videoPlayer.pause();

            double startMillis;
            if (newItem instanceof VideoPoint)
                startMillis = ((VideoPoint) newItem).getAnnotationModel().getTimestampStartMillis();
            else if (newItem instanceof VideoBoundingBox)
                startMillis = ((VideoBoundingBox) newItem).getAnnotationModel().getTimestampStartMillis();
            else
                throw logger.throwing(new IllegalArgumentException(String.format("%s is not a supported annotation", newItem.getClass())));

            videoPlayer.jumpToTimestamp(startMillis);


            vaController.getAnnotationPane().getChildren().removeIf(node -> node instanceof Rectangle
                    && ((Rectangle) node).getStroke() == Color.BLUE);

            if (newItem instanceof VideoPoint) {
                VideoPoint p = (VideoPoint) newItem;
                BoundingBox bb = p.getAnnotationModel().getInferredBox();
                if (bb != null) {
                    BoundingBox inferredBox = new BoundingBox(bb.getX(), bb.getY(), bb.getWidth(), bb.getHeight(),
                            bb.getLabel(), bb.getInstance());

                    // convert to relative values
                    int absImgWidth = p.getOriginalImageWidth();
                    int absImgHeight = p.getOriginalImageHeight();
                    double paneWidth = vaController.getAnnotationPane().getWidth();
                    double paneHeight = vaController.getAnnotationPane().getHeight();
                    double relX = (inferredBox.getX() / absImgWidth) * paneWidth;
                    double relY = (inferredBox.getY() / absImgHeight) * paneHeight;
                    double relWidth = (inferredBox.getWidth() / absImgWidth) * paneWidth;
                    double relHeight = (inferredBox.getHeight() / absImgHeight) * paneHeight;

                    Rectangle rect = new Rectangle(relX, relY, relWidth, relHeight);
                    rect.setFill(Color.TRANSPARENT);
                    rect.setStroke(Color.BLUE);
                    vaController.getAnnotationPane().getChildren().add(rect);
                }
            }
        }
    };
}
