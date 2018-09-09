package cucumber.api.java8;

import io.cucumber.core.api.Scenario;

public interface HookBody {
    void accept(Scenario scenario) throws Throwable;
}
