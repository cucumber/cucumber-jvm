package io.cucumber.core.api.plugin;

import io.cucumber.core.api.event.EventListener;

/**
 * @deprecated as of version 4.0.0; use {@link EventListener } and {@link Plugin } instead.
 * Optionally, use {@link ColorAware } and/or {@link StrictAware } instead of {@link Plugin }.
 */
@Deprecated
public interface Formatter extends EventListener, Plugin {
}
