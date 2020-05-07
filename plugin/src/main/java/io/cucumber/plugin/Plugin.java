package io.cucumber.plugin;

import org.apiguardian.api.API;

import java.io.File;
import java.net.URI;
import java.net.URL;

/**
 * Marker interface for all plugins.
 * <p>
 * A plugin can be added to the runtime to listen in on step definition, summary
 * printing and test execution.
 * <p>
 * Plugins are added to the runtime from the command line or by annotating a
 * runner class with {@code @CucumberOptions} and may be provided with a
 * parameter using this syntax {@code com.example.MyPlugin:path/to/output.json}.
 * To accept this parameter the plugin must have a public constructor that
 * accepts one of the following arguments:
 * <ul>
 * <li>{@link String}</li>
 * <li>{@link Appendable}</li>
 * <li>{@link URI}</li>
 * <li>{@link URL}</li>
 * <li>{@link File}</li>
 * </ul>
 * <p>
 * To make the parameter optional the plugin must also have a public default
 * constructor.
 * <p>
 * Plugins may also implement one of these interfaces:
 * <ul>
 * <li>{@link ColorAware}</li>
 * <li>{@link StrictAware}</li>
 * <li>{@link EventListener}</li>
 * <li>{@link ConcurrentEventListener}</li>
 * <li>{@link SummaryPrinter}</li>
 * </ul>
 */
@API(status = API.Status.STABLE)
public interface Plugin {

}
