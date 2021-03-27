package io.cucumber.examples.wicket.main.model.dao;

import io.cucumber.examples.wicket.main.model.entity.Car;

public interface CarDAO {

    void add(Car car);

    Car findAvailableCar();

    int getNumberOfAvailableCars();

}
