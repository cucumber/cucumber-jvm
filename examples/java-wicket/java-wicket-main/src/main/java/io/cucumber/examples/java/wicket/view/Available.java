package io.cucumber.examples.java.wicket.view;

import io.cucumber.examples.java.wicket.Application;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

public class Available extends WebPage {

    public Available() {
        String availableCars = "" + getAvailableCars();
        Label message = new Label("availableCars", availableCars);
        add(message);
    }

    public int getAvailableCars() {
        return ((Application) getApplication()).getNumberOfAvailableCars();
    }

}
