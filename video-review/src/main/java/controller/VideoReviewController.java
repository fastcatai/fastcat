package controller;

import annotation.SelectionGroup;
import annotation.UIAnnotation;
import handler.AutoSaveVR;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import keymap.model.ReadOnlyKeyActionClass;
import model.FrameData;
import model.ImageData;
import model.LoadedReviewData;
import model.SuperframeData;
import ui.IconRadioButton;
import ui.ListAnnotatedImages;
import ui.ListCellFreeze;
import ui.ListFrameData;
import ui.ListViewClasses;
import util.BaseController;
import util.VideoReviewView;
import videoreview.ReviewAnnotationChoice;
import videoreview.ViewMode;

public class VideoReviewController implements BaseController {
    @FXML
    private MenuBar menuBar;
    @FXML
    private VRMenuBarController menuBarController;
    @FXML
    private HBox hBoxLandingView;

    // Review Controls
    @FXML
    private Label labelLoadedVideo;
    @FXML
    private SplitPane splitPaneLists;
    @FXML
    private ListView<SuperframeData> listViewSuperframes;
    @FXML
    public ListView<FrameData> listViewFrameData;
    @FXML
    public ListFrameData listViewFrameDataController;

    @FXML
    public VBox vBoxSuperframes;
    // Annotated Images List
    @FXML
    private VBox vBoxAnnotatedImages;
    @FXML
    private ListView<ImageData> listViewAnnotatedImages;
    @FXML
    private ListAnnotatedImages listViewAnnotatedImagesController;

    // List of Classes
    @FXML
    private VBox vBoxClasses;
    @FXML
    public ListView<ReadOnlyKeyActionClass> listClasses;
    @FXML
    private ListViewClasses listClassesController;

    // lists buttons
    @FXML
    private IconRadioButton buttonSuperframesList;
    @FXML
    private IconRadioButton buttonAnnotatedImagesList;
    @FXML
    private IconRadioButton buttonClassesList;

    // Annotation Controls
    @FXML
    private ChoiceBox<ReviewAnnotationChoice> choiceBoxAnnotations;
    @FXML
    private TextField textFieldAnnotationLabel;
    @FXML
    private ListView<UIAnnotation<?>> listViewAnnotations;

    // View modes with controllers
    @FXML
    private StackPane stackPaneModeView;
    @FXML
    public VBox videoMode;
    @FXML
    private VideoModeController videoModeController;
    @FXML
    public VBox frameMode;
    @FXML
    private FrameModeController frameModeController;

    private final ObjectProperty<LoadedReviewData> currentlyLoadedData = new SimpleObjectProperty<>();
    // tracks in which view the user is at the moment
    private final ObjectProperty<ViewMode> currentViewMode = new SimpleObjectProperty<>();
    private final ObjectProperty<SuperframeData> currentSuperframeData = new SimpleObjectProperty<>();

    private final VideoReviewView videoReviewView;

    public VideoReviewController(VideoReviewView videoReviewView) {
        this.videoReviewView = videoReviewView;
    }

    @FXML
    private void initialize() {
        // set initial view
        currentViewMode.set(ViewMode.VIDEO);
        frameMode.setVisible(false);
        videoMode.setVisible(true);
        hBoxLandingView.setVisible(true);

        // controls the switching between video and frame mode
        currentViewMode.addListener((observable, oldMode, newMode) -> {
            if (newMode == ViewMode.FRAMES && videoModeController.videoIsPlaying())
                videoModeController.videoPause(); // pause video
            videoMode.setVisible(newMode == ViewMode.VIDEO);
            frameMode.setVisible(newMode == ViewMode.FRAMES);
        });

        initLoadSaveControls();
        initListViews();
        initAnnotationControls();

        buttonSuperframesList.setOnAction(event -> vBoxSuperframes.toFront());
        buttonClassesList.setOnAction(event -> vBoxClasses.toFront());
        buttonAnnotatedImagesList.setOnAction(event -> vBoxAnnotatedImages.toFront());
        buttonSuperframesList.fire();


        getListAnnotatedImagesController().currentSelectionGroupProperty().addListener(observable -> {
            menuBarController.getMenuItemDeleteAnnotation().disableProperty().unbind();
            SelectionGroup sg = getListAnnotatedImagesController().getCurrentSelectionGroup();
            if (sg != null)
                menuBarController.getMenuItemDeleteAnnotation().disableProperty().bind(sg.selectedAnnotationProperty().isNull());
            else menuBarController.getMenuItemDeleteAnnotation().setDisable(true);
        });

        textFieldAnnotationLabel.requestFocus();
    }

