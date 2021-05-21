package listviewcells;

import javafx.beans.binding.Bindings;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import settings.SettingsProperties;
import model.ImageData;

public class ListCellImages extends ListCell<ImageData>
        implements Callback<ListView<ImageData>, ListCell<ImageData>> {

    @Override
    protected void updateItem(ImageData item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            textProperty().unbind();
            setText(null);
            setGraphic(null);
        } else {
            textProperty().bind(Bindings.createStringBinding(() -> {
                String imgLabel = item.getImageClassification().getLabel();
                int count = item.annotationCountProperty().get();
                String suffix = "";
                if (!imgLabel.isBlank()) suffix += " : " + imgLabel;
                if (count > 0) suffix += " (" + count + ")";
                return item.getFilename() + suffix;
            }, item.annotationCountProperty(), item.getImageClassification().labelProperty()));

            // TODO: bind textFillProperty?
            boolean gotAnyVerifiedAnnotation = item.getAnnotations().stream().anyMatch(annotationUI -> annotationUI.getAnnotationModel().isVerified());
            if (gotAnyVerifiedAnnotation)
                setTextFill(Color.valueOf(SettingsProperties.getProperty(SettingsProperties.ANNOTATION_STROKE_COLOR_VERIFIED)));
            else setTextFill(Color.BLACK);

            setGraphic(null);
        }
    }

    @Override
    public ListCell<ImageData> call(ListView<ImageData> param) {
        return new ListCellImages();
    }
}
