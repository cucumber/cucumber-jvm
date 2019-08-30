package io.cucumber.core.backend;

import io.cucumber.docstring.DocStringType;
import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL)
public interface DocStringTypeDefinition {

    DocStringType docStringType();

    /**
     * The source line where the parameter type is defined.
     * Example: com/example/app/Cucumber.test():42
     *
     * @param detail true if extra detailed location information should be included.
     * @return The source line of the step definition.
     */
    String getLocation(boolean detail);
}
