package detector;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DockerClientBuilder;
import javafx.beans.property.ObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ToggleButton;
import settings.SettingsProperties;

import java.io.IOException;
import java.util.List;

public class DockerContainer {

    // TODO: Check if Docker Engine is running
    // TODO: Check running status on program start
    // TODO: Check periodically if container is still running

    private final ObjectProperty<Boolean> dockerContainerRunning;
    private final ToggleButton toggleButton;

    private DockerClient dockerClient;
    private Container detectionContainer;

    public DockerContainer(ObjectProperty<Boolean> dockerContainerRunning, ToggleButton toggleButton) {
        this.dockerContainerRunning = dockerContainerRunning;
        this.toggleButton = toggleButton;
    }

    private boolean buildDockerClient() {
        if (dockerClient == null) {
            String host = SettingsProperties.getProperty(SettingsProperties.IMAGE_ANNOTATION_DOCKER_HOST);
            if (host != null) {
                dockerClient = DockerClientBuilder.getInstance(host).build();
                return true;
            } else {
                showInformationDialog("No Docker Host Address", "No Docker host address was define. Go into settings and set an address.");
                return false;
            }
        }
        return true;
    }

    /**
     * Create task for starting the docker container
     */
    private Task<Boolean> createStartContainerTask() {
        Task<Boolean> startContainerTask = new Task<>() {
            @Override
            protected Boolean call() {
                List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
                // search for container
                String containerName = SettingsProperties.getProperty(SettingsProperties.IMAGE_ANNOTATION_DOCKER_CONTAINER);
                if (containerName != null) {
                    for (Container container : containers) {
                        if (container.getNames()[0].equals("/" + containerName)) {
                            detectionContainer = container;
                            break;
                        }
                    }
                } else return false; // no container name in settings
                if (detectionContainer == null)
                    return false; // container not found
                if (!detectionContainer.getState().equals("running")) {
                    dockerClient.startContainerCmd(detectionContainer.getId()).exec();
                    return true; // started
                }
                return null; // already running
            }
        };

        startContainerTask.setOnRunning(event -> dockerContainerRunning.set(null));

        startContainerTask.setOnSucceeded(event -> {
            Boolean result = (Boolean) event.getSource().getValue();
            dockerContainerRunning.set(result == null || result);
            if (result != null && !result) {
                toggleButton.setSelected(false);
                showInformationDialog("Not Found", String.format("A container with the name '%s' could not be found. Check the name and change in the settings.",
                                SettingsProperties.getProperty(SettingsProperties.IMAGE_ANNOTATION_DOCKER_CONTAINER, " ")));
            }
        });

        startContainerTask.setOnCancelled(event -> {
            dockerContainerRunning.set(false);
            toggleButton.setSelected(false);
            showInformationDialog("Cancel", "During start of the container the process was canceled.");
        });

        startContainerTask.setOnFailed(event -> {
            dockerContainerRunning.set(false);
            toggleButton.setSelected(false);
            showInformationDialog("Failed to start", "Container could not be started. Is the Docker Engine running? Is the host address correct?");
        });

        return startContainerTask;
    }

    /**
     * Create task for stopping the docker container
     */
    private Task<Void> createStopContainerTask() {
        Task<Void> stopContainerTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    dockerClient.stopContainerCmd(detectionContainer.getId()).exec();
                    dockerClient.close();
                    dockerClient = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                detectionContainer = null;
                return null;
            }
        };

        stopContainerTask.setOnRunning(event -> dockerContainerRunning.set(null));

        stopContainerTask.setOnSucceeded(event -> dockerContainerRunning.set(false));

        stopContainerTask.setOnCancelled(event -> {
            dockerContainerRunning.set(true);
            toggleButton.setSelected(true);
            showInformationDialog("Failed to stop", "Process cancel during stopping process.");
        });

        stopContainerTask.setOnFailed(event -> {
            dockerContainerRunning.set(true);
            toggleButton.setSelected(true);
            showInformationDialog("Failed to stop", "Container could not be stopped");
        });

        return stopContainerTask;
    }

    private void showInformationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void startContainer() {
        if (buildDockerClient()) {
            Thread startContainerThread = new Thread(createStartContainerTask());
            startContainerThread.setName("Starting Docker Container");
            startContainerThread.start();
        }
    }

    public void stopContainer() {
        if (detectionContainer != null && buildDockerClient()) {
            Thread stopContainerThread = new Thread(createStopContainerTask());
            stopContainerThread.setName("Stopping Docker Container");
            stopContainerThread.start();
        }
    }
}
