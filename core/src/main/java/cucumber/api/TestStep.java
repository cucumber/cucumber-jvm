package cucumber.api;

import cucumber.api.event.TestStepFinished;
import cucumber.api.event.TestStepStarted;
import cucumber.runner.EventBus;
import cucumber.runtime.DefinitionMatch;
import cucumber.runtime.StopWatch;
import cucumber.runtime.UndefinedStepDefinitionException;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleStep;

import java.util.Arrays;
import java.util.List;

public abstract class TestStep {
    private static final String[] PENDING_EXCEPTIONS = {
            "org.junit.AssumptionViolatedException",
            "org.junit.internal.AssumptionViolatedException"
    };
    static {
        Arrays.sort(PENDING_EXCEPTIONS);
    }
    private final StopWatch stopWatch;
    protected final DefinitionMatch definitionMatch;

    public TestStep(DefinitionMatch definitionMatch, StopWatch stopWatch) {
        this.definitionMatch = definitionMatch;
        this.stopWatch = stopWatch;
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

    public Result run(EventBus bus, String language, Scenario scenario, boolean skipSteps) {
        bus.send(new TestStepStarted(this));
        String status;
        Throwable error = null;
        stopWatch.start();
        try {
            status = executeStep(language, scenario, skipSteps);
        } catch (Throwable t) {
            error = t;
            status = mapThrowableToStatus(t);
        }
        long duration = stopWatch.stop();
        Result result = mapStatusToResult(status, error, duration);
        bus.send(new TestStepFinished(this, result));
        return result;
    }

    protected String nonExceptionStatus(boolean skipSteps) {
        return skipSteps ? Result.SKIPPED.getStatus() : Result.PASSED;
    }

    protected String executeStep(String language, Scenario scenario, boolean skipSteps) throws Throwable {
        if (!skipSteps) {
            definitionMatch.runStep(language, scenario);
            return Result.PASSED;
        } else {
            definitionMatch.dryRunStep(language, scenario);
            return Result.SKIPPED.getStatus();
        }
    }

    private String mapThrowableToStatus(Throwable t) {
        if (t.getClass().isAnnotationPresent(Pending.class) || Arrays.binarySearch(PENDING_EXCEPTIONS, t.getClass().getName()) >= 0) {
            return Result.PENDING;
        }
        if (t.getClass() == UndefinedStepDefinitionException.class) {
            return Result.UNDEFINED;
        }
        return Result.FAILED;
    }

    private Result mapStatusToResult(String status, Throwable error, long duration) {
        Long resultDuration = duration;
        Throwable resultError = error;
        if (status == Result.SKIPPED.getStatus()) {
            return Result.SKIPPED;
        }
        if (status == Result.UNDEFINED) {
            return new Result(status, 0l, null, definitionMatch.getSnippets());
        }
        return new Result(status, resultDuration, resultError);
    }
}
