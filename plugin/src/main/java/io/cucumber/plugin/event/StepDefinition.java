package io.cucumber.plugin.event;


import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public interface StepDefinition {

    /**
     * The source line where the step definition is defined.
     * Example: com/example/app/Cucumber.test():42
     *
     * @return The source line of the step definition.
     */
    String getLocation();

    /**
     * @return the pattern associated with this instance. Used for error reporting only.
     */
    String getPattern();
}
