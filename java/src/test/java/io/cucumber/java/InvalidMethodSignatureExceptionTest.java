package io.cucumber.java;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InvalidMethodSignatureExceptionTest {

    @TestFactory
    public Collection<DynamicTest> exceptions() {

        return Arrays.asList(

            DynamicTest.dynamicTest("Null Method", () -> {
                Executable testMethod = () -> {
                    throw InvalidMethodSignatureException.builder(null)
                        .build();
                };
                IllegalArgumentException expectedThrown = assertThrows(IllegalArgumentException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(equalTo(
                        "Supplied Method can't be null for InvalidMethodSignatureException"
                    ))),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue()))
                );
            }),

            DynamicTest.dynamicTest("Method", () -> {
                final Method aMethod = TestingMethodClass.class.getMethod("aMethod");
                Executable testMethod = () -> {
                    throw InvalidMethodSignatureException.builder(aMethod)
                        .build();
                };
                InvalidMethodSignatureException expectedThrown = assertThrows(InvalidMethodSignatureException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(equalTo(
                        String.format("A method annotated with  must have one of these signatures:\n * \nat io.cucumber.java.TestingMethodClass.aMethod() in file:%s/target/test-classes/\n\n",
                            Paths.get(".")
                                .normalize()
                                .toAbsolutePath()
                                .toString()
                        )
                    ))),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue()))
                );
            })

        );
    }

}
