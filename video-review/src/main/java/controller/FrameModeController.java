package controller;

import annotation.UIAnnotation;
import frameloader.Frame;
import frameloader.FrameLoaderBase;
import frameloader.QueuedFrameLoader;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import keymap.model.ActionVR;
import model.FrameData;
import model.ImageData;
import model.SuperframeData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ui.Dialogs;
import ui.FrameViewer;
import videoreview.AnnotationPaneVR;
import videoreview.PDFViewer;
import videoreview.ViewMode;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class FrameModeController {
    private static final Logger logger = LogManager.getLogger(FrameModeController.class);

    @FXML
    private VBox root;
    @FXML
    private StackPane stackPaneViewer;
    @FXML
    private VBox imageViewerControls;
    @FXML
    private Label labelPreviousImage;
    @FXML
    private Label labelNextImage;
    @FXML
    private Label labelDoubleSkipBackwards;
    @FXML
    private Label labelDoubleSkipForwards;
    @FXML
    private Label labelTripleSkipBackwards;
    @FXML
    private Label labelTripleSkipForwards;
    @FXML
    public Label labelCurrentSuperframe;
    @FXML
    private Label labelCurrentFrameNumber;
    @FXML
    private TextField textFieldJumpToFrameNumber;
    @FXML
    private TextArea textAreaComment;
    @FXML
    private Label labelButtonSwitchToVideoMode;
    @FXML
    private GridPane gridPaneClassCounts;
    @FXML
    private CheckBox checkBoxAutoNext;
    @FXML
    private CheckBox checkBoxJumpSuperframes;
    @FXML
    private Button buttonReport;
    @FXML
    private Button buttonPathologyReport;

    // frame player overlay
    @FXML
    private VBox imageViewerFramePlayerMode;
    @FXML
    private Label labelFramePlayerFrameNumber;

    public enum FrameViewerStates {
        DEFAULT,
        FRAME_PLAYER
    }

    // controller
    private VideoReviewController vrController;
    // indicates if frame player is activated
    private final ObjectProperty<FrameViewerStates> currentFrameViewerState;
    // mapping for class counts
    private final Map<String, Label> mappingClassToCountLabel = new HashMap<>();
    // pane
    private AnnotationPaneVR annotationPane;

    public FrameModeController(VideoReviewController vrController) {
        this.vrController = vrController;

        // controls the bottom control view
        currentFrameViewerState = new SimpleObjectProperty<>(FrameViewerStates.DEFAULT);
        currentFrameViewerState.addListener(this::frameViewerStateChanged);
    }

    /**
     * Handles the state change between default view and frame player.
     */
    private void frameViewerStateChanged(ObservableValue<? extends FrameViewerStates> observable,
                                         FrameViewerStates oldState, FrameViewerStates newState) {
        if (oldState == FrameViewerStates.FRAME_PLAYER)
            labelFramePlayerFrameNumber.textProperty().unbind();

        if (newState == FrameViewerStates.DEFAULT) {
            imageViewerControls.toFront();
        } else if (newState == FrameViewerStates.FRAME_PLAYER) {
            imageViewerFramePlayerMode.toFront();
            labelFramePlayerFrameNumber.textProperty().bind(Bindings.createStringBinding(() ->
                            getFrameViewer().imageProperty().get() != null
                                    ? getFrameLoader().getCurrentFrame().getFrameNumberString() : "###",
                    getFrameViewer().imageProperty()));
        }
    }


    @FXML
    private void initialize() {
        stackPaneViewer.getChildren().add(getFrameViewer());
        stackPaneViewer.getChildren().add(getAnnotationPane());

        labelCurrentSuperframe.textProperty().bind(Bindings.createStringBinding(() -> {
            SuperframeData sf = vrController.getCurrentSuperframeData();
            if (sf != null) return Integer.toString(sf.getFrameNumber());
            return "N/A";
        }, vrController.currentSuperframeDataProperty()));

        labelCurrentFrameNumber.textProperty().bind(Bindings.createStringBinding(() -> {
            if (getFrameViewer().imageProperty().get() == null) return "###";
            return String.valueOf(getFrameLoader().getCurrentFrame().getFrameNumber());
        }, getFrameViewer().imageProperty()));

        // listener to display the total count of classes
        MapChangeListener<String, Integer> mapClassCountChangeListener = change -> {
            String key = change.getKey();
            if (change.wasRemoved()) {
                // only delete from grid pane when class does not exists anymore
                if (!vrController.getCurrentlySelectedSuperframe().getMappingClassToCount().containsKey(key)) {
                    Label removedCountLabel = mappingClassToCountLabel.remove(key);
                    for (Node l : gridPaneClassCounts.getChildren()) {
                        if (((Label) l).getText().equals(key)) {
                            gridPaneClassCounts.getChildren().remove(l);
                            gridPaneClassCounts.getChildren().remove(removedCountLabel);
                            break;
                        }
                    }
                }
            }
            if (change.wasAdded()) {
                if (mappingClassToCountLabel.containsKey(key))
                    mappingClassToCountLabel.get(key).setText(change.getValueAdded().toString());
                else addClassCountToGridPane(key, change.getValueAdded().toString());
            }

        };
        vrController.currentSuperframeDataProperty().addListener((observable, oldSuperframe, newSuperframe) -> {
            gridPaneClassCounts.getChildren().clear();
            mappingClassToCountLabel.clear();
            if (oldSuperframe != null) {
                oldSuperframe.getMappingClassToCount().removeListener(mapClassCountChangeListener);
            }
            if (newSuperframe != null) {
                // add already existing class counts
                for (String key : newSuperframe.getMappingClassToCount().keySet()) {
                    addClassCountToGridPane(key, newSuperframe.getMappingClassToCount().get(key).toString());
                }
                // add listener to react on changes
                newSuperframe.getMappingClassToCount().addListener(mapClassCountChangeListener);
            }
        });

        initAnnotationPane();
        initImageControls();

        buttonReport.setOnAction(this::onActionReportButtons);
        buttonPathologyReport.setOnAction(this::onActionReportButtons);
    }

    private void addClassCountToGridPane(String key, String count) {
        Label clazz = new Label(key);
        clazz.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
        Label classCount = new Label(count);
        gridPaneClassCounts.addRow(gridPaneClassCounts.getRowCount(), clazz, classCount);
        mappingClassToCountLabel.put(key, classCount);
    }

    private void initAnnotationPane() {
        // bind annotation pane to frame viewer
        getAnnotationPane().maxWidthProperty().bind(getAnnotationPane().prefWidthProperty());
        getAnnotationPane().maxHeightProperty().bind(getAnnotationPane().prefHeightProperty());
        getFrameViewer().boundsInParentProperty().addListener((observable, oldBounds, newBounds) ->
                getAnnotationPane().setPrefSize(newBounds.getWidth(), newBounds.getHeight()));

        getFrameLoader().currentFrameProperty().addListener(frameChangeListener);

        // add annotation selection listener when image data is added
        vrController.getListAnnotatedImagesController().getImageDataList().addListener((ListChangeListener<ImageData>) c -> {
            while (c.next()) {
                if (c.wasAdded())
                    c.getAddedSubList().forEach(imageData -> imageData.selectedAnnotationProperty().addListener(annotationSelectionChanged));
            }
        });
    }

    /**
     * Selects the annotation within the annotations list view
     */
    private final InvalidationListener annotationSelectionChanged = observable -> {
        UIAnnotation<?> selectedAnnotation = (UIAnnotation<?>) ((ReadOnlyObjectProperty<?>) observable).get();
        if (selectedAnnotation != null) {
            vrController.getListViewAnnotations().getSelectionModel().select(selectedAnnotation);
        }
    };

    /**
     * Remove old binding and add new bindings and annotations, if image data is available
     */
    private final ChangeListener<Frame> frameChangeListener = (observable, oldFrame, newFrame) -> {
        if (oldFrame != null) {
            getAnnotationPane().getChildren().clear();
            vrController.getListAnnotatedImagesController().getImageData(oldFrame.getFrameNumber()).ifPresent(imageData -> {
                imageData.removeAnnotationSelection();
                imageData.selectedAnnotationProperty().removeListener(annotationSelectionChanged);
            });
        }

        if (newFrame != null) {
            vrController.getListAnnotatedImagesController().getImageData(newFrame.getFrameNumber()).ifPresent(imageData -> {
                for (UIAnnotation<?> annotation : imageData.getAnnotations()) {
                    annotation.addToPane();
                }
                imageData.selectedAnnotationProperty().addListener(annotationSelectionChanged);
            });
        }
    };

    private void initImageControls() {
        checkBoxJumpSuperframes.setSelected(false);
        checkBoxJumpSuperframes.setDisable(false);
        checkBoxAutoNext.selectedProperty().addListener((observable, oldSelected, newSelected) ->
                checkBoxJumpSuperframes.setDisable(!newSelected));

        // add event listener to controls
        labelButtonSwitchToVideoMode.setOnMouseClicked(event -> {
            double millisecondsPerFrame = 1.0 / (vrController.getVideoPlayerMode().getVideoPlayer().getVideoInfo().getFPS().get() / 1000.0);
            vrController.getVideoPlayerMode().videoJumpToTimestamp((long) (getFrameLoader().getCurrentFrame().getFrameNumber() * millisecondsPerFrame));
            vrController.setCurrentViewMode(ViewMode.VIDEO);
        });
        labelPreviousImage.setOnMouseClicked(event -> getFrameLoader().previousFrame());
        labelNextImage.setOnMouseClicked(event -> getFrameLoader().nextFrame());
        labelDoubleSkipForwards.setOnMouseClicked(event -> vrController.getMenuBarController().fireAction(ActionVR.SKIP_IMAGE_FORWARD_DOUBLE));
        labelDoubleSkipBackwards.setOnMouseClicked(event -> vrController.getMenuBarController().fireAction(ActionVR.SKIP_IMAGE_BACKWARDS_DOUBLE));
        labelTripleSkipForwards.setOnMouseClicked(event -> vrController.getMenuBarController().fireAction(ActionVR.SKIP_IMAGE_FORWARD_TRIPLE));
        labelTripleSkipBackwards.setOnMouseClicked(event -> vrController.getMenuBarController().fireAction(ActionVR.SKIP_IMAGE_BACKWARDS_TRIPLE));

        // only allow frame number > 0
        textFieldJumpToFrameNumber.setTextFormatter(new TextFormatter<Integer>(change -> {
            String newText = change.getControlNewText();
            if (newText.isBlank()) return change;
            try {
                if (Integer.parseInt(newText) <= 0) return null;
            } catch (NumberFormatException e) {
                return null;
            }
            return change;
        }));
        textFieldJumpToFrameNumber.setOnAction(event -> {
            getFrameLoader().jumpToFrame(Integer.parseInt(textFieldJumpToFrameNumber.getText().strip()));
            textFieldJumpToFrameNumber.getParent().requestFocus();
        });

        // comment text area, show comment and enable text area on frame change
        textAreaComment.setDisable(true);
        getFrameLoader().currentFrameProperty().addListener((observable, oldFrame, newFrame) -> {
            if (oldFrame != null) {
                vrController.getCurrentlySelectedSuperframe().getFrame(oldFrame.getFrameNumber()).ifPresent(frameData ->
                        textAreaComment.textProperty().unbindBidirectional(frameData.commentProperty()));
            }
            textAreaComment.setDisable(true);
            textAreaComment.setText(null);
            if (newFrame != null) {
                vrController.getCurrentlySelectedSuperframe().getFrame(newFrame.getFrameNumber()).ifPresent(frameData -> {
                    textAreaComment.textProperty().bindBidirectional(frameData.commentProperty());
                    textAreaComment.setDisable(false);
                });
            }
        });

        // comment text area, enable/disable and bind text area when frame data is added/removed
        ListChangeListener<? super FrameData> listChangeListenerFrameData = c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    vrController.getCurrentlySelectedSuperframe().getFrame(getFrameLoader().getCurrentFrame().getFrameNumber()).ifPresent(frameData -> {
                        textAreaComment.textProperty().bindBidirectional(frameData.commentProperty());
                        textAreaComment.setDisable(false);
                    });
                }
                if (c.wasRemoved()) {
                    if (c.getRemovedSize() == 1) {
                        textAreaComment.textProperty().unbindBidirectional(c.getRemoved().get(0).commentProperty());
                        textAreaComment.setDisable(true);
                    }
                }
            }
        };
        vrController.currentSuperframeDataProperty().addListener((observable, oldSuperframe, newSuperframe) -> {
            if (oldSuperframe != null) oldSuperframe.getChildFrames().removeListener(listChangeListenerFrameData);
            if (newSuperframe != null) newSuperframe.getChildFrames().addListener(listChangeListenerFrameData);
        });
    }

    private void onActionReportButtons(ActionEvent event) {
        final String fileExtension;
        final Alert dialog;
        if (event.getSource() == buttonReport) {
            fileExtension = ".report.pdf";
            dialog = Dialogs.createInformation("Report Not Found", null, "No report with the extension *.pathology.pdf was found");
        } else if (event.getSource() == buttonPathologyReport) {
            fileExtension = ".pathology.pdf";
            dialog = Dialogs.createInformation("Report Not Found", null, "No report with the extension *.report.pdf was found");
        } else return;

        try {
            Files.list(vrController.getCurrentlyLoadedData().getVideoFile().getParent())
                    .filter(path -> path.getFileName().toString().endsWith(fileExtension))
                    .findFirst().ifPresentOrElse(PDFViewer::openPdfWindow, dialog::show);
        } catch (IOException e) {
            logger.throwing(e);
        }
    }

    public void stop() {
        System.out.println("FrameModeController STOP");
        if (getFrameLoader() instanceof QueuedFrameLoader)
            ((QueuedFrameLoader) getFrameLoader()).stopThreads();

        PDFViewer.closeAllWindows();
    }

    public AnnotationPaneVR getAnnotationPane() {
        if (annotationPane == null)
            annotationPane = new AnnotationPaneVR(this, vrController);
        return annotationPane;
    }

    private FrameViewer frameViewer;

    public FrameViewer getFrameViewer() {
        if (frameViewer == null)
            frameViewer = new FrameViewer();
        return frameViewer;
    }

    private FrameLoaderBase frameLoader;

    public FrameLoaderBase getFrameLoader() {
        if (frameLoader == null)
            frameLoader = new QueuedFrameLoader(getFrameViewer().imageProperty());
        return frameLoader;
    }

    public void setCurrentFrameViewerState(FrameViewerStates currentFrameViewerState) {
        this.currentFrameViewerState.set(currentFrameViewerState);
    }

    public boolean isCheckBoxAutoNextSelected() {
        return checkBoxAutoNext.isSelected();
    }

    public boolean isCheckBoxJumpSuperframesSelected() {
        return checkBoxJumpSuperframes.isSelected();
    }

    public VideoReviewController getVrController() {
        return vrController;
    }

    public TextField getTextFieldJumpToFrameNumber() {
        return textFieldJumpToFrameNumber;
    }

    public TextArea getTextAreaComment() {
        return textAreaComment;
    }
}
