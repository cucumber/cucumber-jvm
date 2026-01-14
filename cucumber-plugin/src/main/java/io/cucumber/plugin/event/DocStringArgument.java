package io.cucumber.plugin.event;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

/**
 * Represents a Gherkin doc string argument.
 */
@API(status = API.Status.STABLE)
public interface DocStringArgument extends StepArgument {

    String getContent();

    /**
     * @deprecated use {@link #getMediaType()} instead.
     */
    @Deprecated
    @Nullable
    String getContentType();

    @Nullable
    String getMediaType();

    int getLine();

}
