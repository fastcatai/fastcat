package about;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;

class AboutController {

    @FXML
    private Hyperlink hyperlinkIkonli;
    @FXML
    private Hyperlink hyperlinkMaterialIcons;
    @FXML
    private Hyperlink hyperlinkPDFViewer;

    private final Application application;

    AboutController(Application application) {
        this.application = application;
    }

    @FXML
    private void initialize() {
        hyperlinkIkonli.setOnAction(event ->
                application.getHostServices().showDocument("https://github.com/kordamp/ikonli"));
        hyperlinkMaterialIcons.setOnAction(event ->
                application.getHostServices().showDocument("https://github.com/Templarian/MaterialDesign"));
        hyperlinkPDFViewer.setOnAction(event -> {
            application.getHostServices().showDocument("https://github.com/Dansoftowner/PDFViewerFX");
        });
    }
}
