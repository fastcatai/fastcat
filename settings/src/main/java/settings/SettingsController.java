package settings;

import annotation.AnnotationSettingsControllerV2;
import controller.GeneralSettingsController;
import controller.ImageAnnotationSettingsController;
import controller.KeymapIAController;
import controller.KeymapVRController;
import controller.SettingsBaseController;
import controller.VideoReviewSettingsController;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SettingsController implements Callback<Class<?>, Object> {
    private static final Logger logger = LogManager.getLogger(SettingsController.class);

    @FXML
    private StackPane stackPaneContent;
    @FXML
    private ListView<SettingsEntry> listViewEntries;

    // save all instances so that every FXMl and controller is only created once
    private final Map<String, Parent> loadedFxml = new HashMap<>();
    private final Map<Class<?>, SettingsBaseController> loadedControllers = new HashMap<>();
    private final ObjectProperty<Node> currentlyLoadedFxml = new SimpleObjectProperty<>();

    public SettingsController() {
        // sets the visibility of the old node to false and the new node to true
        currentlyLoadedFxml.addListener((observable, oldNode, newNode) -> {
            if (oldNode != null) oldNode.setVisible(false);
            if (newNode != null) newNode.setVisible(true);
        });
    }

    @FXML
    private void initialize() {
        listViewEntries.setCellFactory(new ListCellSettingsEntry());
        listViewEntries.getItems().addAll(SettingsEntry.values());

        listViewEntries.getSelectionModel().selectedItemProperty().addListener((observable, oldItem, newItem) -> {
            if (newItem.getFxml() != null && !newItem.getFxml().isEmpty()) {
                loadFxml(newItem.getFxml());
            }
        });

        listViewEntries.getSelectionModel().selectFirst();
    }

    @FXML
    private void onOkAction() {
        for (Class<?> c : loadedControllers.keySet())
            loadedControllers.get(c).onClickOk();
        ((Stage) stackPaneContent.getScene().getWindow()).close();
        SettingsProperties.saveProperty();
    }

    @FXML
    private void onCancelAction() {
        for (Class<?> c : loadedControllers.keySet())
            loadedControllers.get(c).onClickCancel();
        ((Stage) stackPaneContent.getScene().getWindow()).close();
    }

    /**
     * Loads a FXML if it not was already loaded or sets the Node as loaded.
     * The property currentlyLoadedFxml will then set the visibility to <code>true</code>.
     *
     * @param resourcePath the resource path
     */
    private void loadFxml(String resourcePath) {
        Parent fxmlRoot = loadedFxml.computeIfAbsent(resourcePath, s -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
                loader.setControllerFactory(this);
                Parent root = loader.load();
                stackPaneContent.getChildren().add(root);
                return root;
            } catch (IOException e) {
                logger.throwing(e);
                return null;
            }
        });
        currentlyLoadedFxml.set(fxmlRoot);
    }

    @Override
    public Object call(Class<?> param) {
        if (param == GeneralSettingsController.class)
            return loadedControllers.computeIfAbsent(param, aClass -> new GeneralSettingsController());
        else if (param == KeymapIAController.class)
            return loadedControllers.computeIfAbsent(param, aClass -> new KeymapIAController());
        else if (param == KeymapVRController.class)
            return loadedControllers.computeIfAbsent(param, aClass -> new KeymapVRController());
        else if (param == ImageAnnotationSettingsController.class)
            return loadedControllers.computeIfAbsent(param, aClass -> new ImageAnnotationSettingsController());
        else if (param == AnnotationSettingsControllerV2.class)
            return loadedControllers.computeIfAbsent(param, aClass -> new AnnotationSettingsControllerV2());
        else if (param == VideoReviewSettingsController.class)
            return loadedControllers.computeIfAbsent(param, aClass -> new VideoReviewSettingsController());
        else throw new RuntimeException("Class " + param.getName() + " is not supported");
    }
}
