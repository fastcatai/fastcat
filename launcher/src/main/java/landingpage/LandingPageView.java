package landingpage;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.util.Builder;
import util.BaseView;

public class LandingPageView extends BaseView {

    private LandingPageController landingPageController;

    public LandingPageView(final Application application, final Stage stage) {
        super("/fxml/landingpage/LandingPage.fxml", application, stage);
    }

    @Override
    public LandingPageController getController() {
        return landingPageController;
    }

    @Override
    public void setDefaultControllers() {

    }

    @Override
    public Object call(Class<?> param) {
        if (param == LandingPageController.class) {
            landingPageController = new LandingPageController(getApplication(), getStage());
            return landingPageController;
        } else return null;
    }

    @Override
    public Builder<?> getBuilder(Class<?> type) {
        return null;
    }
}
