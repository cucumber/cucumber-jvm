package io.cucumber.core.order;

import io.cucumber.core.gherkin.Pickle;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomPickleOrder implements PickleOrder {

    private Random random;

    public RandomPickleOrder() {
        this(new Random());
    }

    public RandomPickleOrder(final String seed) {
        this(Long.parseLong(seed));
    }

    public RandomPickleOrder(final long seed) {
        this(new Random(seed));
    }

    private RandomPickleOrder(final Random random) {
        this.random = random;
    }

    @Override
    public void setArgument(String argument) {
        this.random = new Random(Long.parseLong(argument));
    }

    @Override
    public String getName() {
        return "random";
    }

    @Override
    public List<Pickle> orderPickles(List<Pickle> pickles) {
        Collections.shuffle(pickles, random);
        return pickles;
    }
}
