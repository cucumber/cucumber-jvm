package io.cucumber.junit;

import cucumber.runtime.CucumberException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AssertionsTest {

    @Test(expected = CucumberException.class)
    public void should_throw_cucumber_exception_when_annotated() {
        Assertions.assertNoCucumberAnnotatedMethods(WithCucumberMethod.class);
    }

    @RunWith(Cucumber.class)
    final static class WithCucumberMethod {

        @StubCucumberAnnotation
        public void before() {

        }

    }
    @Retention(RetentionPolicy.RUNTIME)
    @interface StubCucumberAnnotation {
    }

}
