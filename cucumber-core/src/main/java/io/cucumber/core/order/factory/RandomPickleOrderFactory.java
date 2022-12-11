package io.cucumber.core.order.factory;

import io.cucumber.core.order.PickleOrder;
import io.cucumber.core.order.RandomPickleOrder;

import java.util.Random;

public final class RandomPickleOrderFactory implements PickleOrderFactory {

    @Override
    public String getName() {
        return "random";
    }

    @Override
    public PickleOrder create(String argument) {
        if (argument == null) {
            return random();
        }
        return random(Long.parseLong(argument));
    }

    private PickleOrder random(final long seed) {
        return random(new Random(seed));
    }

    private PickleOrder random() {
        return random(new Random());
    }

    public PickleOrder random(Random random) {
        return new RandomPickleOrder(random);
    }
}
