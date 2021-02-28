package io.cucumber.examples.java.wicket.view;

import io.cucumber.examples.java.wicket.Application;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.util.value.ValueMap;

public class Rent extends WebPage {

    public Rent() {
        RentCarForm rentCarForm = new RentCarForm("rentCarForm");
        add(rentCarForm);
    }

    public void rent() {
        ((Application) getApplication()).rentCar();
    }

    private class RentCarForm extends Form<ValueMap> {

        public RentCarForm(String id) {
            super(id, new CompoundPropertyModel<>(new ValueMap()));
        }

        @Override
        public final void onSubmit() {
            rent();

            setResponsePage(Available.class);
        }

    }

}
