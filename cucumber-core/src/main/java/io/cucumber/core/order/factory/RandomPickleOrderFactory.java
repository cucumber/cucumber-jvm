package io.cucumber.core.order.factory;

import io.cucumber.core.order.PickleOrder;
import io.cucumber.core.order.StandardPickleOrders;

public class RandomPickleOrderFactory implements PickleOrderFactory {

    @Override
    public String getName() {
        return "random";
    }

    @Override
    public PickleOrder create(String argument) {
        if (argument == null) {
            return StandardPickleOrders.random();
        }
        return StandardPickleOrders.random(Long.parseLong(argument));
    }
}
