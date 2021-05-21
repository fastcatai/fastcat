package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import settings.SettingsProperties;

public class ImageAnnotationSettingsController implements SettingsBaseController {

    @FXML
    private TextField textFieldDetectionServerUrl;
    @FXML
    private TextField textFieldHost;
    @FXML
    private TextField textFieldDockerImage;
    @FXML
    private TextField textFieldDockerContainer;

    @FXML
    private void initialize() {
        textFieldDetectionServerUrl.setText(SettingsProperties.getProperty(SettingsProperties.IMAGE_ANNOTATION_DETECTION_SERVER, ""));
        textFieldHost.setText(SettingsProperties.getProperty(SettingsProperties.IMAGE_ANNOTATION_DOCKER_HOST, ""));
        textFieldDockerImage.setText(SettingsProperties.getProperty(SettingsProperties.IMAGE_ANNOTATION_DOCKER_IMAGE, ""));
        textFieldDockerContainer.setText(SettingsProperties.getProperty(SettingsProperties.IMAGE_ANNOTATION_DOCKER_CONTAINER, ""));
    }

    @Override
    public void onClickOk() {
        SettingsProperties.setProperty(SettingsProperties.IMAGE_ANNOTATION_DETECTION_SERVER, textFieldDetectionServerUrl.getText().strip());
        SettingsProperties.setProperty(SettingsProperties.IMAGE_ANNOTATION_DOCKER_HOST, textFieldHost.getText().strip());
        SettingsProperties.setProperty(SettingsProperties.IMAGE_ANNOTATION_DOCKER_IMAGE, textFieldDockerImage.getText().strip());
        SettingsProperties.setProperty(SettingsProperties.IMAGE_ANNOTATION_DOCKER_CONTAINER, textFieldDockerContainer.getText().strip());
    }

    @Override
    public void onClickCancel() {
        // nothing
    }
}
