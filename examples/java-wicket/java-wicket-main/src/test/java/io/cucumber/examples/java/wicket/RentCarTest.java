package io.cucumber.examples.java.wicket;

import io.cucumber.examples.java.wicket.view.Available;
import io.cucumber.examples.java.wicket.view.Create;
import io.cucumber.examples.java.wicket.view.Rent;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class RentCarTest {

    private final Application application = new Application();
    private final WicketTester wicketTester = new WicketTester(application);

    @Test
    void shouldRentACar() {
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
