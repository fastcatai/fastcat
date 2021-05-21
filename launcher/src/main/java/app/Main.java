package app;

public class Main {

    public static void main(String[] args) {
        System.setProperty("javafx.preloader", "preloader.SplashScreenLoader");
        MainApplication.launch(MainApplication.class, args);
    }
}
