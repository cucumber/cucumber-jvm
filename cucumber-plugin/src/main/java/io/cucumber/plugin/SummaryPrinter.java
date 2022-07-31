package io.cucumber.plugin;

import org.apiguardian.api.API;

/**
 * Interface for plugins that print a summary after test execution. Deprecated
 * use the {@link EventListener} or {@link ConcurrentEventListener} interface
 * instead.
 *
 * @see Plugin
 */
@API(status = API.Status.STABLE)
@Deprecated
public interface SummaryPrinter extends Plugin {

}
