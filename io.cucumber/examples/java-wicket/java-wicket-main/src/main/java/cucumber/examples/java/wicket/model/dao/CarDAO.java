package cucumber.examples.java.wicket.model.dao;

import cucumber.examples.java.wicket.model.entity.Car;

public interface CarDAO {
    public void add(Car car);

    Car findAvailableCar();

    int getNumberOfAvailableCars();
}
