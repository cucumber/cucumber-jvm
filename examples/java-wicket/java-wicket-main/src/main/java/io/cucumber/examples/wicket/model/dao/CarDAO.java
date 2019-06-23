package io.cucumber.examples.wicket.model.dao;

import io.cucumber.examples.wicket.model.entity.Car;

public interface CarDAO {
    public void add(Car car);

    Car findAvailableCar();

    int getNumberOfAvailableCars();
}
