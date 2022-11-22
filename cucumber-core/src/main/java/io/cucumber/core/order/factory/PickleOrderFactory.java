package io.cucumber.core.order.factory;

import io.cucumber.core.order.PickleOrder;
import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL)
public interface PickleOrderFactory {

    String getName();

    PickleOrder create(String argument);
}
