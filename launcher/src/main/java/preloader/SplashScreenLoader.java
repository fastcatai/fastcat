package preloader;

import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SplashScreenLoader extends Preloader {

    private Stage primaryStage;
    private SplashScreenController splashScreenController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SplashScreen.fxml"));
        splashScreenController = new SplashScreenController();
        loader.setController(splashScreenController);
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
        primaryStage.centerOnScreen();
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification info) {
        if (info.getType() == StateChangeNotification.Type.BEFORE_START)
            primaryStage.hide();
    }

    @Override
    public void handleProgressNotification(ProgressNotification info) {
//        splashScreenController.getProgressBar().setProgress(info.getProgress());
    }
}
