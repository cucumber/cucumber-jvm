package io.cucumber.core.backend;

import io.cucumber.plugin.event.Step;
import org.apiguardian.api.API;

import java.util.Optional;

@API(status = API.Status.STABLE)
public interface HookDefinition extends Located {

    void execute(TestCaseState state);

    /**
     * Executes the hook with step information. This method is called for
     * {@code @BeforeStep} and {@code @AfterStep} hooks to provide access to
     * step details.
     * <p>
     * The default implementation ignores the step parameter and delegates to
     * {@link #execute(TestCaseState)} for backward compatibility.
     *
     * @param state the current test case state
     * @param step  the step being executed (for step hooks)
     */
    default void execute(TestCaseState state, Step step) {
        execute(state);
    }

    String getTagExpression();

    int getOrder();

    default Optional<HookType> getHookType() {
        return Optional.empty();
    }

    enum HookType {

        BEFORE,

        AFTER,

        BEFORE_STEP,

        AFTER_STEP;
    }
}
