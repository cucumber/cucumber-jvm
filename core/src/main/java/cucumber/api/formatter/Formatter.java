package cucumber.api.formatter;

import cucumber.api.Plugin;
import cucumber.api.event.EventListener;

/**
 * This is the interface you should implement if you want your own custom
 * formatter.
 *
 * @see EventListener
 * @see Plugin
 */
public interface Formatter extends EventListener, Plugin {
}
