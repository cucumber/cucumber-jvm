package cucumber.runtime.java8.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import cucumber.api.DataTable;
import cucumber.api.Scenario;
import cucumber.api.java8.En;

import java.util.List;

public class LambdaStepdefs implements En {
    private static LambdaStepdefs lastInstance;

    private final int outside = 41;

    public LambdaStepdefs() {
        Before((Scenario scenario) -> {
            assertNotSame(this, lastInstance);
            lastInstance = this;
        });

        Before(this::methodThatDeclaresException);

        Before(this::hookWithArgs);


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

        Given("^A statement with a simple match$", () -> {
            assertTrue(true);
        });

        int localInt = 1;
        Given("^A statement with a scoped argument$", () -> {
            assertEquals(2, localInt + 1);
            assertEquals(42, outside + 1);
        });

        Given("^I will give you (\\d+) and ([\\d\\.]+) and (\\w+) and (\\d+)$", (Integer a, Float b, String c,
                                                                                 Integer d)
            -> {
            assertEquals((Integer) 1, a);
            assertEquals((Float) 2.2f, b);
            assertEquals("three", c);
            assertEquals((Integer) 4, d);
        });

        Given("^A method reference that declares an exception$", this::methodThatDeclaresException);
        Given("^A method reference with an argument (\\d+)$", this::methodWithAnArgument);
        Given("^A constructor reference with an argument (.*)$", Contact::new);
        Given("^A static method reference with an argument (\\d+)$", LambdaStepdefs::staticMethodWithAnArgument);

        Given("^A method reference to an arbitrary object of a particular type (\\d+)$", Contact::call);
        Given("^A method reference to an arbitrary object of a particular type (.*) with argument (.*)$", Contact::update);

    }

    private void methodThatDeclaresException() throws Throwable {
    }
    private void methodWithAnArgument(Integer cuckes) throws Throwable {
        assertEquals(42, cuckes.intValue());
    }

    public static void staticMethodWithAnArgument(Integer cuckes) throws Throwable {
        assertEquals(42, cuckes.intValue());
    }


    private void hookWithArgs(Scenario scenario) throws Throwable {
    }

    public static class Person {
        String first;
        String last;
    }

    public static class Contact {

        private final String number;

        public Contact(String number){
            this.number = number;
            assertEquals("42", number);
        }

        public void call(){
            assertEquals("42", number);
        }

        public void update(String number){
            assertEquals("42", this.number);
            assertEquals("314", number);
        }
    }


}
