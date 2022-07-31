package io.cucumber.core.options;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.order.PickleOrder;
import io.cucumber.core.order.StandardPickleOrders;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class PickleOrderParser {

    private static final Logger log = LoggerFactory.getLogger(PickleOrderParser.class);

    private static final Pattern RANDOM_AND_SEED_PATTERN = Pattern.compile("random(?::(\\d+))?");

    static PickleOrder parse(String argument) {
        if ("reverse".equals(argument)) {
            return StandardPickleOrders.reverseLexicalUriOrder();
        }

        if ("lexical".equals(argument)) {
            return StandardPickleOrders.lexicalUriOrder();
        }

        Matcher matcher = RANDOM_AND_SEED_PATTERN.matcher(argument);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid order. Must be either reverse, random or random:<long>");
        }

        final long seed;
        String seedString = matcher.group(1);
        if (seedString != null) {
            seed = Long.parseLong(seedString);
        } else {
            seed = Math.abs(new Random().nextLong());
            log.info(() -> "Using random scenario order. Seed: " + seed);
        }
        return StandardPickleOrders.random(seed);
    }

}
