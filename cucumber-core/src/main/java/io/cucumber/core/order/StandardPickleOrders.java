package io.cucumber.core.order;

import io.cucumber.core.gherkin.Pickle;

import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public final class StandardPickleOrders {

    private static final Comparator<Pickle> pickleUriComparator = Comparator.comparing(Pickle::getUri)
            .thenComparing(pickle -> pickle.getLocation().getLine());

    private StandardPickleOrders() {

    }

    public static PickleOrder lexicalUriOrder() {
        return pickles -> {
            pickles.sort(pickleUriComparator);
            return pickles;
        };
    }

    public static PickleOrder reverseLexicalUriOrder() {
        return pickles -> {
            pickles.sort(pickleUriComparator.reversed());
            return pickles;
        };
    }

    public static PickleOrder random(final long seed) {
        return random(new Random(seed));
    }

    public static PickleOrder random() {
        return random(new Random());
    }

    private static PickleOrder random(Random random) {
        return pickles -> {
            Collections.shuffle(pickles, random);
            return pickles;
        };
    }

}
