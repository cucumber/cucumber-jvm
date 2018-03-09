package cucumber.api;

import java.util.List;

public interface Step {
    String getCodeLocation();

    List<Argument> getDefinitionArgument();
}
