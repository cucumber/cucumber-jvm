package cucumber.api;

import cucumber.runtime.Argument;

import java.util.List;

public interface Step {
    String getCodeLocation();

    List<Argument> getDefinitionArgument();
}
