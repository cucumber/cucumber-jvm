package cucumber.api.formatter;

import cucumber.api.Plugin;
import cucumber.api.event.EventListener;

/**
 * @deprecated as of version 4.0.0; use {@link EventListener } and {@link Plugin } instead.
 * Optionally, use {@link ColorAware } and/or {@link StrictAware } instead of {@link Plugin }.
 */
@Deprecated
public interface Formatter extends EventListener, Plugin {
}
