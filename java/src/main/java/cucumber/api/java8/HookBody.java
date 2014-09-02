package cucumber.api.java8;

import cucumber.api.Scenario;

@FunctionalInterface
public interface HookBody {
    void accept(Scenario scenario);
}
