package io.cucumber.plugin.event;

import org.apiguardian.api.API;

/**
 * Represents a Gherkin doc string argument.
 */
@API(status = API.Status.STABLE)
public interface DocStringArgument extends StepArgument {
    String getContent();

    String getContentType();

    int getLine();
}
