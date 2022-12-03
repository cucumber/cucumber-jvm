package io.cucumber.core.order;

import io.cucumber.core.gherkin.Pickle;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomPickleOrder implements PickleOrder {

    private final Random random;

    public RandomPickleOrder() {
        this(new Random());
    }

    public RandomPickleOrder(long seed) {
        this(new Random(seed));
    }

    public RandomPickleOrder(final Random random) {
        this.random = random;
    }

    @Override
    public List<Pickle> orderPickles(List<Pickle> pickles) {
        Collections.shuffle(pickles, random);
        return pickles;
    }
}
