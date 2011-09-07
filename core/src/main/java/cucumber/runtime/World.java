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
    private ScenarioResultImpl scenarioResult;

    public World(List<Backend> backends, Runtime runtime, Collection<String> tags) {
        this.backends = backends;
        this.runtime = runtime;
        this.tags = tags;
    }

    public void prepare() {
        scenarioResult = new ScenarioResultImpl();
        List<HookDefinition> beforeHooks = new ArrayList<HookDefinition>();
        for (Backend backend : backends) {
            backend.newWorld();
            beforeHooks.addAll(backend.getBeforeHooks());
        }
        Collections.sort(beforeHooks, new HookComparator(true));
        for (HookDefinition hook : beforeHooks) {
            runHookMaybe(hook, null);
        }
    }

    public void dispose() {
        List<HookDefinition> afterHooks = new ArrayList<HookDefinition>();
        for (Backend backend : backends) {
            afterHooks.addAll(backend.getAfterHooks());
        }
        Collections.sort(afterHooks, new HookComparator(false));
        for (HookDefinition hook : afterHooks) {
            runHookMaybe(hook, scenarioResult);
        }
        for (Backend backend : backends) {
            backend.disposeWorld();
        }
    }

    private void runHookMaybe(HookDefinition hook, ScenarioResult scenarioResult) {
        if (hook.matches(tags)) {
            try {
                hook.execute(scenarioResult);
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
            scenarioResult.add(Result.SKIPPED);
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
                scenarioResult.add(result);
                reporter.result(result);
            }
        }
    }

    private final class HookComparator implements Comparator<HookDefinition> {

        final boolean ascending;

        public HookComparator(boolean ascending) {
            this.ascending = ascending;
        }

        @Override
        public int compare(HookDefinition hook1, HookDefinition hook2) {
            int comparison = hook1.getOrder() - hook2.getOrder();
            return ascending ? comparison : -comparison;
        }
    }
}
