package io.cucumber.core.order.factory;

import io.cucumber.core.order.PickleOrder;
import org.apiguardian.api.API;

/**
 * Responsible for creating {@link PickleOrder}. Factories use
 * {@link java.util.ServiceLoader}.
 */
@API(status = API.Status.EXPERIMENTAL)
public interface PickleOrderFactory {

    /**
     * The name is used as a lookup when providing the name of order from a
     * command line.
     */
    String getName();

    PickleOrder create(String argument);
}
