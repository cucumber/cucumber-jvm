package io.cucumber.core.order;

import io.cucumber.core.gherkin.Pickle;

import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public final class StandardPickleOrders {

    private StandardPickleOrders() {

    }

    public static PickleOrder lexicalUriOrder() {
        return pickles -> {
            pickles.sort(new PickleUriComparator());
            return pickles;
        };
    }

    public static PickleOrder reverseLexicalUriOrder() {
        return pickles -> {
            pickles.sort(new PickleUriComparator().reversed());
            return pickles;
        };
    }

    public static PickleOrder random(final long seed) {
        return pickles -> {
            Collections.shuffle(pickles, new Random(seed));
            return pickles;
        };
    }

    private static class PickleUriComparator implements Comparator<Pickle> {

        @Override
        public int compare(Pickle a, Pickle b) {
            return a.getUri().compareTo(b.getUri());
        }
    }

}