    private void initLoadSaveControls() {
        labelLoadedVideo.textProperty().bind(Bindings.createStringBinding(() -> {
            if (currentlyLoadedData.get() != null)
                return currentlyLoadedData.get().getVideoFile().getFileName().toString();
            return "";
        }, currentlyLoadedData));

        // new loaded data via video loading dialog
        currentlyLoadedData.addListener((observable, oldLoadedData, newLoadedData) -> {
            if (newLoadedData == null)
                return;
            AutoSaveVR.stopAutoSaveScheduler();
            frameModeController.getFrameLoader().loadFolder(newLoadedData.getImageFolder(), newLoadedData.getAllFrames());
            hBoxLandingView.setVisible(false);
            currentViewMode.set(ViewMode.VIDEO);

            // get loaded superframes
            getListViewSuperframes().setItems(FXCollections.observableList(newLoadedData.getSuperframes()));

            // get loaded image annotation data if available
            getListAnnotatedImagesController().clear();
            if (newLoadedData.getJsonWrapper() != null)
                getListAnnotatedImagesController().addAllImageData(newLoadedData.getJsonWrapper().getImages());

            // start auto saver
            AutoSaveVR.startAutoSaveScheduler(getListViewSuperframes().getItems(),
                    getListAnnotatedImagesController().getImageDataList(),
                    newLoadedData);

            // start video player
            getVideoPlayerMode().videoLoad(newLoadedData.getVideoFile().toString());
            getVideoPlayerMode().videoPlay();
        });
    }

    private void initListViews() {
        // ####################
        // Superframes
        // ####################
        listViewSuperframes.setCellFactory(new ListCellFreeze());

        Runnable jumpToSuperframe = () -> {
            SuperframeData selectedSuperframe = getCurrentlySelectedSuperframe();
            currentSuperframeData.set(selectedSuperframe);
            frameModeController.getFrameLoader().jumpToFrame(selectedSuperframe.getFrameNumber());
            currentViewMode.set(ViewMode.FRAMES);
        };
        listViewSuperframes.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1 && event.getButton() == MouseButton.PRIMARY)
                jumpToSuperframe.run();
        });
        listViewSuperframes.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER)
                jumpToSuperframe.run();
        });
    }

    private void initAnnotationControls() {
        // annotation creation type choice box
        choiceBoxAnnotations.getItems().setAll(ReviewAnnotationChoice.values());
        choiceBoxAnnotations.getSelectionModel().select(ReviewAnnotationChoice.BOX_CLICK_DRAG);

        // default label text field
        textFieldAnnotationLabel.textProperty().addListener((observableValue, oldText, newText) -> {
            if (newText.isBlank()) textFieldAnnotationLabel.getStyleClass().add("text-field-validation-error");
            else textFieldAnnotationLabel.getStyleClass().remove("text-field-validation-error");
        });
        textFieldAnnotationLabel.clear();
        textFieldAnnotationLabel.getStyleClass().add("text-field-validation-error");

        ((StringProperty) defaultAnnotationLabelProperty()).bind(textFieldAnnotationLabel.textProperty());
    }

    public void setCurrentViewMode(ViewMode currentViewMode) {
        this.currentViewMode.set(currentViewMode);
    }

    public VideoModeController getVideoPlayerMode() {
        return videoModeController;
    }

    public FrameModeController getFrameViewerMode() {
        return frameModeController;
    }

    public ChoiceBox<ReviewAnnotationChoice> getChoiceBoxAnnotations() {
        return choiceBoxAnnotations;
    }

    public ListView<UIAnnotation<?>> getListViewAnnotations() {
        return listViewAnnotations;
    }

    private StringProperty defaultAnnotationLabel;

    public String getDefaultAnnotationLabel() {
        return defaultAnnotationLabelProperty().get();
    }

    public ReadOnlyStringProperty defaultAnnotationLabelProperty() {
        if (defaultAnnotationLabel == null)
            defaultAnnotationLabel = new SimpleStringProperty();
        return defaultAnnotationLabel;
    }

    public TextField getTextFieldAnnotationLabel() {
        return textFieldAnnotationLabel;
    }

    public ListView<SuperframeData> getListViewSuperframes() {
        return listViewSuperframes;
    }

    public ListView<FrameData> getListViewFrameData() {
        return listViewFrameData;
    }

    public ListFrameData getListViewFrameDataController() {
        return listViewFrameDataController;
    }

    public LoadedReviewData getCurrentlyLoadedData() {
        return currentlyLoadedData.get();
    }

    public ObjectProperty<LoadedReviewData> currentlyLoadedDataProperty() {
        return currentlyLoadedData;
    }

    public SuperframeData getCurrentlySelectedSuperframe() {
        return listViewSuperframes.getSelectionModel().getSelectedItem();
    }

    public ObjectProperty<SuperframeData> currentSuperframeDataProperty() {
        return currentSuperframeData;
    }

    public SuperframeData getCurrentSuperframeData() {
        return currentSuperframeData.get();
    }

    public VideoReviewView getVideoReviewView() {
        return videoReviewView;
    }

    public VRMenuBarController getMenuBarController() {
        return menuBarController;
    }

    public ListViewClasses getListClassesController() {
        return listClassesController;
    }

    public ListView<ImageData> getListViewAnnotatedImages() {
        return listViewAnnotatedImages;
    }

    public ListAnnotatedImages getListAnnotatedImagesController() {
        return listViewAnnotatedImagesController;
    }

    @Override
    public void close() {
        AutoSaveVR.stopAutoSaveScheduler();
        videoModeController.stop();
        frameModeController.stop();
    }
}
