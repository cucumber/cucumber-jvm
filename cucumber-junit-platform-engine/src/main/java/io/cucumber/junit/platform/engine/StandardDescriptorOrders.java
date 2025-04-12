package io.cucumber.junit.platform.engine;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class StandardDescriptorOrders {
    private static final Logger log = LoggerFactory.getLogger(StandardDescriptorOrders.class);

    private static final Comparator<AbstractCucumberTestDescriptor> lexical = Comparator
            .comparing(AbstractCucumberTestDescriptor::getUri)
            .thenComparing(AbstractCucumberTestDescriptor::getLocation);

    private StandardDescriptorOrders() {

    }

    static UnaryOperator<List<AbstractCucumberTestDescriptor>> lexicalUriOrder() {
        return pickles -> {
            pickles.sort(lexical);
            return pickles;
        };
    }

    static UnaryOperator<List<AbstractCucumberTestDescriptor>> reverseLexicalUriOrder() {
        return pickles -> {
            pickles.sort(lexical.reversed());
            return pickles;
        };
    }

    static UnaryOperator<List<AbstractCucumberTestDescriptor>> random(final long seed) {
        // Invoked multiple times, keep state outside of closure.
        Random random = new Random(seed);
        return pickles -> {
            Collections.shuffle(pickles, random);
            return pickles;
        };
    }

    static UnaryOperator<List<AbstractCucumberTestDescriptor>> parseOrderer(String order) {
        if (order.equals("lexical")) {
            return StandardDescriptorOrders.lexicalUriOrder();
        }
        if (order.equals("reverse")) {
            return StandardDescriptorOrders.reverseLexicalUriOrder();
        }
        Pattern randomAndSeedPattern = Pattern.compile("random(?::(\\d+))?");
        Matcher matcher = randomAndSeedPattern.matcher(order);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid order. Must be either reverse, random or random:<long>");
        }
        final long seed;
        String seedString = matcher.group(1);
        if (seedString != null) {
            seed = Long.parseLong(seedString);
        } else {
            seed = Math.abs(new Random().nextLong());
            log.info(() -> "Using random test descriptor order. Seed: " + seed);
        }
        return StandardDescriptorOrders.random(seed);
    }
}
