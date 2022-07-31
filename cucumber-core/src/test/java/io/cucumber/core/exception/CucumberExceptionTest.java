package io.cucumber.core.exception;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

class CucumberExceptionTest {

    @Test
    void contains_exception() {
        CucumberException expectedThrown = new CucumberException(new RuntimeException());
        assertAll(
            () -> assertThat(expectedThrown.getMessage(), is(equalTo("java.lang.RuntimeException"))),
            () -> assertThat(expectedThrown.getCause(), isA(RuntimeException.class)));
    }

    @Test
    void contains_null() {
        CucumberException expectedThrown = new CucumberException((Throwable) null);
        assertAll(
            () -> assertThat(expectedThrown.getMessage(), is(nullValue())),
            () -> assertThat(expectedThrown.getCause(), is(nullValue())));
    }

    @Test
    void contains_message() {
        CucumberException expectedThrown = new CucumberException("message");
        assertAll(
            () -> assertThat(expectedThrown.getMessage(), is(equalTo("message"))),
            () -> assertThat(expectedThrown.getCause(), is(nullValue())));
    }

    @Test
    void contains_message_null() {
        CucumberException expectedThrown = new CucumberException((String) null);
        assertAll(
            () -> assertThat(expectedThrown.getMessage(), is(nullValue())),
            () -> assertThat(expectedThrown.getCause(), is(nullValue())));
    }

    @Test
    void contains_message_cause() {
        CucumberException expectedThrown = new CucumberException("message", new RuntimeException());
        assertAll(
            () -> assertThat(expectedThrown.getMessage(), is(equalTo("message"))),
            () -> assertThat(expectedThrown.getCause(), isA(RuntimeException.class)));
    }

    @Test
    void contains_message_null_cause_null() {
        CucumberException expectedThrown = new CucumberException(null, null);
        assertAll(
            () -> assertThat(expectedThrown.getMessage(), is(nullValue())),
            () -> assertThat(expectedThrown.getCause(), is(nullValue())));
    }

}
