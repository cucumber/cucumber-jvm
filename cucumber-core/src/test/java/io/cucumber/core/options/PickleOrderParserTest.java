package io.cucumber.core.options;

import io.cucumber.core.order.RandomPickleOrder;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PickleOrderParserTest {


    @Test
    void randomWithoutSeed() {
        assertThat(PickleOrderParser.parse("random").getClass(), is(RandomPickleOrder.class));
    }

    @Test
    void randomPassingNullArgument() {
        assertThat(PickleOrderParser.parse("random", null).getClass(), is(RandomPickleOrder.class));
    }

    @Test
    void randomPassingSeed() {
        assertThat(PickleOrderParser.parse("random", "200").getClass(), is(RandomPickleOrder.class));
    }

    @Test
    void randomWithSeed() {
        assertThat(PickleOrderParser.parse("random:200").getClass(), is(RandomPickleOrder.class));
    }

    @Test
    void randomWithInvalidArgument() {
        assertThrows(NumberFormatException.class, () -> {
            assertThat(PickleOrderParser.parse("random:invalid").getClass(), is(RandomPickleOrder.class));
        });
    }

    @Test
    void invalidName() {
        assertThrows(IllegalArgumentException.class, () -> {
            assertThat(PickleOrderParser.parse("invalid").getClass(), is(RandomPickleOrder.class));
        });
    }

    @Test
    void invalidNameAndArgument() {
        assertThrows(IllegalArgumentException.class, () -> {
            assertThat(PickleOrderParser.parse("invalidname:invalidargument").getClass(), is(RandomPickleOrder.class));
        });
    }
}
