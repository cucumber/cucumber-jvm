package cucumber.api.formatter;

import cucumber.api.Plugin;
import cucumber.api.event.ConcurrentEventListener;

/**
 * This is the interface you should implement if you want your own custom
 * concurrent formatter.
 * <p>
 * @see ConcurrentEventListener
 * @see Plugin
 */
public interface ConcurrentFormatter extends ConcurrentEventListener, Formatter {
}
