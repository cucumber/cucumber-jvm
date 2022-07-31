package io.cucumber.plugin.event;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public final class StepDefinition {

    private final String location;
    private final String pattern;

    public StepDefinition(String location, String pattern) {
        this.location = location;
        this.pattern = pattern;
    }

    /**
     * The source line where the step definition is defined. Example:
     * com/example/app/Cucumber.test():42
     *
     * @return The source line of the step definition.
     */
    public String getLocation() {
        return location;
    }

    /**
     * @return the pattern associated with this instance. Used for error
     *         reporting only.
     */
    public String getPattern() {
        return pattern;
    }

}
