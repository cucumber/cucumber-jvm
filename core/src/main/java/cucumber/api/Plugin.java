package cucumber.api;

import java.io.File;
import java.net.URI;
import java.net.URL;

/**
 * Interface for all plugins.
 * <p>
 * A plugin must have a constructor that is either empty
 * or takes a single argument of one of the following types:
 * <ul>
 * <li>{@link Appendable}</li>
 * <li>{@link File}</li>
 * <li>{@link URL}</li>
 * <li>{@link URI}</li>
 * </ul>
 * Plugins must implement one of the following interfaces:
 * <ul>
 * <li>{@link cucumber.api.StepDefinitionReporter}</li>
 * <li>{@link cucumber.api.SummaryPrinter}</li>
 * <li>{@link cucumber.api.formatter.Formatter}</li>
 * </ul>
 */
public interface Plugin {
}
