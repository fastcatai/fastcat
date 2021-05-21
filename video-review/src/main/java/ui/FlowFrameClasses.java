package ui;

import controller.FrameModeController;
import controller.VideoReviewController;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import model.FrameData;
import videoreview.BindingUtil;

public class FlowFrameClasses {

    @FXML
    private HBox hBoxFrameClasses;
    @FXML
    private FlowPane flowPaneFrameClasses;

    private final FrameModeController fmController;
    private final VideoReviewController vrController;

    public FlowFrameClasses(FrameModeController fmController) {
        this.fmController = fmController;
        vrController = fmController.getVrController();
    }

    @FXML
    private void initialize() {
        // set the FrameData object when Frame changes, or null if no FrameData exists
        fmController.getFrameLoader().currentFrameProperty().addListener((observable, oldFrame, newFrame) -> {
            FrameData frameData = null;
            if (newFrame != null) {
                frameData = fmController.getVrController().getCurrentlySelectedSuperframe()
                        .getFrame(newFrame.getFrameNumber())
                        .orElse(null);
            }
            currentFrameData.set(frameData);
        });
        // Add set FrameData when new FrameData object is added
        vrController.getListViewFrameDataController().addFrameDataListener(currentFrameData::set);
    }

    private final ObjectProperty<FrameData> currentFrameData = new SimpleObjectProperty<>() {
        private FrameData oldFrameData;
        private BindingUtil.ListContentMapping<String, Node> oldListMapping;

        @Override
        protected void invalidated() {
            if (oldFrameData != null) {
                oldFrameData.getClasses().removeListener(oldListMapping);
            }
            flowPaneFrameClasses.getChildren().clear();
            final FrameData frameData = get();
            BindingUtil.ListContentMapping<String, Node> listMapping = null;

            if (frameData != null) {
                listMapping = BindingUtil.mapContent(
                        frameData.getClasses(),
                        flowPaneFrameClasses.getChildren(),
                        s -> {
                            Label label = new Label(s);
                            label.setOnMouseClicked(event -> frameData.getClasses().remove(s));
                            return label;
                        });
                frameData.getClasses().addListener(listMapping);
            }
            oldFrameData = frameData;
            oldListMapping = listMapping;
        }
    };
}
