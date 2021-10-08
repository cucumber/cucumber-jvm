package io.cucumber.core.backend;

import org.apiguardian.api.API;

/**
 * Instantiates glue classes. Loaded via SPI.
 * <p>
 * Cucumber scenarios are executed against a test context that consists of
 * multiple glue classes. These must be instantiated and may optionally be
 * injected with dependencies. The object factory facilitates the creation of
 * both the glue classes and dependencies.
 *
 * @see java.util.ServiceLoader
 * @see io.cucumber.core.runtime.ObjectFactoryServiceLoader
 */
@API(status = API.Status.STABLE)
public interface ObjectFactory extends Container, Lookup {

    /**
     * Start the object factory. Invoked once per scenario.
     * <p>
     * While started {@link Lookup#getInstance(Class)} may be invoked.
     */
    void start();

    /**
     * Stops the object factory. Called once per scenario.
     * <p>
     * When stopped the object factory should dispose of all glue instances.
     */
    void stop();

}
