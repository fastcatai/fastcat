package ui;

import annotation.UIAnnotation;
import controller.FrameModeController;
import controller.VideoReviewController;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import model.ImageData;
import videoreview.BindingUtil;

public class FlowAnnotationClasses {

    @FXML
    private HBox hBoxAnnotationClasses;
    @FXML
    private FlowPane flowPaneAnnotationClasses;

    private final FrameModeController fmController;
    private final VideoReviewController vrController;

    public FlowAnnotationClasses(FrameModeController fmController) {
        this.fmController = fmController;
        vrController = fmController.getVrController();
    }

    @FXML
    private void initialize() {
        // Binds selected annotation if ImageData changes for displaying the additional label
        final InvalidationListener selectedAnnotationListener = observable -> {
            ImageData imageData = vrController.getListAnnotatedImagesController().getCurrentImageData();
            if (imageData != null) {
                UIAnnotation<?> annotation = imageData.selectedAnnotationProperty().get();
                hBoxAnnotationClasses.setVisible(annotation != null);
                selectedAnnotation.set(annotation);
            }
        };

        // Add annotation selection listener when new ImageData object is added
        vrController.getListAnnotatedImagesController().addImageDataListener(imageData ->
                imageData.selectedAnnotationProperty().addListener(selectedAnnotationListener));

        // Adds/Removes listener if ImageData changes
        vrController.getListAnnotatedImagesController().currentImageDataProperty().addListener((observable, oldImageData, newImageData) -> {
            if (oldImageData != null)
                oldImageData.selectedAnnotationProperty().removeListener(selectedAnnotationListener);
            if (newImageData != null)
                newImageData.selectedAnnotationProperty().addListener(selectedAnnotationListener);
        });
    }

    /**
     * Adds a binding from additional labels list to FlowPane, if annotation is selected.
     */
    private final ObjectProperty<UIAnnotation<?>> selectedAnnotation = new SimpleObjectProperty<>() {
        private UIAnnotation<?> oldAnnotation;
        private BindingUtil.ListContentMapping<String, Node> oldListMapping;

        @Override
        protected void invalidated() {
            if (oldAnnotation != null) {
                oldAnnotation.getAnnotationModel().getAdditionalLabels().removeListener(oldListMapping);
            }
            flowPaneAnnotationClasses.getChildren().clear();
            final UIAnnotation<?> annotation = get();
            BindingUtil.ListContentMapping<String, Node> listMapping = null;

            if (annotation != null) {
                listMapping = BindingUtil.mapContent(
                        annotation.getAnnotationModel().getAdditionalLabels(),
                        flowPaneAnnotationClasses.getChildren(),
                        s -> {
                            Label label = new Label(s);
                            label.setOnMouseClicked(event -> annotation.getAnnotationModel().removeAdditionalLabel(s));
                            return label;
                        }
                );
                annotation.getAnnotationModel().getAdditionalLabels().addListener(listMapping);
            }
            oldAnnotation = annotation;
            oldListMapping = listMapping;
        }
    };
}
