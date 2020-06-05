package io.cucumber.plugin.event;

import org.apiguardian.api.API;

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
    String getContentType();

    String getMediaType();

    int getLine();

}
