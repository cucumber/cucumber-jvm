package io.cucumber.examples.wicket.view;

import io.cucumber.examples.wicket.Application;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.util.value.ValueMap;

public class Create extends WebPage {
    private int numberOfCars;

    public Create() {
        CreateCarsForm createCarsForm = new CreateCarsForm("createCarsForm");
        add(createCarsForm);
    }

    public void setNumberOfCars(int initialNumberOfCars) {
        numberOfCars = initialNumberOfCars;
    }

    public void create() {
        for (int i = 0; i < numberOfCars; i++) {
            ((Application) getApplication()).createCar();
        }
    }

    private class CreateCarsForm extends Form<ValueMap> {
        public CreateCarsForm(String id) {
            super(id, new CompoundPropertyModel<ValueMap>(new ValueMap()));

            FormComponent<Integer> textField = new TextField<Integer>("numberOfCarsField");
            textField.setType(String.class);

            add(textField);
        }

        @Override
        public final void onSubmit() {
            ValueMap values = getModelObject();

            String addedCars = (String) values.get("numberOfCarsField");

            numberOfCars = Integer.parseInt(addedCars);
            create();

            setResponsePage(Available.class);
        }
    }
}
