package cucumber.api;

import java.io.File;
import java.net.URI;
import java.net.URL;

/**
 * Marker interface for all plugins.
 * <p>
 * A plugin can be added to the runtime to listen in on step definition, summary printing and test
 * execution.
 * <p>
 * Plugins are added to the runtime from the command line or @{@link CucumberOptions} and may be provided with a
 * parameter. To accept this parameter the plugin must have a public constructor that accepts one of the following
 * arguments:
 * <ul>
 * <li>{@link String}</li>
 * <li>{@link Appendable}</li>
 * <li>{@link URI}</li>
 * <li>{@link URL}</li>
 * <li>{@link File}</li>
 * </ul>
 * <p>
 * To make the parameter optional the plugin must also have a public default constructor.
 * <p>
 * Plugins may also implement one of these interfaces:
 * <ul>
 * <li>{@link cucumber.api.formatter.ColorAware}</li>
 * <li>{@link cucumber.api.formatter.StrictAware}</li>
 * <li>{@link cucumber.api.event.EventListener}</li>
 * <li>{@link cucumber.api.StepDefinitionReporter}</li>
 * <li>{@link cucumber.api.SummaryPrinter}</li>
 * </ul>
 */
public interface Plugin {
}
