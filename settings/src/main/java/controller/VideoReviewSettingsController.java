package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;
import settings.SettingsProperties;

public class VideoReviewSettingsController implements SettingsBaseController {
    @FXML
    private Spinner<Integer> spinnerFramePlayerDelay;

    @FXML
    private void initialize() {
        int initialValue = Integer.parseInt(SettingsProperties.getProperty(SettingsProperties.VIDEO_REVIEW_DISPLAY_FRAME_PLAYER_DELAY, "2"));
        int maxValue = 60;
        // boundary check
        initialValue = Math.max(initialValue, 1);
        initialValue = Math.min(initialValue, maxValue);
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxValue, initialValue, 1);
        valueFactory.setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer frames) {
                if (frames == 1) return frames + " frame";
                return frames + " frames";
            }

            @Override
            public Integer fromString(String string) {
                String valueWithoutUnit = string.substring(0, string.indexOf(" ")).trim();
                if (valueWithoutUnit.isEmpty()) return 2;
                return Integer.valueOf(valueWithoutUnit);
            }
        });
        spinnerFramePlayerDelay.setValueFactory(valueFactory);
    }

    @Override
    public void onClickOk() {
        SettingsProperties.setProperty(SettingsProperties.VIDEO_REVIEW_DISPLAY_FRAME_PLAYER_DELAY, spinnerFramePlayerDelay.getValue().toString());
    }

    @Override
    public void onClickCancel() {
        // nothing
    }
}
