package io.cucumber.core.exception;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CucumberExceptionTest {

    @TestFactory
    public Collection<DynamicTest> exceptions() {

        return Arrays.asList(

            DynamicTest.dynamicTest("cause", () -> {
                final Executable testMethod = () -> {
                    throw new CucumberException(new RuntimeException());
                };
                final CucumberException expectedThrown = assertThrows(CucumberException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(equalTo("java.lang.RuntimeException"))),
                    () -> assertThat(expectedThrown.getCause(), isA(RuntimeException.class))
                );
            }),

            DynamicTest.dynamicTest("cause null", () -> {
                final Executable testMethod = () -> {
                    throw new CucumberException((Throwable) null);
                };
                final CucumberException expectedThrown = assertThrows(CucumberException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(nullValue())),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue()))
                );
            }),

            DynamicTest.dynamicTest("message", () -> {
                final Executable testMethod = () -> {
                    throw new CucumberException("message");
                };
                final CucumberException expectedThrown = assertThrows(CucumberException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(equalTo("message"))),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue()))
                );
            }),

            DynamicTest.dynamicTest("message null", () -> {
                final Executable testMethod = () -> {
                    throw new CucumberException((String) null);
                };
                final CucumberException expectedThrown = assertThrows(CucumberException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(nullValue())),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue()))
                );
            }),

            DynamicTest.dynamicTest("message, cause", () -> {
                final Executable testMethod = () -> {
                    throw new CucumberException("message", new RuntimeException());
                };
                final CucumberException expectedThrown = assertThrows(CucumberException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(equalTo("message"))),
                    () -> assertThat(expectedThrown.getCause(), isA(RuntimeException.class))
                );
            }),

            DynamicTest.dynamicTest("message null, cause null", () -> {
                final Executable testMethod = () -> {
                    throw new CucumberException(null, null);
                };
                final CucumberException expectedThrown = assertThrows(CucumberException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(nullValue())),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue()))
                );
            })

        );
    }

}
