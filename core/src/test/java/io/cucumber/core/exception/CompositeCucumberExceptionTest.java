package io.cucumber.core.exception;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

class CompositeCucumberExceptionTest {

    @Test
    void throws_for_zero_exceptions() {
        final List<Throwable> causes = Collections.emptyList();
        CompositeCucumberException expectedThrown = new CompositeCucumberException(causes);
        assertAll(
            () -> assertThat(expectedThrown.getMessage(), is(equalTo("There were 0 exceptions:\n"))),
            () -> assertThat(expectedThrown.getCause(), is(nullValue())),
            () -> assertThat(expectedThrown.getCauses(), is(equalTo(causes))));
    }

    @Test
    void throws_for_one_exception() {
        final List<Throwable> causes = Collections.singletonList(new IllegalArgumentException());
        CompositeCucumberException expectedThrown = new CompositeCucumberException(causes);
        assertAll(
            () -> assertThat(expectedThrown.getMessage(),
                is(equalTo("There were 1 exceptions:\n  java.lang.IllegalArgumentException(null)"))),
            () -> assertThat(expectedThrown.getCause(), is(nullValue())),
            () -> assertThat(expectedThrown.getCauses(), is(equalTo(causes))));
    }

    @Test
    void throws_for_two_exceptions() {
        final List<Throwable> causes = Arrays.asList(new IllegalArgumentException(), new RuntimeException());
        CompositeCucumberException expectedThrown = new CompositeCucumberException(causes);
        assertAll(
            () -> assertThat(expectedThrown.getMessage(), is(equalTo(
                "There were 2 exceptions:\n  java.lang.IllegalArgumentException(null)\n  java.lang.RuntimeException(null)"))),
            () -> assertThat(expectedThrown.getCause(), is(nullValue())),
            () -> assertThat(expectedThrown.getCauses(), is(equalTo(causes))));
    }

}
