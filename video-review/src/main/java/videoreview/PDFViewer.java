package videoreview;

import com.dansoftware.pdfdisplayer.PDFDisplayer;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import ui.Dialogs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PDFViewer {

    private final PDFDisplayer viewer;

    public PDFViewer(Path pdfFile) {
        viewer = new PDFDisplayer();
        hideFunctionalities();
        // load and display PDF file
        loadPDF(pdfFile);
    }

    /**
     * Hide some buttons and UI elements that are not needed
     */
    private void hideFunctionalities() {
        // hide sidebar button
        viewer.setVisibilityOf("sidebarToggle", false);
        // remove sidebar because it could show up in some PDFs
        viewer.executeScript("document.getElementById('sidebarContainer').remove();");
        // delete custom scale entry (JavaFX browser is not able to hide it)
        viewer.executeScript("document.getElementById('customScaleOption').remove();");
        // bookmark button
        viewer.setVisibilityOf("secondaryViewBookmark", false);
        // separator
        viewer.executeScript("document.getElementsByClassName('horizontalToolbarSeparator visibleLargeView')[0].style.display = 'none';" +
                "document.getElementsByClassName('horizontalToolbarSeparator visibleLargeView')[0].style.visibility = 'hidden';");
        // open, print and download button
        viewer.setVisibilityOf("secondaryOpenFile", false);
        viewer.setVisibilityOf("secondaryPrint", false);
        viewer.setVisibilityOf("secondaryDownload", false);
        // page spread buttons
        viewer.setVisibilityOf("spreadNone", false);
        viewer.setVisibilityOf("spreadOdd", false);
        viewer.setVisibilityOf("spreadEven", false);
        // separator
        viewer.executeScript("document.getElementsByClassName('spreadModeButtons')[3].style.display = 'none';" +
                "document.getElementsByClassName('spreadModeButtons')[3].style.visibility = 'hidden';");
    }

    private void loadPDF(Path pdfFile) {
        try {
            viewer.displayPdf(Files.newInputStream(pdfFile));
//            viewer.loadPDF(Files.newInputStream(pdfFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Scene getScene() {
        return new Scene(viewer.toNode());
    }

    private static final List<Stage> openStages = new ArrayList<>();

    public static void openPdfWindow(Path pdfFile) {
        Stage stage = new Stage();
        stage.setTitle(pdfFile.getFileName().toString());
        stage.setWidth(650);
        stage.setHeight(925);
        InputStream iconIS = Dialogs.class.getClassLoader().getResourceAsStream("logo_polygon.png");
        if (iconIS != null) stage.getIcons().add(new Image(iconIS));

        // keep track of all open PDF viewer stages, because we need to close them when application closes
        openStages.add(stage);
        stage.setOnCloseRequest(event -> openStages.remove(stage));

        // position windows on screen
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getMinX() + screenBounds.getWidth()) - stage.getWidth());
        stage.setY(0);

        PDFViewer viewer = new PDFViewer(pdfFile);
        stage.setScene(viewer.getScene());

        stage.show();
    }

    public static void closeAllWindows() {
        for (Stage s : openStages)
            s.close();
    }
}
