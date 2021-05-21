package ui;

import controller.VideoReviewController;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.util.Callback;
import model.FrameData;
import model.SuperframeData;
import util.FrameDataCreatedListener;

import java.util.ArrayList;
import java.util.List;

public class ListFrameData implements Callback<ListView<FrameData>, ListCell<FrameData>> {

    @FXML
    private ListView<FrameData> listViewFrameData;

    private VideoReviewController vrController;

    public ListFrameData(VideoReviewController vrController) {
        this.vrController = vrController;
    }

    @FXML
    private void initialize() {
        listViewFrameData.itemsProperty().bind(Bindings.createObjectBinding(() -> {
            SuperframeData data = vrController.getCurrentlySelectedSuperframe();
            if (data != null) return data.getChildFrames();
            return FXCollections.emptyObservableList();
        }, vrController.getListViewSuperframes().getSelectionModel().selectedItemProperty()));
    }

    @Override
    public ListCell<FrameData> call(ListView<FrameData> listView) {
        return new ListCellFrame();
    }

    private final Runnable jumpToFrame = () -> {
        FrameData frameData = listViewFrameData.getSelectionModel().getSelectedItem();
        if (frameData != null)
            vrController.getFrameViewerMode().getFrameLoader().jumpToFrame(frameData.getFrameNumber());
    };

    @FXML
    private void onFrameDataMouseClicked(MouseEvent event) {
        if (event.getClickCount() == 1 && event.getButton() == MouseButton.PRIMARY)
            jumpToFrame.run();
    }

    @FXML
    private void onFrameDataKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER)
            jumpToFrame.run();
    }

    // Holds the list of registered listeners
    private final List<FrameDataCreatedListener> frameDataCreatedListeners = new ArrayList<>();

    public void addFrameDataListener(FrameDataCreatedListener listener) {
        frameDataCreatedListeners.add(listener);
    }

    public void removeFrameDataListener(FrameDataCreatedListener listener) {
        frameDataCreatedListeners.remove(listener);
    }

    /**
     * Fire a FrameData event.
     *
     * @param frameData pbject that was created and added
     */
    public void fireFrameDataEvent(FrameData frameData) {
        for (FrameDataCreatedListener l : frameDataCreatedListeners) {
            l.onNewFrameData(frameData);
        }
    }

    private static class ListCellFrame extends ListCell<FrameData> {

        public ListCellFrame() {
            Popup popup = new Popup();
            VBox vBox = new VBox();
            vBox.getStylesheets().add(getClass().getResource("/css/VideoReviewPopUp.css").toExternalForm());
            vBox.getStyleClass().add("freeze-popup");
            popup.getContent().add(vBox);

            hoverProperty().addListener((observable, oldValue, newValue) -> {
                if (getItem() == null || isEmpty())
                    return;
                if (newValue && getItem().getClasses().size() > 2) {
                    vBox.getChildren().add(new Label("Classes"));
                    for (String cls : getItem().getClasses()) {
                        vBox.getChildren().add(new Label(cls));
                    }
                    setOnMouseMoved(event -> popup.show(this, event.getScreenX() + 20, event.getScreenY()));
                } else {
                    popup.hide();
                    setOnMouseMoved(null);
                    vBox.getChildren().clear();
                }
            });
        }

        @Override
        protected void updateItem(FrameData item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                textProperty().unbind();
                setText(null);
                setGraphic(null);
            } else {
                textProperty().bind(Bindings.createStringBinding(() -> {
                    String text = String.valueOf(item.getFrameNumber());
                    if (item.getClasses().size() >= 1) text += ": " + item.getClasses().get(0);
                    if (item.getClasses().size() >= 2) text += "; " + item.getClasses().get(1);
                    return text;
                }, item.getClasses()));

                setGraphic(null);
            }
        }
    }
}
