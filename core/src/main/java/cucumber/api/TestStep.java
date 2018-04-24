package cucumber.api;

import java.util.Arrays;
import java.util.List;

import cucumber.api.event.TestStepFinished;
import cucumber.api.event.TestStepStarted;
import cucumber.runner.EventBus;
import cucumber.runtime.AmbiguousStepDefinitionsException;
import cucumber.runtime.DefinitionMatch;
import cucumber.runtime.UndefinedStepDefinitionException;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleStep;

public abstract class TestStep {
    private static final String[] ASSUMPTION_VIOLATED_EXCEPTIONS = {
            "org.junit.AssumptionViolatedException",
            "org.junit.internal.AssumptionViolatedException",
            "org.testng.SkipException"
    };

    static {
        Arrays.sort(ASSUMPTION_VIOLATED_EXCEPTIONS);
    }

    /**
     * @deprecated not part of the public api
     */
    @Deprecated
    protected final DefinitionMatch definitionMatch;

    /**
     * Creates a new test step from the matching step definition
     *
     * @param definitionMatch the matching step definition
     * @deprecated not part of the public api
     */
    @Deprecated
    public TestStep(DefinitionMatch definitionMatch) {
        this.definitionMatch = definitionMatch;
    }

    public String getPattern() {
        return definitionMatch.getPattern();
    }

    public String getCodeLocation() {
        return definitionMatch.getCodeLocation();
    }

    public List<cucumber.runtime.Argument> getDefinitionArgument() {
        return definitionMatch.getArguments();
    }

    public abstract boolean isHook();

    public abstract PickleStep getPickleStep();

    public abstract String getStepText();

    public abstract String getStepLocation();

    public abstract int getStepLine();

    public abstract List<Argument> getStepArgument();

    public abstract HookType getHookType();

    /**
     * @param bus       to which events should be broadcast
     * @param language  in which the step is defined
     * @param scenario  of which this step is part
     * @param skipSteps if this step should be skipped
     * @return result of running this step
     * @deprecated not part of the public api
     */
    @Deprecated
    public Result run(EventBus bus, String language, Scenario scenario, boolean skipSteps, boolean reRunTestCase) {
        Long startTime = bus.getTime();
        bus.send(new TestStepStarted(startTime, this, reRunTestCase));
        Result.Type status;
        Throwable error = null;

        try {
            status = executeStep(language, scenario, skipSteps);
        } catch (Throwable t) {
            error = t;
            status = mapThrowableToStatus(t);
        }
        Long stopTime = bus.getTime();
        Result result = mapStatusToResult(status, error, stopTime - startTime);
        bus.send(new TestStepFinished(stopTime, this, result));
        return result;
    }

    @Deprecated
    protected Result.Type nonExceptionStatus(boolean skipSteps) {
        return skipSteps ? Result.Type.SKIPPED : Result.Type.PASSED;
    }

    @Deprecated
    protected Result.Type executeStep(String language, Scenario scenario, boolean skipSteps) throws Throwable {
        if (!skipSteps) {
            definitionMatch.runStep(language, scenario);
            return Result.Type.PASSED;
        } else {
            definitionMatch.dryRunStep(language, scenario);
            return Result.Type.SKIPPED;
        }
    }

    private Result.Type mapThrowableToStatus(Throwable t) {
        if (t.getClass().isAnnotationPresent(Pending.class)) {
            return Result.Type.PENDING;
        }
        if (Arrays.binarySearch(ASSUMPTION_VIOLATED_EXCEPTIONS, t.getClass().getName()) >= 0) {
            return Result.Type.SKIPPED;
        }
        if (t.getClass() == UndefinedStepDefinitionException.class) {
            return Result.Type.UNDEFINED;
        }
        if (t.getClass() == AmbiguousStepDefinitionsException.class) {
            return Result.Type.AMBIGUOUS;
        }
        return Result.Type.FAILED;
    }

    private Result mapStatusToResult(Result.Type status, Throwable error, long duration) {
        Long resultDuration = duration;
        if (status == Result.Type.SKIPPED && error == null) {
            return Result.SKIPPED;
        }
        if (status == Result.Type.UNDEFINED) {
            return Result.UNDEFINED;
        }
        return new Result(status, resultDuration, error);
    }
}
