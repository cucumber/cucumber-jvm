package io.cucumber.java8;

import io.cucumber.datatable.DataTable;
import org.opentest4j.TestAbortedException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LambdaStepDefinitions implements io.cucumber.java8.En {

    private static LambdaStepDefinitions lastInstance;

    private final int outside = 41;

    public LambdaStepDefinitions() {
        DataTableType("[blank]", (Map<String, String> entry) -> {
            Person person = new Person();
            person.first = entry.get("first");
            person.last = entry.get("last");
            return person;
        });

        ParameterType("optional", "[a-z]*", args -> Optional.of(args));

        Before((Scenario scenario) -> {
            assertNotSame(this, lastInstance);
            lastInstance = this;
        });

        BeforeStep((Scenario scenario) -> {
            assertSame(this, lastInstance);
            lastInstance = this;
        });

        AfterStep((Scenario scenario) -> {
            assertSame(this, lastInstance);
            lastInstance = this;
        });

        After((Scenario scenario) -> {
            assertSame(this, lastInstance);
            lastInstance = this;
        });

        Before(this::methodThatDeclaresException);

        Before(this::hookWithArgs);

        Given("this data table:", (DataTable peopleTable) -> {
            List<Person> people = peopleTable.asList(Person.class);
            assertEquals("Hellesøy", people.get(0).last);
            assertEquals("", people.get(1).last);
            assertNull(people.get(3).last);
        });

        Integer alreadyHadThisManyCukes = 1;

        Given("I have {long} cukes in my belly", (Long n) -> {
            assertEquals((Integer) 1, alreadyHadThisManyCukes);
            assertEquals((Long) 42L, n);
        });

        String localState = "hello";
        Then("I really have {int} cukes in my belly", (Integer i) -> {
            assertEquals((Integer) 42, i);
            assertEquals("hello", localState);
        });

        Given("A statement with a simple match", () -> {
            assertTrue(true);
        });

        int localInt = 1;
        Given("A statement with a scoped argument", () -> {
            assertEquals(2, localInt + 1);
            assertEquals(42, outside + 1);
        });

        Given("I will give you {int} and {float} and {word} and {int}",
            (Integer a, Float b, String c, Integer d) -> {
                assertEquals((Integer) 1, a);
                assertEquals((Float) 2.2f, b);
                assertEquals("three", c);
                assertEquals((Integer) 4, d);
            });

        Given("A {optional} generic that is not a data table", (Optional<String> optional) -> {
            assertEquals(Optional.of("string"), optional);
        });

        Given("a step that is skipped", () -> {
            throw new TestAbortedException("skip this");
        });

        Given("A method reference that declares an exception$", this::methodThatDeclaresException);
        Given("A method reference with an argument {int}", this::methodWithAnArgument);
        Given("A method reference with an int argument {int}", this::methodWithAnIntArgument);
        Given("A constructor reference with an argument {string}", Contact::new);
        Given("A static method reference with an argument {int}", LambdaStepDefinitions::staticMethodWithAnArgument);
        Given("A method reference to an arbitrary object of a particular type {string}", Contact::call);
        Given("A method reference to an arbitrary object of a particular type {string} with argument {string}",
            Contact::update);

    }

    private void methodThatDeclaresException() {
    }

    private void hookWithArgs(Scenario scenario) {
    }

    private void methodWithAnArgument(Integer cuckes) {
        assertEquals(42, cuckes.intValue());
    }

    private void methodWithAnIntArgument(int cuckes) {
        assertEquals(42, cuckes);
    }

    public static void staticMethodWithAnArgument(Integer cuckes) {
        assertEquals(42, cuckes.intValue());
    }

    public static class Person {

        String first;
        String last;

    }

    public static class Contact {

        private final String number;

        public Contact(String number) {
            this.number = number;
            assertEquals("42", number);
        }

        public void call() {
            assertEquals("42", number);
        }

        public void update(String number) {
            assertEquals("42", this.number);
            assertEquals("314", number);
        }

    }

}
