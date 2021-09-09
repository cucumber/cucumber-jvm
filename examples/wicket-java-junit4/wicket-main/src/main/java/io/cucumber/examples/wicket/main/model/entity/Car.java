package io.cucumber.examples.wicket.main.model.entity;

public class Car {

    private boolean rented;

    public void rent() {
        rented = true;
    }

    public boolean isRented() {
        return rented;
    }

}
