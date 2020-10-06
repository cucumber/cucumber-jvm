package io.cucumber.java;

import io.cucumber.core.backend.TestCaseState;
import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.StepArgument;
import io.cucumber.plugin.event.TestStep;
import org.apiguardian.api.API;

import java.util.Optional;

/**
 * BeforeStep or AfterStep Hooks that declare a parameter of this type will
 * receive an instance of this class.
 */
@API(status = API.Status.EXPERIMENTAL)
public class Step implements io.cucumber.plugin.event.Step {

    final TestCaseState testCaseState;

    final io.cucumber.plugin.event.Step delegate;

    public Step(TestCaseState state) {
        this.testCaseState = state;
        Optional<TestStep> currentTestStep = state.getCurrentTestStep();
        if (!currentTestStep.isPresent()) {
            throw new IllegalStateException("No current TestStep was found in TestCaseState");
        }
        if (!(currentTestStep.get() instanceof HookTestStep)) {
            throw new IllegalStateException("Current TestStep is not a HookTestStep");
        }
        TestStep relatedTestStep = ((HookTestStep) currentTestStep.get()).getRelatedTestStep();
        if (null == relatedTestStep) {
            throw new IllegalStateException("No related TestStep for current HookTestStep was found");
        }
        if (!(relatedTestStep instanceof PickleStepTestStep)) {
            throw new IllegalStateException("Related TestStep is not a PickleStepTestStep");
        }
        delegate = ((PickleStepTestStep) relatedTestStep).getStep();
    }

    @Override
    public StepArgument getArgument() {
        return delegate.getArgument();
    }

    @Override
    public String getKeyword() {
        return delegate.getKeyword();
    }

    @Override
    public String getText() {
        return delegate.getText();
    }

    @Override
    public int getLine() {
        return delegate.getLine();
    }

    @Override
    public Location getLocation() {
        return delegate.getLocation();
    }

}
