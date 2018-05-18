package cucumber.runtime.java.test;

import cucumber.api.java.en.Given;

import java.util.List;

public class Stepdefs {
    @Given("I have {int} cukes in the belly")
    public void I_have_cukes_in_the_belly(int arg1) {
    }

    @Given("this data table:")
    public void this_data_table(List<Person> people) throws Throwable {
    }

    public static class Person {
        String first;
        String last;
    }
}
