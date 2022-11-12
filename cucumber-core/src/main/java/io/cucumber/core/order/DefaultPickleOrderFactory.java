package io.cucumber.core.order;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;

import java.util.Random;

public class DefaultPickleOrderFactory implements PickleOrderFactory {

    private static final Logger log = LoggerFactory.getLogger(DefaultPickleOrderFactory.class);

    @Override
    public PickleOrder create(String name, String argument) {
        if ("reverse".equals(name)) {
            return StandardPickleOrders.reverseLexicalUriOrder();
        }

        if ("lexical".equals(name)) {
            return StandardPickleOrders.lexicalUriOrder();
        }

        if ("random".equals(name)) {
            return StandardPickleOrders.random(seedOrRandom(argument));
        }

        throw new IllegalArgumentException("Invalid order. Must be either reverse, random or random:<long>");
    }

    static long seedOrRandom(String argument) {
        if (canBeLong(argument)) {
            return Long.parseLong(argument);
        }
        long seed = Math.abs(new Random().nextLong());
        log.info(() -> "Using random scenario order. Seed: " + seed);
        return seed;
    }

    static boolean canBeLong(String argument) {
        try {
            Long.parseLong(argument);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
