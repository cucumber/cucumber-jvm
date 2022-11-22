package io.cucumber.core.order;

import io.cucumber.core.order.factory.LexicalUriPickleOrderFactory;
import io.cucumber.core.order.factory.RandomPickleOrderFactory;
import io.cucumber.core.order.factory.ReverseLexicalUriPickleOrderFactory;

import java.util.Random;

public final class StandardPickleOrders {

    private StandardPickleOrders() {

    }

    public static PickleOrder lexicalUriOrder() {
        return new LexicalUriPickleOrderFactory().create(null);
    }

    public static PickleOrder reverseLexicalUriOrder() {
        return new ReverseLexicalUriPickleOrderFactory().create(null);
    }

    public static PickleOrder random(final long seed) {
        return new RandomPickleOrderFactory().random(new Random(seed));
    }

}
