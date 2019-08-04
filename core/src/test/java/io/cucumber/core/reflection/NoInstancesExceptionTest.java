package io.cucumber.core.reflection;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NoInstancesExceptionTest {

    @TestFactory
    public Collection<DynamicTest> exceptions() {

        return Arrays.asList(

            DynamicTest.dynamicTest("Null No Instances", () -> {
                Executable testMethod = () -> {
                    throw new NoInstancesException(null);
                };
                NoInstancesException expectedThrown = assertThrows(NoInstancesException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(equalTo("Couldn't find a single implementation of null"))),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue()))
                );
            }),

            DynamicTest.dynamicTest("No Instances", () -> {
                Executable testMethod = () -> {
                    throw new NoInstancesException(NoInstancesExceptionTest.class);
                };
                NoInstancesException expectedThrown = assertThrows(NoInstancesException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(equalTo("Couldn't find a single implementation of class io.cucumber.core.reflection.NoInstancesExceptionTest"))),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue()))
                );
            })

        );
    }


}
