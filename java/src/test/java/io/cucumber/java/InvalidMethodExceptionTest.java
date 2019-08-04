package io.cucumber.java;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InvalidMethodExceptionTest {

    @TestFactory
    public Collection<DynamicTest> exceptions() {

        return Arrays.asList(

            DynamicTest.dynamicTest("Null Method, Null Glue Code Class", () -> {
                Executable testMethod = () -> {
                    throw InvalidMethodException.createInvalidMethodException(null, null);
                };
                IllegalArgumentException expectedThrown = assertThrows(IllegalArgumentException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(equalTo(
                        "Supplied Method can't be null for InvalidMethodException"
                    ))),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue()))
                );
            }),

            DynamicTest.dynamicTest("Method, Glue Code Class", () -> {
                final Method aMethod = TestingMethodClass.class.getMethod("aMethod");
                Executable testMethod = () -> {
                    throw InvalidMethodException.createInvalidMethodException(aMethod, InvalidMethodExceptionTest.class);
                };
                InvalidMethodException expectedThrown = assertThrows(InvalidMethodException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(equalTo(
                        "You're not allowed to extend classes that define Step Definitions or hooks. class io.cucumber.java.InvalidMethodExceptionTest extends class io.cucumber.java.TestingMethodClass"
                    ))),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue()))
                );
            })

        );
    }

}
