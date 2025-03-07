package io.cucumber.junit.platform.engine;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.UnaryOperator;

final class StandardDescriptorOrders {

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

}
