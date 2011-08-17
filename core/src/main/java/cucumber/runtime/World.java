package cucumber.runtime;

import gherkin.formatter.Reporter;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;

import java.util.List;
import java.util.Locale;

public class World {
    private final List<Backend> backends;
    private final Runtime runtime;
    private boolean skipNextStep = false;
    private static final Object DUMMY_ARG = new Object();

    public World(List<Backend> backends, Runtime runtime) {
        this.backends = backends;
        this.runtime = runtime;
        for (Backend backend : backends) {
            backend.newWorld();
        }
    }

    public void runStep(Step step, String stackTracePath, Reporter reporter, Locale locale) {
        StepDefinitionMatch match = runtime.stepDefinitionMatch(step);
        if (match != null) {
            reporter.match(match);
        } else {
            reporter.match(Match.NONE);
            skipNextStep = true;
        }

        if (skipNextStep) {
            // Undefined steps (Match.NONE) will always get the Result.SKIPPED result
            reporter.result(Result.SKIPPED);
        } else {
            Throwable e = null;
            long start = System.nanoTime();
            try {
                match.runStep(step, stackTracePath, locale);
            } catch (Throwable t) {
                skipNextStep = true;
                e = t;
            } finally {
                long duration = System.nanoTime() - start;
                String status = e == null ? Result.PASSED : Result.FAILED;
                Result result = new Result(status, duration, e, DUMMY_ARG);
                reporter.result(result);
            }
        }
    }

    public void dispose() {
        for (Backend backend : backends) {
            backend.disposeWorld();
        }
    }
}
