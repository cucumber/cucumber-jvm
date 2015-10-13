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

        // No-arg lambda is also OK.
        Before(() -> {});

        Given("^this data table:$", (DataTable peopleTable) -> {
            List<Person> people = peopleTable.asList(Person.class);
            assertEquals("Hellesøy", people.get(0).last);
        });

        // Arity steps
        Given("0 args",
              () -> {});

        Given("1 arg: (.)",
              (String arg1) -> {
                  assertEquals("a", arg1);
              });

        Given("2 args: (.) (.)",
              (String arg1, String arg2) -> {
                  assertEquals("a", arg1);
                  assertEquals("b", arg2);
              });

        Given("3 args: (.) (.) (.)",
              (String arg1, String arg2, String arg3) -> {
                  assertEquals("a", arg1);
                  assertEquals("b", arg2);
                  assertEquals("c", arg3);
              });

        Given("4 args: (.) (.) (.) (.)",
              (String arg1, String arg2, String arg3, String arg4) -> {
                  assertEquals("a", arg1);
                  assertEquals("b", arg2);
                  assertEquals("c", arg3);
                  assertEquals("d", arg4);
              });

        Given("5 args: (.) (.) (.) (.) (.)",
              (String arg1, String arg2, String arg3, String arg4, String arg5) -> {
                  assertEquals("a", arg1);
                  assertEquals("b", arg2);
                  assertEquals("c", arg3);
                  assertEquals("d", arg4);
                  assertEquals("e", arg5);
              });

        Given("6 args: (.) (.) (.) (.) (.) (.)",
              (String arg1, String arg2, String arg3, String arg4, String arg5, String arg6) -> {
                  assertEquals("a", arg1);
                  assertEquals("b", arg2);
                  assertEquals("c", arg3);
                  assertEquals("d", arg4);
                  assertEquals("e", arg5);
                  assertEquals("f", arg6);
              });

        Given("7 args: (.) (.) (.) (.) (.) (.) (.)",
              (String arg1, String arg2, String arg3, String arg4, String arg5, String arg6, String arg7) -> {
                  assertEquals("a", arg1);
                  assertEquals("b", arg2);
                  assertEquals("c", arg3);
                  assertEquals("d", arg4);
                  assertEquals("e", arg5);
                  assertEquals("f", arg6);
              });

        Given("8 args: (.) (.) (.) (.) (.) (.) (.) (.)",
              (String arg1, String arg2, String arg3, String arg4, String arg5, String arg6, String arg7,
               String arg8) -> {
                  assertEquals("a", arg1);
                  assertEquals("b", arg2);
                  assertEquals("c", arg3);
                  assertEquals("d", arg4);
                  assertEquals("e", arg5);
                  assertEquals("f", arg6);
                  assertEquals("g", arg7);
              });

        Given("9 args: (.) (.) (.) (.) (.) (.) (.) (.) (.)",
              (String arg1, String arg2, String arg3, String arg4, String arg5, String arg6, String arg7,
               String arg8, String arg9) -> {
                  assertEquals("a", arg1);
                  assertEquals("b", arg2);
                  assertEquals("c", arg3);
                  assertEquals("d", arg4);
                  assertEquals("e", arg5);
                  assertEquals("f", arg6);
                  assertEquals("g", arg7);
                  assertEquals("h", arg8);
              });
    }

    public static class Person {
        String first;
        String last;
    }
}
