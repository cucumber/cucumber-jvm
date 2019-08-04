package io.cucumber.core.runner;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UndefinedStepDefinitionExceptionTest {

    @TestFactory
    public Collection<DynamicTest> exceptions() {

        return Arrays.asList(

            DynamicTest.dynamicTest("no args constructor", () -> {
                final Executable testMethod = () -> {
                    throw new UndefinedStepDefinitionException();
                };
                final UndefinedStepDefinitionException expectedThrown = assertThrows(UndefinedStepDefinitionException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(equalTo("No step definitions found"))),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue()))
                );
            })

        );
    }

}
