package io.cucumber.core.order.factory;

import io.cucumber.core.order.PickleOrder;
import org.apiguardian.api.API;

/**
 * Responsible for creating {@link PickleOrder}. Factories use
 * {@link java.util.ServiceLoader}.
 * <p>
 * All orders can have a string argument. In that case, the order is defined as
 * {@code <name>:<argument>} (for example {@code random:20} for random with
 * specified seed). Correct factory is selected according to the name part
 * {@link #getName()}. The argument is passed to {@link #create(String)},
 * {@code null} if no argument was provided.
 * <p>
 * To add more, implement {@link PickleOrderFactory} and register it for
 * {@link java.util.ServiceLoader}. The factory is creating instance of
 * {@link PickleOrder}, responsible for ordering pickles.
 */
@API(status = API.Status.EXPERIMENTAL)
public interface PickleOrderFactory {

    /**
     * The name is used as a lookup from {@link java.util.ServiceLoader}.
     */
    String getName();

    /**
     * @param argument Optional parameter, can be null if not viable.
     */
    PickleOrder create(String argument);
}
