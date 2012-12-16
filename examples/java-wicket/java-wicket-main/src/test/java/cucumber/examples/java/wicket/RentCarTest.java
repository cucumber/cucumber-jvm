package cucumber.examples.java.wicket;

import cucumber.examples.java.wicket.view.Available;
import cucumber.examples.java.wicket.view.Create;
import cucumber.examples.java.wicket.view.Rent;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RentCarTest {
    private Application application = new Application();
    private WicketTester wicketTester = new WicketTester(application);

    @Test
    public void shouldRentACar() {
        int initialNumberOfCars = 43;

        Create create = wicketTester.startPage(Create.class);
        create.setNumberOfCars(initialNumberOfCars);
        create.create();

        int oneRentedCar = 1;
        int expected = initialNumberOfCars - oneRentedCar;

        Rent rent = wicketTester.startPage(Rent.class);
        rent.rent();

        Available available = wicketTester.startPage(Available.class);
        int actual = available.getAvailableCars();

        assertThat(actual, is(expected));
    }
}
