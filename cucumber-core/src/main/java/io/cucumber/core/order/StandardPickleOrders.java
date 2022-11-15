package io.cucumber.core.order;

import io.cucumber.core.gherkin.Pickle;

import java.util.Comparator;

public final class StandardPickleOrders {

    public static final Comparator<Pickle> pickleUriComparator = Comparator.comparing(Pickle::getUri)
            .thenComparing(pickle -> pickle.getLocation().getLine());

    private StandardPickleOrders() {

    }

    public static PickleOrder lexicalUriOrder() {
        return new LexicalUriPickleOrder();
    }

    public static PickleOrder reverseLexicalUriOrder() {
        return new ReverseLexicalUriPickleOrder();
    }

    public static PickleOrder random(final long seed) {
        return new RandomPickleOrder(seed);
    }

}
