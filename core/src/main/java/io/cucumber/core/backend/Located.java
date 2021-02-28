package io.cucumber.core.backend;

import org.apiguardian.api.API;

import java.util.Optional;

@API(status = API.Status.STABLE)
public interface Located {

    /**
     * @param  stackTraceElement The location of the step.
     * @return                   Return true if this matches the location. This
     *                           is used to filter stack traces.
     */
    boolean isDefinedAt(StackTraceElement stackTraceElement);

    /**
     * Location of step definition. Can either be a a method or stack trace
     * style location.
     * <p>
     * Examples:
     * <ul>
     * <li>
     * {@code com.example.StepDefinitions.given_an_example(io.cucumber.datatable.DataTable) }
     * </li>
     * <li>{@code com.example.StepDefinitions.<init>(StepDefinitions.java:9)}
     * </li>
     * </ul>
     *
     * @return The source line of the step definition.
     */
    String getLocation();

    default Optional<SourceReference> getSourceReference() {
        return Optional.empty();
    }
}
