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

        Integer alreadyHadThisManyCukes = 1;

        Given("^I have (\\d+) cukes in my belly$", (Long n) -> {
            assertEquals((Integer) 1, alreadyHadThisManyCukes);
            assertEquals((Long) 42L, n);
        });

        String localState = "hello";
        Then("^I really have (\\d+) cukes in my belly", (Integer i) -> {
            assertEquals((Integer) 42, i);
            assertEquals("hello", localState);
        });

        int localInt = 1;
        Given("^A statement with a simple match$", () -> {
            assertEquals(2, localInt+1);
        });

        Given("^I will give you (\\d+) and ([\\d\\.]+) and (\\w+) and (\\d+)$", (Integer a, Float b, String c,
                                                                                 Integer d)
                -> {
            assertEquals((Integer) 1, a);
            assertEquals((Float) 2.2f, b);
            assertEquals("three", c);
            assertEquals((Integer) 4, d);
        });
    }

    public static class Person {
        String first;
        String last;
    }
}
