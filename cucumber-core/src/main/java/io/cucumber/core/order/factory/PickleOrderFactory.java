package io.cucumber.core.order.factory;

import io.cucumber.core.order.PickleOrder;

public interface PickleOrderFactory {

    String getName();

    PickleOrder create(String argument);
}
