package cucumber.runtime;

import gherkin.formatter.Reporter;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;

import java.util.*;

public class World {
    private static final Object DUMMY_ARG = new Object();

    private final List<Backend> backends;
    private final Runtime runtime;
    private final Collection<String> tags;

    private boolean skipNextStep = false;

    public World(List<Backend> backends, Runtime runtime, Collection<String> tags) {
        this.backends = backends;
        this.runtime = runtime;
        this.tags = tags;
    }
    
    public void prepare() {
        for (Backend backend : backends) {
            backend.newWorld();

            List<HookDefinition> hooks = backend.getBeforeHooks();
            for (HookDefinition hook : hooks) {
                runHookMaybe(hook);
            }
        }
    }

    public void dispose() {
        for (Backend backend : backends) {            
            List<HookDefinition> hooks = backend.getAfterHooks();
            for (HookDefinition hook : hooks) {
                runHookMaybe(hook);
            }
            backend.disposeWorld();
        }
    }

    private void runHookMaybe(HookDefinition hook) {
        if (hook.matches(tags)) {
            try {
                hook.execute();
            } catch (Throwable t) {
                skipNextStep = true;
                throw new CucumberException("Hook execution failed", t);
            }
        }
    }
    
    public void runStep(String uri, Step step, Reporter reporter, Locale locale) {
        StepDefinitionMatch match = runtime.stepDefinitionMatch(uri, step);
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
                match.runStep(locale);
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
}
