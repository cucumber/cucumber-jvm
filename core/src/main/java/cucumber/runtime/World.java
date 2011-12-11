package cucumber.runtime;

import cucumber.runtime.converters.LocalizedXStreams;
import gherkin.formatter.Argument;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class World {
    private static final Object DUMMY_ARG = new Object();

    // TODO - it's expensive to create a new LocalizedXStreams for each scenario - reuse a global one.
    private final LocalizedXStreams localizedXStreams = new LocalizedXStreams();
    private final List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();
    private final List<HookDefinition> beforeHooks = new ArrayList<HookDefinition>();
    private final List<HookDefinition> afterHooks = new ArrayList<HookDefinition>();
    private final ScenarioResultImpl scenarioResult = new ScenarioResultImpl();

    private final Runtime runtime;

    private final Collection<String> tags;

    private boolean skipNextStep = false;

    public World(Runtime runtime, Collection<String> tags) {
        this.runtime = runtime;
        this.tags = tags;
    }

    public void buildBackendWorldsAndRunBeforeHooks(List<String> gluePaths, Reporter reporter) {
        runtime.buildBackendWorlds(gluePaths, this);
        Collections.sort(beforeHooks, new HookComparator(true));
        runHooks(beforeHooks, reporter);
    }

    public void runAfterHooksAndDisposeBackendWorlds(Reporter reporter) {
        Collections.sort(afterHooks, new HookComparator(false));
        runHooks(afterHooks, reporter);
        runtime.disposeBackendWorlds();
    }

    private void runHooks(List<HookDefinition> hooks, Reporter reporter) {
        for (HookDefinition hook : hooks) {
            runHookIfTagsMatch(hook, reporter);
        }
    }

    private void runHookIfTagsMatch(HookDefinition hook, Reporter reporter) {
        if (hook.matches(tags)) {
            long start = System.nanoTime();
            try {
                hook.execute(scenarioResult);
            } catch (Throwable t) {
                skipNextStep = true;

                long duration = System.nanoTime() - start;
                Result result = new Result(Result.FAILED, duration, t, DUMMY_ARG);
                scenarioResult.add(result);
                reporter.result(result);
            }
        }
    }

    public void runStep(String uri, Step step, Reporter reporter, Locale locale) {
        StepDefinitionMatch match = stepDefinitionMatch(uri, step, locale);
        if (match != null) {
            reporter.match(match);
        } else {
            reporter.match(Match.UNDEFINED);
            reporter.result(Result.UNDEFINED);
            skipNextStep = true;
            return;
        }

        if (runtime.isDryRun()) {
            skipNextStep = true;
        }

        if (skipNextStep) {
            scenarioResult.add(Result.SKIPPED);
            reporter.result(Result.SKIPPED);
        } else {
            String status = Result.PASSED;
            Throwable error = null;
            long start = System.nanoTime();
            try {
                match.runStep(locale);
            } catch (Throwable t) {
                error = t;
                status = Result.FAILED;
                runtime.addError(t);
                skipNextStep = true;
            } finally {
                long duration = System.nanoTime() - start;
                Result result = new Result(status, duration, error, DUMMY_ARG);
                scenarioResult.add(result);
                reporter.result(result);
            }
        }
    }

    private StepDefinitionMatch stepDefinitionMatch(String uri, Step step, Locale locale) {
        List<StepDefinitionMatch> matches = stepDefinitionMatches(uri, step);
        try {
            if (matches.size() == 0) {
                runtime.addUndefinedStep(step, locale);
                return null;
            }
            if (matches.size() == 1) {
                return matches.get(0);
            } else {
                throw new AmbiguousStepDefinitionsException(matches);
            }
        } finally {
            runtime.storeStepKeyword(step, locale);
        }
    }

    public void addStepDefinition(StepDefinition stepDefinition) {
        stepDefinitions.add(stepDefinition);
    }

    private List<StepDefinitionMatch> stepDefinitionMatches(String uri, Step step) {
        List<StepDefinitionMatch> result = new ArrayList<StepDefinitionMatch>();
        for (StepDefinition stepDefinition : stepDefinitions) {
            List<Argument> arguments = stepDefinition.matchedArguments(step);
            if (arguments != null) {
                result.add(new StepDefinitionMatch(arguments, stepDefinition, uri, step, localizedXStreams));
            }
        }
        return result;
    }

    public void addBeforeHook(HookDefinition hookDefinition) {
        beforeHooks.add(hookDefinition);
    }

    public void addAfterHook(HookDefinition hookDefinition) {
        afterHooks.add(hookDefinition);
    }

    public List<HookDefinition> getBeforeHooks() {
        return beforeHooks;
    }

    public List<HookDefinition> getAfterHooks() {
        return afterHooks;
    }

    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
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
