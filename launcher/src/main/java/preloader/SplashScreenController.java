package preloader;

import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;

public class SplashScreenController {

    @FXML
    private ProgressBar progressBar;

    @FXML
    private void initialize() {
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }
}
