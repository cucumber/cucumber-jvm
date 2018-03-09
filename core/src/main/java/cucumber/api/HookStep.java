package cucumber.api;

public interface HookStep extends Step {
    HookType getHookType();
}
