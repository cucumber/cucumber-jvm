package cucumber.api.formatter;

import cucumber.api.Plugin;
import cucumber.api.event.EventListener;

/**
 * @deprecated as of version 4.0.0; use {@link Plugin } or {@link EventListener } instead.
 */
@Deprecated
public interface Formatter extends EventListener, Plugin {
}
