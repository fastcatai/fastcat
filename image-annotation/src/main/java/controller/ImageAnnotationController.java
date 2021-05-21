package controller;

import annotation.UIAnnotation;
import boundingbox.SimpleBoundingBoxV3;
import boundingbox.UIBoundingBox;
import detector.DetectionServer;
import detector.DockerContainer;
import handler.AutoSaveIA;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.StringConverter;
import listviewcells.ListCellImages;
import listviewcells.ListCellSecondaryLabel;
import model.ImageData;
import model.LoadedAnnotationData;
import org.opencv.core.Core;
import settings.SettingsProperties;
import tracking.TrackerTypes;
import tracking.TrackingHandler;
import ui.AnnotationPane;
import ui.FrameViewer;
import util.BaseController;
import util.ImageAnnotationChoice;
import util.ImageAnnotationView;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageAnnotationController implements BaseController {

    static {
        // OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @FXML
    private MenuBar menuBar;
    @FXML
    private IAMenuBarController menuBarController;

    // Navigation section
    @FXML
    private ListView<ImageData> listViewImages;
    @FXML
    private TextField textFieldJumpImage;
    @FXML
    private Spinner<Integer> spinnerAutoNextSeconds;
    @FXML
    private ToggleButton toggleAutoNext;
    @FXML
    private CheckBox checkBoxAutoNext;
    @FXML
    private Label labelFileCount;
    @FXML
    private Label labelImageWidth;
    @FXML
    private Label labelImageHeight;

    //========================================
    // Annotation
    //========================================
    @FXML
    private Button buttonDeleteAnnotations;

    // Annotation Tab
    @FXML
    private Label labelFilename;
    @FXML
    private ChoiceBox<ImageAnnotationChoice> choiceBoxAnnotations;
    @FXML
    private TextField textFieldDefaultAnnotationLabel;
    @FXML
    private ListViewAnnotations listViewAnnotations;

    // Annotation extras
    @FXML
    private CheckBox checkBoxAutoCreated;
    @FXML
    private CheckBox checkBoxVerified;
    @FXML
    private TextField textFieldInstance;
    @FXML
    private TextField textFieldSecondaryAnnotationLabel;
    @FXML
    private ListView<String> listViewSecondaryAnnotationLabels;

    // Image tab section
    @FXML
    private TextField textFieldImageLabel;
    @FXML
    private TextField textFieldSecondaryImageLabel;
    @FXML
    private ListView<String> listViewSecondaryImageLabels;
    @FXML
    private TextField textFieldDefaultImageLabel;

    // Detection tab
    @FXML
    private Circle circleStatusDetection;
    @FXML
    private Button buttonSingleDetect;
    @FXML
    private ToggleButton toggleStartStopDocker;
    @FXML
    private ToggleButton toggleBackgroundDetection;
    @FXML
    private ProgressBar progressBackgroundDetection;

    // Tracker tab
    @FXML
    private Circle circleStatusTracker;
    @FXML
    private ChoiceBox<TrackerTypes> choiceBoxTracker;
    @FXML
    private ToggleButton toggleTrack;
    @FXML
    private CheckBox checkBoxOneTimeTrack;
    @FXML
    private CheckBox checkBoxSizeChangedInit;

    // Copy tab
    @FXML
    private Button buttonCopyNextNImages;
    @FXML
    private Spinner<Integer> spinnerCopyNumber;


    @FXML
    private HBox buttonPrevImage;
    @FXML
    private HBox buttonNextImage;

    @FXML
    private StackPane stackPaneImageHolder;

    @FXML
    private Label labelLoadedImageFolderPath;

    private Pane annotationPane;
    private FrameViewer frameViewer;

    private final ObjectProperty<LoadedAnnotationData> currentlyLoadedData;
    // Auto next scheduler
    private ScheduledExecutorService autoNextSchedulerService;
    // Docker
    private final ObjectProperty<Boolean> dockerContainerRunning;
    private final DetectionServer detectionServer;
    // Tracking
    private TrackingHandler tracker;

    private final ImageAnnotationView imageAnnotationView;

    public ImageAnnotationController(ImageAnnotationView imageAnnotationView) {
        this.imageAnnotationView = imageAnnotationView;

        currentlyLoadedData = new SimpleObjectProperty<>();
//        currentSelectedAnnotation = new SimpleObjectProperty<>();

        detectionServer = new DetectionServer();
        dockerContainerRunning = new SimpleObjectProperty<>(false);
        dockerContainerRunning.addListener((observable, oldValue, newValue) -> {
            // true = running, false = stopped, null = in process
            if (newValue == null) {
                circleStatusDetection.setFill(Color.ORANGE);
                if (oldValue == null || !oldValue) toggleStartStopDocker.setText("Starting...");
                else toggleStartStopDocker.setText("Stopping...");
            } else if (newValue) {
                circleStatusDetection.setFill(Color.GREEN);
                toggleStartStopDocker.setText("Stop Docker");
            } else {
                circleStatusDetection.setFill(Color.RED);
                toggleStartStopDocker.setText("Start Docker");
            }
        });
    }

    @FXML
    public void initialize() {
        frameViewer = new FrameViewer();
        stackPaneImageHolder.getChildren().add(frameViewer);
        annotationPane = new AnnotationPane(this);
        stackPaneImageHolder.getChildren().add(annotationPane);

        initNavigationControls();
        initAnnotationControls();
        initCopyControls();
        initTrackerControls();
        initDetectionControls();

        // bind annotation pane to frame viewer
        annotationPane.maxWidthProperty().bind(annotationPane.prefWidthProperty());
        annotationPane.maxHeightProperty().bind(annotationPane.prefHeightProperty());
        frameViewer.boundsInParentProperty().addListener((observable, oldBounds, newBounds) ->
                annotationPane.setPrefSize(newBounds.getWidth(), newBounds.getHeight()));

        // bind frame viewer to image of selected item
        frameViewer.imageProperty().bind(Bindings.createObjectBinding(() -> {
            ImageData data = getCurrentSelectedImageData();
            annotationPane.setVisible(data != null);
            if (data != null) return new Image(Files.newInputStream(data.getFile()));
            else return new Image(getClass().getResourceAsStream("/logo_polygon.png"));
        }, listViewImages.getSelectionModel().selectedItemProperty()));

        getListViewImages().getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            currentSelectedAnnotation.unbind();
            if (newValue != null) currentSelectedAnnotation.bind(newValue.selectedAnnotationProperty());
        });


        String savedDefaultLabel = SettingsProperties.getProperty(SettingsProperties.IMAGE_ANNOTATION_DEFAULT_LABEL);
        textFieldDefaultAnnotationLabel.setText(savedDefaultLabel);
        textFieldDefaultAnnotationLabel.setOnAction(event -> {
            SettingsProperties.setProperty(SettingsProperties.IMAGE_ANNOTATION_DEFAULT_LABEL,
                    textFieldDefaultAnnotationLabel.getText().strip());
            SettingsProperties.saveProperty();
        });

        String trackingOption = SettingsProperties.getProperty(SettingsProperties.IMAGE_ANNOTATION_TRACKING_CHECKED_OPTION);
        if (trackingOption.equals("oneTime")) checkBoxOneTimeTrack.setSelected(true);
        else if (trackingOption.equals("sizeChanged")) checkBoxSizeChangedInit.setSelected(true);
        checkBoxOneTimeTrack.selectedProperty().addListener((observable, oldValue, newValue) -> {
            SettingsProperties.setProperty(SettingsProperties.IMAGE_ANNOTATION_TRACKING_CHECKED_OPTION,
                    newValue ? "oneTime" : "");
            SettingsProperties.saveProperty();
        });
        checkBoxSizeChangedInit.selectedProperty().addListener((observable, oldValue, newValue) -> {
            SettingsProperties.setProperty(SettingsProperties.IMAGE_ANNOTATION_TRACKING_CHECKED_OPTION,
                    newValue ? "sizeChanged" : "");
            SettingsProperties.saveProperty();
        });

        menuBarController.imageAnnotationControllerInitialize();
    }

    //<editor-fold desc="Initialization of controls">

    private void initNavigationControls() {
        // select next or previous item within list view
        buttonNextImage.setOnMouseClicked(event -> menuBarController.selectNextImage());
        buttonPrevImage.setOnMouseClicked(event -> menuBarController.selectPreviousImage());

        // set custom list cell
        listViewImages.setCellFactory(new ListCellImages());
        listViewImages.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // bind size of list view to file count label
        labelFileCount.textProperty().bind(Bindings.createStringBinding(() -> {
            if (currentlyLoadedData.get() == null) return "0";
            return String.valueOf(currentlyLoadedData.get().getImageDataList().size());
        }, currentlyLoadedData));

        // bind image width of selected item
        labelImageWidth.textProperty().bind(Bindings.createStringBinding(() -> {
            ImageData data = getCurrentSelectedImageData();
            if (data != null) return String.valueOf(data.getWidth());
            return "0";
        }, listViewImages.getSelectionModel().selectedItemProperty()));

        // bind image height of selected item
        labelImageHeight.textProperty().bind(Bindings.createStringBinding(() -> {
            ImageData data = getCurrentSelectedImageData();
            if (data != null) return String.valueOf(data.getHeight());
            return "0";
        }, listViewImages.getSelectionModel().selectedItemProperty()));

        // clear current selected annotation
//        listViewImages.getSelectionModel().selectedItemProperty().addListener((observable, oldItem, newItem) -> {
//            if (getCurrentSelectedImageData() != null) getCurrentSelectedImageData().removeAnnotationSelection();
//        });

        // define action when enter is pressed for jumping
        textFieldJumpImage.setOnAction(actionEvent -> {
            listViewImages.getItems().stream()
                    .filter(imageData -> {
                        String filename = imageData.getFilename();
                        filename = filename.substring(0, imageData.getFilename().lastIndexOf('.')).toLowerCase().trim();
                        return textFieldJumpImage.getText().toLowerCase().trim().equals(filename);
                    })
                    .findFirst()
                    .ifPresent(imageData -> {
                        listViewImages.getSelectionModel().select(imageData);
                        listViewImages.scrollTo(listViewImages.getSelectionModel().getSelectedIndex() - 2);
                    });
            textFieldJumpImage.clear();
        });

        // configure spinner for auto next
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, 2, 1);
        valueFactory.setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer object) {
                return object.toString() + " sec";
            }

            @Override
            public Integer fromString(String string) {
                String valueWithoutUnit = string.replaceAll("sec", "").trim();
                if (valueWithoutUnit.isEmpty()) return 0;
                return Integer.valueOf(valueWithoutUnit);
            }
        });
        spinnerAutoNextSeconds.setValueFactory(valueFactory);

        toggleAutoNext.selectedProperty().addListener((observable, oldIsSelected, newIsSelected) -> {
            if (newIsSelected) {
                autoNextSchedulerService = Executors.newSingleThreadScheduledExecutor();
                autoNextSchedulerService.scheduleWithFixedDelay(() -> Platform.runLater(() -> {
                            menuBarController.selectNextImage();
                            // shutdown if end is reached
                            if (listViewImages.getSelectionModel().getSelectedIndex() == listViewImages.getItems().size() - 1) {
                                autoNextSchedulerService.shutdown();
                                toggleAutoNext.setSelected(false);
                            }
                        }),
                        spinnerAutoNextSeconds.getValue(), spinnerAutoNextSeconds.getValue(), TimeUnit.SECONDS);
                toggleAutoNext.setText("Clear");
            } else {
                if (autoNextSchedulerService != null && !autoNextSchedulerService.isShutdown())
                    autoNextSchedulerService.shutdown();
                toggleAutoNext.setText("Auto Next");
            }
        });
    }

    private void initAnnotationControls() {
        // bind delete annotation button to image selection count
        buttonDeleteAnnotations.textProperty().bind(Bindings.createStringBinding(() -> {
            List<Integer> selectedIndices = listViewImages.getSelectionModel().getSelectedIndices();
            if (selectedIndices == null || selectedIndices.size() == 0)
                return "Delete";
            if (selectedIndices.size() == 1) {
                ImageData imageData = listViewImages.getSelectionModel().getSelectedItem();
                if (imageData != null) return "Delete from " + imageData.getFilename();
            }
            return "Delete Multiple";
        }, listViewImages.getSelectionModel().getSelectedIndices()));

        // disable when no image is selected
        buttonDeleteAnnotations.disableProperty().bind(Bindings.size(listViewImages.getSelectionModel().getSelectedIndices()).lessThanOrEqualTo(0));

        // delete annotation button action
        buttonDeleteAnnotations.setOnAction(event -> {
            List<ImageData> selectedImages = listViewImages.getSelectionModel().getSelectedItems();
            if (selectedImages != null && !selectedImages.isEmpty()) {
                // iterate over all selected images
                for (ImageData image : selectedImages) {
                    List<UIAnnotation<?>> items = new ArrayList<>(image.getAnnotations());
                    // iterate over all annotations within image
                    for (UIAnnotation<?> annotation : items) {
                        annotation.removeFromPane();
                        image.removeAnnotation(annotation);
                    }
                }
                if (checkBoxAutoNext.isSelected()) {
                    menuBarController.selectNextImage();
                } else {
                    int nextIndex = listViewImages.getSelectionModel().getSelectedIndex();
                    listViewImages.getSelectionModel().clearAndSelect(nextIndex);
                    listViewImages.scrollTo(nextIndex - 2);
                }
            }
        });

        choiceBoxAnnotations.getItems().setAll(ImageAnnotationChoice.values());
        choiceBoxAnnotations.getSelectionModel().select(ImageAnnotationChoice.BOX_CLICK_DRAG);

        // bind image filename of selected item
        labelFilename.textProperty().bind(Bindings.createStringBinding(() -> {
            ImageData data = getCurrentSelectedImageData();
            if (data != null) return File.separatorChar + data.getFilename();
            return "";
        }, listViewImages.getSelectionModel().selectedItemProperty()));

        // ##############################
        // extra annotation controls
        // ##############################

        checkBoxAutoCreated.disableProperty().bind(currentSelectedAnnotation.isNull());
        checkBoxVerified.disableProperty().bind(currentSelectedAnnotation.isNull());
        textFieldInstance.disableProperty().bind(currentSelectedAnnotation.isNull());
        textFieldSecondaryAnnotationLabel.disableProperty().bind(currentSelectedAnnotation.isNull());
        listViewSecondaryAnnotationLabels.disableProperty().bind(currentSelectedAnnotation.isNull());

        listViewSecondaryAnnotationLabels.setCellFactory(new ListCellSecondaryLabel());
        // bind list view to additional annotations labels
        listViewSecondaryAnnotationLabels.itemsProperty().bind(Bindings.createObjectBinding(() -> {
            ImageData data = getCurrentSelectedImageData();
            if (data != null) {
                UIAnnotation<?> uiAnnotation = currentSelectedAnnotation.get();
                if (uiAnnotation != null)
                    return uiAnnotation.getAnnotationModel().getAdditionalLabels();
            }
            return null;
        }, currentSelectedAnnotation));

        textFieldSecondaryAnnotationLabel.setOnAction(event -> {
            ImageData data = getCurrentSelectedImageData();
            if (data != null) {
                String label = textFieldSecondaryAnnotationLabel.getText().strip();
                if (!currentSelectedAnnotation.get().getAnnotationModel().getAdditionalLabels().contains(label))
                    currentSelectedAnnotation.get().getAnnotationModel().addAdditionalLabel(label);
                textFieldSecondaryAnnotationLabel.clear();
            }
        });

        // ##############################
        // image annotation control
        // ##############################

        // bind image classification label to text field when a new image is selected
        listViewImages.getSelectionModel().selectedItemProperty().addListener((observable, oldItem, newItem) -> {
            if (oldItem != null)
                oldItem.getImageClassification().labelProperty().unbind();
            if (newItem != null) {
                textFieldImageLabel.clear();
                textFieldImageLabel.setText(newItem.getImageClassification().getLabel());
                newItem.getImageClassification().labelProperty().bind(Bindings.createStringBinding(
                        () -> textFieldImageLabel.getText().strip(),
                        textFieldImageLabel.textProperty()));
            }
        });

        listViewSecondaryImageLabels.setCellFactory(new ListCellSecondaryLabel());
        // bind list view to additional image labels
        listViewSecondaryImageLabels.itemsProperty().bind(Bindings.createObjectBinding(() -> {
            ImageData data = getCurrentSelectedImageData();
            if (data != null) return data.getImageClassification().getAdditionalLabels();
            return null;
        }, listViewImages.getSelectionModel().selectedItemProperty()));

        // add new secondary label to list view on ENTER
        textFieldSecondaryImageLabel.setOnAction(event -> {
            ImageData data = getCurrentSelectedImageData();
            if (data != null) {
                String label = textFieldSecondaryImageLabel.getText().strip();
                if (!data.getImageClassification().getAdditionalLabels().contains(label))
                    data.getImageClassification().addAdditionalLabel(label);
                textFieldSecondaryImageLabel.clear();
            }
        });
    }

    private void initDetectionControls() {
        buttonSingleDetect.disableProperty().bind(listViewImages.getSelectionModel().selectedItemProperty().isNull()
                .or(dockerContainerRunning.isEqualTo(Boolean.FALSE)).or(dockerContainerRunning.isNull()));
        toggleBackgroundDetection.disableProperty().bind(listViewImages.getSelectionModel().selectedItemProperty().isNull()
                .or(dockerContainerRunning.isEqualTo(Boolean.FALSE)).or(dockerContainerRunning.isNull()));

        buttonSingleDetect.setOnMouseClicked(event -> menuBarController.singleDetection());

        DockerContainer dockerContainer = new DockerContainer(dockerContainerRunning, toggleStartStopDocker);
        toggleStartStopDocker.selectedProperty().addListener((observable, oldIsSelected, newIsSelected) -> {
            if (newIsSelected) dockerContainer.startContainer();
            else dockerContainer.stopContainer();
        });

        toggleBackgroundDetection.selectedProperty().addListener((observable, oldIsSelected, newIsSelected) -> {
            if (newIsSelected != null) {
                if (newIsSelected) {
                    // start background detection for all following images, including the selected one
                    int selectedIndex = listViewImages.getSelectionModel().getSelectedIndex();
                    int length = listViewImages.getItems().size();
                    List<ImageData> list = listViewImages.getItems().subList(selectedIndex, length);
                    ReadOnlyDoubleProperty backgroundDetectionProgress = detectionServer.startDetection(list,
                            listViewImages.getSelectionModel().selectedItemProperty(), currentSelectedAnnotation,
                            annotationPane, toggleBackgroundDetection.selectedProperty());
                    progressBackgroundDetection.progressProperty().bind(backgroundDetectionProgress);
                    toggleBackgroundDetection.setText("Stop");

                } else {
                    detectionServer.stopDetection();
                    progressBackgroundDetection.progressProperty().unbind();
                    progressBackgroundDetection.setProgress(0);
                    toggleBackgroundDetection.setText("Pre-Detection");
                }
            }
        });
    }

    private void initTrackerControls() {
        listViewImages.mouseTransparentProperty().bind(toggleTrack.selectedProperty());
        listViewImages.focusTraversableProperty().bind(toggleTrack.selectedProperty().not());
        listViewAnnotations.mouseTransparentProperty().bind(toggleTrack.selectedProperty());
        listViewAnnotations.focusTraversableProperty().bind(toggleTrack.selectedProperty().not());
        buttonPrevImage.disableProperty().bind(toggleTrack.selectedProperty());
        // pickOnBounds: no new boxes can be created but existing box can be moved and resized
        annotationPane.pickOnBoundsProperty().bind(toggleTrack.selectedProperty().not());
        annotationPane.focusTraversableProperty().bind(toggleTrack.selectedProperty().not());

        // set tracker choices
        choiceBoxTracker.getItems().setAll(TrackerTypes.values());
        choiceBoxTracker.getSelectionModel().select(TrackerTypes.CSRT);

        toggleTrack.disableProperty().bind(listViewAnnotations.getSelectionModel().selectedItemProperty().isNull()
                .and(toggleTrack.selectedProperty().not()));
        choiceBoxTracker.disableProperty().bind(toggleTrack.selectedProperty()
                .and(checkBoxOneTimeTrack.selectedProperty().not()));

        // disable check boxes when tracking begins
        checkBoxOneTimeTrack.disableProperty().bind(toggleTrack.selectedProperty());
        checkBoxSizeChangedInit.disableProperty().bind(toggleTrack.selectedProperty());
        // only allow a single selection out of both check boxes
        checkBoxOneTimeTrack.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && checkBoxSizeChangedInit.isSelected())
                checkBoxSizeChangedInit.setSelected(false);
        });
        checkBoxSizeChangedInit.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && checkBoxOneTimeTrack.isSelected())
                checkBoxOneTimeTrack.setSelected(false);
        });

        // bind circle status color to the track toggle button
        circleStatusTracker.fillProperty().bind(Bindings.createObjectBinding(() -> {
            if (toggleTrack.isSelected()) return Color.GREEN;
            else return Color.RED;
        }, toggleTrack.selectedProperty()));

        // listener for tracking
        AtomicInteger latestTrackedIndex = new AtomicInteger(-1);
        toggleTrack.selectedProperty().addListener((observable, oldIsSelected, newIsSelected) -> {
            if (newIsSelected) {
                // create new tracker handler
                tracker = new TrackingHandler(annotationPane);
                boolean initSuccessful = initializeTracker(getCurrentSelectedAnnotation());
                if (!initSuccessful)
                    toggleTrack.setSelected(false);
                else toggleTrack.setText("Clear");
            } else {
                tracker = null;
                latestTrackedIndex.set(-1);
                toggleTrack.setText("Track");
            }
        });

        listViewImages.getSelectionModel().selectedIndexProperty().addListener((observable, oldIndex, newIndex) -> {
            if (!toggleTrack.isSelected() || tracker == null) return;
            if (newIndex == null || newIndex.intValue() < 0) return;
            if (oldIndex != null && oldIndex.intValue() < 0) return;

            // on image change: update tracker and display new bounding box (only if newIndex is bigger then oldValue and latestTrackedIndex)
            if (oldIndex == null || (newIndex.intValue() > oldIndex.intValue() && newIndex.intValue() > latestTrackedIndex.get())) {
                latestTrackedIndex.set(newIndex.intValue());
                ImageData imageData = listViewImages.getItems().get(newIndex.intValue());

                Optional<SimpleBoundingBoxV3> box = tracker.update(imageData);
                // add box to pane when tracker delivered one
                box.ifPresent(boundingBox -> {
                    imageData.addAnnotation(boundingBox);
                    if (getCurrentSelectedImageData() == imageData) {
                        boundingBox.addToPane();
                        boundingBox.selectedProperty().set(true);
                    }
                });
            }
        });
    }

    private void initCopyControls() {
        spinnerCopyNumber.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 1, 1));
        // bind spinner value to button text
        buttonCopyNextNImages.textProperty().bind(Bindings.concat("Next ").concat(spinnerCopyNumber.valueProperty().asString()).concat(" Images"));
        // disable when no annotation is selected
        buttonCopyNextNImages.disableProperty().bind(listViewAnnotations.getSelectionModel().selectedItemProperty().isNull());

        buttonCopyNextNImages.setOnAction(event -> {
            UIAnnotation<?> annotation = getCurrentSelectedAnnotation();
            int startIdx = listViewImages.getSelectionModel().getSelectedIndex() + 1;
            int endIdx = startIdx + spinnerCopyNumber.getValue();
            int length = listViewImages.getItems().size();
            for (int i = startIdx; i < endIdx && i < length; i++) {
                ImageData imageData = listViewImages.getItems().get(i);
                UIAnnotation<?> annotationCopy = annotation.copy(imageData.getWidth(), imageData.getHeight());
                imageData.addAnnotation(annotationCopy);
            }
        });
    }
    //</editor-fold>

    boolean initializeTracker(UIAnnotation<?> selectedAnnotation) {
        if (selectedAnnotation instanceof UIBoundingBox) {
            return tracker.initializeTracker(getSelectedTracker().getTrackerClass(),
                    getCurrentSelectedImageData(),
                    (SimpleBoundingBoxV3) selectedAnnotation);
        }
        return false;
    }

    @Override
    public void close() {
        AutoSaveIA.stopAutoSaveScheduler();
        currentlyLoadedData.set(null);
    }
    //<editor-fold desc="Getter and Setter">

    public ImageAnnotationView getImageAnnotationView() {
        return imageAnnotationView;
    }

    public IAMenuBarController getMenuBarController() {
        return menuBarController;
    }

    public ChoiceBox<ImageAnnotationChoice> getChoiceBoxAnnotations() {
        return choiceBoxAnnotations;
    }

    public FrameViewer getFrameViewer() {
        return frameViewer;
    }

    public TextField getTextFieldDefaultAnnotationLabel() {
        return textFieldDefaultAnnotationLabel;
    }

    public ImageData getCurrentSelectedImageData() {
        return listViewImages.getSelectionModel().getSelectedItem();
    }

    private ObjectProperty<UIAnnotation<?>> currentSelectedAnnotation;

    public UIAnnotation<?> getCurrentSelectedAnnotation() {
        return currentSelectedAnnotation.get();
    }

    public ReadOnlyObjectProperty<UIAnnotation<?>> currentSelectedAnnotationProperty() {
        if (currentSelectedAnnotation == null) {
            currentSelectedAnnotation = new SimpleObjectProperty<>() {
                UIAnnotation<?> oldAnnotation;

                @Override
                protected void invalidated() {

                    if (oldAnnotation != null) {
                        checkBoxAutoCreated.selectedProperty().unbindBidirectional(oldAnnotation.getAnnotationModel().autoCreatedProperty());
                        checkBoxVerified.selectedProperty().unbindBidirectional(oldAnnotation.getAnnotationModel().verifiedProperty());
                        textFieldInstance.textProperty().unbindBidirectional(oldAnnotation.getAnnotationModel().instanceProperty());
                        checkBoxAutoCreated.setSelected(false);
                        checkBoxVerified.setSelected(false);
                        textFieldInstance.clear();
                    }

                    UIAnnotation<?> selectedAnnotation = get();
                    if (selectedAnnotation != null) {
                        listViewAnnotations.getSelectionModel().select(selectedAnnotation);
                        listViewAnnotations.scrollTo(listViewAnnotations.getSelectionModel().getSelectedIndex() - 1);
                        if (menuBarController.editOnSelect())
                            selectedAnnotation.setEditable(true);

                        checkBoxAutoCreated.selectedProperty().bindBidirectional(selectedAnnotation.getAnnotationModel().autoCreatedProperty());
                        checkBoxVerified.selectedProperty().bindBidirectional(selectedAnnotation.getAnnotationModel().verifiedProperty());
                        textFieldInstance.textProperty().bindBidirectional(selectedAnnotation.getAnnotationModel().instanceProperty());
                    } else {
                        listViewAnnotations.getSelectionModel().clearSelection();
                    }

                    oldAnnotation = selectedAnnotation;
                }
            };
        }
        return currentSelectedAnnotation;
    }

    public TrackerTypes getSelectedTracker() {
        return choiceBoxTracker.getSelectionModel().getSelectedItem();
    }

    public ListView<UIAnnotation<?>> getListViewAnnotations() {
        return listViewAnnotations;
    }

    public CheckBox getCheckBoxAutoNext() {
        return checkBoxAutoNext;
    }

    public Pane getAnnotationPane() {
        return annotationPane;
    }

    LoadedAnnotationData getCurrentlyLoadedData() {
        return currentlyLoadedData.get();
    }

    ReadOnlyObjectProperty<LoadedAnnotationData> currentlyLoadedDataProperty() {
        return currentlyLoadedData;
    }

    void setCurrentlyLoadedData(LoadedAnnotationData currentlyLoadedData) {
        this.currentlyLoadedData.set(currentlyLoadedData);
    }

    ListView<ImageData> getListViewImages() {
        return listViewImages;
    }

    Label getLabelLoadedImageFolderPath() {
        return labelLoadedImageFolderPath;
    }

    TrackingHandler getTracker() {
        return tracker;
    }

    ToggleButton getToggleTrack() {
        return toggleTrack;
    }

    CheckBox getCheckBoxOneTimeTrack() {
        return checkBoxOneTimeTrack;
    }

    CheckBox getCheckBoxSizeChangedInit() {
        return checkBoxSizeChangedInit;
    }

    public TextField getTextFieldDefaultImageLabel() {
        return textFieldDefaultImageLabel;
    }

    DetectionServer getDetectionServer() {
        return detectionServer;
    }



    //</editor-fold>
}
