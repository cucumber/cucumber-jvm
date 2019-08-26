package cucumber.examples.java.wicket;

import cucumber.examples.java.wicket.model.dao.CarDAO;
import cucumber.examples.java.wicket.model.dao.InMemoryCarDAO;
import cucumber.examples.java.wicket.model.entity.Car;
import cucumber.examples.java.wicket.view.Available;
import cucumber.examples.java.wicket.view.Create;
import cucumber.examples.java.wicket.view.Rent;
import org.apache.wicket.protocol.http.WebApplication;

public class Application extends WebApplication {
    private CarDAO carDAO = new InMemoryCarDAO();

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
