package io.cucumber.core.exception;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CompositeCucumberExceptionTest {

    @TestFactory
    public Collection<DynamicTest> exceptions() {

        return Arrays.asList(

            DynamicTest.dynamicTest("Null Causes Collection", () -> {
                final Executable testMethod = () -> {
                    throw new CompositeCucumberException(null);
                };
                final CompositeCucumberException expectedThrown = assertThrows(CompositeCucumberException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(equalTo("There were 0 exceptions:"))),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue())),
                    () -> assertThat(expectedThrown.getCauses(), is(equalTo(new ArrayList<Throwable>())))
                );
            }),

            DynamicTest.dynamicTest("Empty Causes Collection", () -> {
                final Executable testMethod = () -> {
                    throw new CompositeCucumberException(new ArrayList<>());
                };
                final CompositeCucumberException expectedThrown = assertThrows(CompositeCucumberException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(equalTo("There were 0 exceptions:"))),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue())),
                    () -> assertThat(expectedThrown.getCauses(), is(equalTo(new ArrayList<Throwable>())))
                );
            }),

            DynamicTest.dynamicTest("Causes Collection of One", () -> {
                final List<Throwable> causes = new ArrayList<>();
                causes.add(new IllegalArgumentException());
                final Executable testMethod = () -> {
                    throw new CompositeCucumberException(causes);
                };
                final CompositeCucumberException expectedThrown = assertThrows(CompositeCucumberException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(equalTo("There were 1 exceptions:\n  java.lang.IllegalArgumentException(null)"))),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue())),
                    () -> assertThat(expectedThrown.getCauses(), is(equalTo(causes)))
                );
            }),

            DynamicTest.dynamicTest("Causes Collection of Two", () -> {
                final List<Throwable> causes = new ArrayList<>();
                causes.add(new IllegalArgumentException());
                causes.add(new RuntimeException());
                final Executable testMethod = () -> {
                    throw new CompositeCucumberException(causes);
                };
                final CompositeCucumberException expectedThrown = assertThrows(CompositeCucumberException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(equalTo("There were 2 exceptions:\n  java.lang.IllegalArgumentException(null)\n  java.lang.RuntimeException(null)"))),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue())),
                    () -> assertThat(expectedThrown.getCauses(), is(equalTo(causes)))
                );
            })

        );
    }

}
