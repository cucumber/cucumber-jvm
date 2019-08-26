package cucumber.examples.java.wicket.view;

import cucumber.examples.java.wicket.Application;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

public class Available extends WebPage {
    private Application application;

    public Available() {
        application = (Application) getApplication();

        String availableCars = "" + getAvailableCars();
        Label message = new Label("availableCars", availableCars);
        add(message);
    }

    public int getAvailableCars() {
        return application.getNumberOfAvailableCars();
    }
}
