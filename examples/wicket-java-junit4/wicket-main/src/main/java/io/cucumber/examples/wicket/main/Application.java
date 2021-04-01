package io.cucumber.examples.wicket.main;

import io.cucumber.examples.wicket.main.model.dao.CarDAO;
import io.cucumber.examples.wicket.main.model.dao.InMemoryCarDAO;
import io.cucumber.examples.wicket.main.model.entity.Car;
import io.cucumber.examples.wicket.main.view.Available;
import io.cucumber.examples.wicket.main.view.Create;
import io.cucumber.examples.wicket.main.view.Rent;
import org.apache.wicket.protocol.http.WebApplication;

public class Application extends WebApplication {

    private final CarDAO carDAO = new InMemoryCarDAO();

    @Override
    protected void init() {
        mountPage("create", Create.class);
        mountPage("available", Available.class);
        mountPage("rent", Rent.class);
    }

    @Override
    public Class<Available> getHomePage() {
        return Available.class;
    }

    public void createCar() {
        Car car = new Car();
        carDAO.add(car);
    }

    public void rentCar() {
        Car car = carDAO.findAvailableCar();
        car.rent();
    }

    public int getNumberOfAvailableCars() {
        return carDAO.getNumberOfAvailableCars();
    }

}
