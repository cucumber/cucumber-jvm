package cucumber.runtime;

import cucumber.runtime.converters.LocalizedXStreams;
import gherkin.formatter.Argument;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;

import java.util.*;

public class World {
    private static final Object DUMMY_ARG = new Object();

    // TODO - it's expensive to create a new LocalizedXStreams for each scenario - reuse a global one.
    private final LocalizedXStreams localizedXStreams = new LocalizedXStreams();
    private final List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();
    private final List<HookDefinition> beforeHooks = new ArrayList<HookDefinition>();
    private final List<HookDefinition> afterHooks = new ArrayList<HookDefinition>();

    private final Runtime runtime;
    private final Collection<String> tags;

    private boolean skipNextStep = false;
    private ScenarioResultImpl scenarioResult;

    public World(Runtime runtime, Collection<String> tags) {
        this.runtime = runtime;
        this.tags = tags;
    }

    public void prepare(List<String> gluePaths) {
        runtime.buildWorlds(gluePaths, this);

        scenarioResult = new ScenarioResultImpl();
        Collections.sort(beforeHooks, new HookComparator(true));
        for (HookDefinition hook : beforeHooks) {
            // We're passing in null deliberately so Before hooks can't access the result.
            runHookMaybe(hook, null);
        }
    }

    private List<StepDefinitionMatch> stepDefinitionMatches(String uri, Step step) {
        List<StepDefinitionMatch> result = new ArrayList<StepDefinitionMatch>();
        for (StepDefinition stepDefinition : stepDefinitions) {
            List<Argument> arguments = stepDefinition.matchedArguments(step);
            if (arguments != null && stepDefinition.matches(tags)) {
                result.add(new StepDefinitionMatch(arguments, stepDefinition, uri, step, localizedXStreams));
            }
        }
        return result;
    }

    public void dispose() {
        Collections.sort(afterHooks, new HookComparator(false));
        for (HookDefinition hook : afterHooks) {
            runHookMaybe(hook, scenarioResult);
        }
        runtime.disposeWorlds();
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

    public Throwable runStep(String uri, Step step, Reporter reporter, Locale locale) {
        StepDefinitionMatch match = stepDefinitionMatch(uri, step);
        if (match != null) {
            reporter.match(match);
        } else {
            reporter.match(Match.UNDEFINED);
            reporter.result(Result.UNDEFINED);
            skipNextStep = true;
            return null;
        }

        if (runtime.isDryRun()) {
            skipNextStep = true;
        }

        Throwable e = null;
        if (skipNextStep) {
            scenarioResult.add(Result.SKIPPED);
            reporter.result(Result.SKIPPED);
        } else {
            long start = System.nanoTime();
            try {
                match.runStep(locale);
            } catch (Throwable t) {
                skipNextStep = true;
                e = t;
                runtime.addError(e);
            } finally {
                long duration = System.nanoTime() - start;
                String status = e == null ? Result.PASSED : Result.FAILED;
                Result result = new Result(status, duration, e, DUMMY_ARG);
                scenarioResult.add(result);
                reporter.result(result);
            }
        }
        return e;
    }

    private StepDefinitionMatch stepDefinitionMatch(String uri, Step step) {
        List<StepDefinitionMatch> matches = stepDefinitionMatches(uri, step);
        if (matches.size() == 0) {
            runtime.addUndefinedStep(step);
            return null;
        }
        if (matches.size() == 1) {
            return matches.get(0);
        } else {
            throw new AmbiguousStepDefinitionsException(matches);
        }
    }

    public void addStepDefinition(StepDefinition stepDefinition) {
        stepDefinitions.add(stepDefinition);
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
