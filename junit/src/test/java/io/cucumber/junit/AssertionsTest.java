package io.cucumber.junit;

import io.cucumber.core.exception.CucumberException;
import org.junit.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.runner.RunWith;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AssertionsTest {

    @Test
    public void should_throw_cucumber_exception_when_annotated() {
        Executable testMethod = () -> Assertions.assertNoCucumberAnnotatedMethods(WithCucumberMethod.class);
        CucumberException expectedThrown = assertThrows(CucumberException.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(equalTo("\n\nClasses annotated with @RunWith(Cucumber.class) must not define any\nStep Definition or Hook methods. Their sole purpose is to serve as\nan entry point for JUnit. Step Definitions and Hooks should be defined\nin their own classes. This allows them to be reused across features.\nOffending class: class io.cucumber.junit.AssertionsTest$WithCucumberMethod\n")));
    }

    @RunWith(Cucumber.class)
    public static final class WithCucumberMethod {

        @StubCucumberAnnotation
        public void before() {

        }

    }
    @Retention(RetentionPolicy.RUNTIME)
    @interface StubCucumberAnnotation {
    }

}
