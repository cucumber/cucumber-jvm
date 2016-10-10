package cucumber.runtime;

import java.util.List;

public interface ArgumentMatcher {
    List<Argument> argumentsFrom(String stepName);
}
