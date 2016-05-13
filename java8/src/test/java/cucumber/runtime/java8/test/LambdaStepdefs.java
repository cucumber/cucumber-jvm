package cucumber.runtime.java8.test;

import cucumber.api.DataTable;
import cucumber.api.PendingException;
import cucumber.api.Scenario;
import cucumber.api.java8.En;
import cucumber.api.java8.StepdefBody;

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
        Integer alreadyHadThisManyCukes = 1;
        Given("^I have 42 cukes in my belly$", () -> {
            assertEquals((Integer) 1, alreadyHadThisManyCukes);
        });
    }

    public static class Person {
        String first;
        String last;
    }
}
