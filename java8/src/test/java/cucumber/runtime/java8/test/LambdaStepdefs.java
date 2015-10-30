package cucumber.runtime.java8.test;

import cucumber.api.DataTable;
import cucumber.api.Scenario;
import cucumber.api.java8.En;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class LambdaStepdefs implements En {
    private static LambdaStepdefs lastInstance;

    public LambdaStepdefs() {
        Before((Scenario scenario) -> {
            assertNotSame(this, lastInstance);
            lastInstance = this;
        });

        Given("^this data table:$", (DataTable peopleTable) -> {
            List<Person> people = peopleTable.asList(Person.class);
            assertEquals("Hellesøy", people.get(0).last);
        });
        String localState = "hello";
        Given("^I have (\\d+) cukes in my belly", (Integer i) -> {
            assertEquals((Integer) 42, i);
            assertEquals("hello", localState);
        });
        int localInt = 1;
        Given("^A statement with a simple match$", () -> {
            assertEquals(2, localInt+1);
        });
    }

    public static class Person {
        String first;
        String last;
    }
}
