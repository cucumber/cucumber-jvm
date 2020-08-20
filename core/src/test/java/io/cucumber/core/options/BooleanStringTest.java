package io.cucumber.core.options;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class BooleanStringTest {
    @Test
    public void no_is_false() {
        assertThat(BooleanString.parseBoolean("no"), is(false));
    }

    @Test
    public void _0_is_false() {
        assertThat(BooleanString.parseBoolean("0"), is(false));
    }

    @Test
    public void false_is_false() {
        assertThat(BooleanString.parseBoolean("false"), is(false));
    }

    @Test
    public void empty_is_false() {
        assertThat(BooleanString.parseBoolean(""), is(false));
    }

    @Test
    public void null_is_false() {
        assertThat(BooleanString.parseBoolean(null), is(false));
    }

    @Test
    public void yes_is_true() {
        assertThat(BooleanString.parseBoolean("yes"), is(true));
    }

    @Test
    public void _1_is_true() {
        assertThat(BooleanString.parseBoolean("1"), is(true));
    }

    @Test
    public void true_is_true() {
        assertThat(BooleanString.parseBoolean("true"), is(true));
    }
}
