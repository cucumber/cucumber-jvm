package io.cucumber.junit;

import io.cucumber.core.exception.CucumberException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.runner.RunWith;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssertionsTest {

    @Test
    void should_throw_cucumber_exception_when_annotated() {
        Executable testMethod = () -> Assertions.assertNoCucumberAnnotatedMethods(WithCucumberMethod.class);
        CucumberException expectedThrown = assertThrows(CucumberException.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(equalTo(
            "\n\n" +
                    "Classes annotated with @RunWith(Cucumber.class) must not define any\n" +
                    "Step Definition or Hook methods. Their sole purpose is to serve as\n" +
                    "an entry point for JUnit. Step Definitions and Hooks should be defined\n" +
                    "in their own classes. This allows them to be reused across features.\n" +
                    "Offending class: class io.cucumber.junit.AssertionsTest$WithCucumberMethod\n")));
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface StubCucumberAnnotation {

    }

    @RunWith(Cucumber.class)
    private static final class WithCucumberMethod {

        @StubCucumberAnnotation
        public void before() {

        }

    }

}
