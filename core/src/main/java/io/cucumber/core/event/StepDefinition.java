package io.cucumber.core.event;


import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public interface StepDefinition {

    /**
     * The source line where the step definition is defined.
     * Example: com/example/app/Cucumber.test():42
     *
     * @param detail true if extra detailed location information should be included.
     * @return The source line of the step definition.
     */
    String getLocation(boolean detail);

    /**
     * @return the pattern associated with this instance. Used for error reporting only.
     */
    String getPattern();
}
