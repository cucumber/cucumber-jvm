package io.cucumber.core.order;

import io.cucumber.core.feature.CucumberPickle;

import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public final class StandardPickleOrders {

    private StandardPickleOrders() {

    }

    public static PickleOrder lexicalUriOrder() {
        return pickleEvents -> {
            pickleEvents.sort(new PickleUriComparator());
            return pickleEvents;
        };
    }

    public static PickleOrder reverseLexicalUriOrder() {
        return pickleEvents -> {
            pickleEvents.sort(new PickleUriComparator().reversed());
            return pickleEvents;
        };
    }

    public static PickleOrder random(final long seed) {
        return pickleEvents -> {
            Collections.shuffle(pickleEvents, new Random(seed));
            return pickleEvents;
        };
    }

    private static class PickleUriComparator implements Comparator<CucumberPickle> {

        @Override
        public int compare(CucumberPickle a, CucumberPickle b) {
            return a.getUri().compareTo(b.getUri());
        }
    }

}