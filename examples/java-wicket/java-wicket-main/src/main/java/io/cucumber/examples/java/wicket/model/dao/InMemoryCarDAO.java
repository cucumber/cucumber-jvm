package io.cucumber.examples.java.wicket.model.dao;

import io.cucumber.examples.java.wicket.model.entity.Car;

import java.util.LinkedList;
import java.util.List;

public class InMemoryCarDAO implements CarDAO {

    private static List<Car> cars;

    public InMemoryCarDAO() {
        if (cars == null) {
            cars = new LinkedList<>();
        }
    }

    @Override
    public void add(Car car) {
        cars.add(car);
    }

    @Override
    public Car findAvailableCar() {
        for (Car car : cars) {
            if (!car.isRented()) {
                return car;
            }
        }
        throw new RuntimeException("No car available");
    }

    @Override
    public int getNumberOfAvailableCars() {
        int availableCars = 0;
        for (Car car : cars) {
            if (!car.isRented()) {
                availableCars++;
            }
        }
        return availableCars;
    }

}
