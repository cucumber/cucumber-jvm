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

public class TooManyInstancesExceptionTest {

    @TestFactory
    public Collection<DynamicTest> exceptions() {

        return Arrays.asList(

            DynamicTest.dynamicTest("Null Instances Collection", () -> {
                Executable testMethod = () -> {
                    throw new TooManyInstancesException(null);
                };
                TooManyInstancesException expectedThrown = assertThrows(TooManyInstancesException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(equalTo("Expected only one instance, but found too many: null"))),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue()))
                );
            }),

            DynamicTest.dynamicTest("Empty Instances Collection", () -> {
                Executable testMethod = () -> {
                    throw new TooManyInstancesException(new ArrayList<>());
                };
                TooManyInstancesException expectedThrown = assertThrows(TooManyInstancesException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(equalTo("Expected only one instance, but found too many: []"))),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue()))
                );
            }),

            DynamicTest.dynamicTest("Instances Collection of One", () -> {
                Executable testMethod = () -> {
                    final Collection<String> instances = new ArrayList<>();
                    instances.add("instanceOne");
                    throw new TooManyInstancesException(instances);
                };
                TooManyInstancesException expectedThrown = assertThrows(TooManyInstancesException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(equalTo("Expected only one instance, but found too many: [instanceOne]"))),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue()))
                );
            }),

            DynamicTest.dynamicTest("Instances Collection of Two", () -> {
                Executable testMethod = () -> {
                    final Collection<String> instances = new ArrayList<>();
                    instances.add("instanceOne");
                    instances.add("instanceTwo");
                    throw new TooManyInstancesException(instances);
                };
                TooManyInstancesException expectedThrown = assertThrows(TooManyInstancesException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(equalTo("Expected only one instance, but found too many: [instanceOne, instanceTwo]"))),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue()))
                );
            })

        );
    }

}
