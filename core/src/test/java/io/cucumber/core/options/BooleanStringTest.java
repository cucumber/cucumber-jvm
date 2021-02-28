package io.cucumber.core.options;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BooleanStringTest {

    @Test
    void null_is_false() {
        assertThat(BooleanString.parseBoolean(null), is(false));
    }

    @ParameterizedTest
    @ValueSource(strings = { "false", "no", "0" })
    void falsy_values_are_false(String value) {
        assertThat(BooleanString.parseBoolean(value), is(false));
    }

    @ParameterizedTest
    @ValueSource(strings = { "true", "yes", "1" })
    void truthy_values_are_true(String value) {
        assertThat(BooleanString.parseBoolean(value), is(true));
    }

    @ParameterizedTest
    @ValueSource(strings = { "y", "n", "-1", " ", "" })
    void unknown_values_throw_illegal_argument_exception(String value) {
        assertThrows(IllegalArgumentException.class, () -> BooleanString.parseBoolean(value));
    }

}
