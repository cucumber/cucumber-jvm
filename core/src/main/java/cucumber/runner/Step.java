package cucumber.runner;

import cucumber.api.Pending;
import cucumber.api.Result;
import cucumber.api.Scenario;
import cucumber.api.event.TestStepFinished;
import cucumber.api.event.TestStepStarted;
import cucumber.runtime.AmbiguousStepDefinitionsException;
import cucumber.runtime.DefinitionMatch;
import cucumber.runtime.UndefinedStepDefinitionException;

import java.util.Arrays;
import java.util.List;

abstract class Step implements cucumber.api.Step {
    private static final String[] ASSUMPTION_VIOLATED_EXCEPTIONS = {
            "org.junit.AssumptionViolatedException",
            "org.junit.internal.AssumptionViolatedException"
    };

    static {
        Arrays.sort(ASSUMPTION_VIOLATED_EXCEPTIONS);
    }

    final DefinitionMatch definitionMatch;

    Step(DefinitionMatch definitionMatch) {
        this.definitionMatch = definitionMatch;
    }

    @Override
    public String getCodeLocation() {
        return definitionMatch.getCodeLocation();
    }

    @Override
    public List<cucumber.runtime.Argument> getDefinitionArgument() {
        return definitionMatch.getArguments();
    }

    Result run(EventBus bus, String language, Scenario scenario, boolean skipSteps) {
        Long startTime = bus.getTime();
        bus.send(new TestStepStarted(startTime, this));
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

    private Result.Type executeStep(String language, Scenario scenario, boolean skipSteps) throws Throwable {
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
