package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;
import settings.SettingsProperties;

public class GeneralSettingsController implements SettingsBaseController {

    @FXML
    private Spinner<Integer> spinnerAutoSaveInterval;

    @FXML
    private void initialize() {
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(15, 600, 30, 15);
        valueFactory.setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer seconds) {
                int sec = seconds % 60;
                int min = seconds / 60;
                if (min <= 0) {
                    return sec + " sec";
                } else {
                    if (sec <= 0) return min + " min";
                    else return min + " min " + sec + " sec";
                }
            }

            @Override
            public Integer fromString(String string) {
                String[] s = string.split("min");
                int sec, min;
                try {
                    min = Integer.parseInt(s[0].trim());
                } catch (NumberFormatException e) {
                    // then "1 sec"
                    return Integer.parseInt(s[0].replaceAll("sec", "").trim());
                }
                // possibilities: "1 min" or "1 min 1 sec"
                if (s.length > 1)
                    sec = Integer.parseInt(s[1].replaceAll("sec", "").trim());
                else sec = 0;
                return min * 60 + sec;
            }
        });
        spinnerAutoSaveInterval.setValueFactory(valueFactory);

        String interval = SettingsProperties.getProperty(SettingsProperties.GENERAL_AUTO_SAVE_INTERVAL);
        if (interval != null)
            spinnerAutoSaveInterval.getValueFactory().setValue(Integer.valueOf(interval));
    }

    @Override
    public void onClickOk() {
        SettingsProperties.setProperty(SettingsProperties.GENERAL_AUTO_SAVE_INTERVAL, spinnerAutoSaveInterval.getValue().toString());
    }

    @Override
    public void onClickCancel() {
        // nothing
    }
}
