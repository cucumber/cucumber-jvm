package io.cucumber.core.order.factory;

import io.cucumber.core.order.PickleOrder;
import io.cucumber.core.order.StandardPickleOrders;

public class LexicalUriPickleOrderFactory implements PickleOrderFactory {

    @Override
    public String getName() {
        return "normal";
    }

    @Override
    public PickleOrder create(String argument) {
        return StandardPickleOrders.lexicalUriOrder();
    }
}
