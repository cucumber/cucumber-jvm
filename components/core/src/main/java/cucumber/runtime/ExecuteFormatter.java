package cucumber.runtime;

import gherkin.formatter.Argument;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ExecuteFormatter implements Formatter {
    private List<Backend> backends;
    private final Reporter reporter;

    private String uri;
    private Feature feature;
    private List<Step> undefinedSteps = new ArrayList<Step>();
    private List<Step> backgroundSteps = new ArrayList<Step>();
    private List<Step> scenarioSteps = new ArrayList<Step>();
    private List<Step> scenarioOutlineSteps = new ArrayList<Step>();
    private List<Step> steps;
    private String featureElementClassName;
    private boolean scenarioOutlineReported;

    public ExecuteFormatter(List<Backend> backends, Reporter reporter) {
        this.backends = backends;
        this.reporter = reporter;
    }

    public void uri(String uri) {
        this.uri = uri;
        reporter.uri(uri);
    }

    public void feature(Feature feature) {
        this.feature = feature;
        backgroundSteps.clear();
        reporter.feature(feature);
    }

    public void background(Background background) {
        setFeatureElementClassNameFrom(background);
        backgroundSteps.clear();
        steps = backgroundSteps;
        reporter.background(background);
    }

    public void scenario(Scenario scenario) {
        executePrevious();
        setFeatureElementClassNameFrom(scenario);
        scenarioSteps.clear();
        steps = scenarioSteps;
        reporter.scenario(scenario);
    }

    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        executePrevious();
        setFeatureElementClassNameFrom(scenarioOutline);
        scenarioOutlineSteps.clear();
        steps = scenarioOutlineSteps;
        scenarioOutlineReported = false;
        reporter.scenarioOutline(scenarioOutline);
    }

    public void examples(Examples examples) {
        executePrevious();
        reporter.examples(examples);

        List<Row> rows = new ArrayList<Row>(examples.getRows());
        Row headerRow = rows.remove(0);
        for (Row example : rows) {
            executeExample(headerRow, example);
        }
    }

    public void step(Step step) {
        steps.add(step);
        reporter.step(step);
    }

    public void eof() {
        executePrevious();
        reporter.eof();
    }

    public void syntaxError(String state, String event, List<String> legalEvents, String uri, int line) {
        throw new UnsupportedOperationException();
    }

    private void executePrevious() {
        for (Backend backend : backends) {
            backend.newScenario();
        }
        boolean skip = false;
        for (Step step : backgroundSteps) {
            skip = execute(step, skip);
        }
        for (Step step : scenarioSteps) {
            skip = execute(step, skip);
        }
        scenarioSteps.clear();

        if(!scenarioOutlineReported) {
            for (Step step : scenarioOutlineSteps) {
                reporter.match(step.getOutlineMatch(uri + ":" + step.getLine()));
                reporter.result(Result.SKIPPED);
            }
            scenarioOutlineReported = true;
        }
    }

    private void executeExample(Row headerRow, Row example) {
        boolean skip = false; // TODO: Add ability to instantiate entire runner with skip=false, for dry runs
        for (Step exampleStep : createExampleSteps(headerRow, example)) {
//            System.out.println("exampleStep = " + exampleStep);
            skip = execute(exampleStep, skip);
        }
    }

    private List<Step> createExampleSteps(Row headerRow, Row example) {
        List<Step> result = new ArrayList<Step>();
        // TODO: Make this based on a copy of steps, because we need to clear this to prevent it from running again!!
        for (Step outlineStep : scenarioOutlineSteps) {
            result.add(outlineStep.createExampleStep(headerRow, example));
        }
        return result;
    }

    private boolean execute(Step step, boolean skip) {
        StackTraceElement stepStackTraceElement = createStackTraceElement(step);
        StepRunner stepRunner = stepRunner(step, stepStackTraceElement);
        return stepRunner.execute(skip, reporter, stepStackTraceElement);
    }

    private StackTraceElement createStackTraceElement(Step step) {
        return new StackTraceElement(featureElementClassName, step.getKeyword() + step.getName(), uri, step.getLine());
    }

    private void setFeatureElementClassNameFrom(DescribedStatement featureElement) {
        featureElementClassName =  feature.getName() + "." + featureElement.getName();
    }

    private StepRunner stepRunner(Step step, StackTraceElement stepStackTraceElement) {
        List<StepDefinitionMatch> matches = stepDefinitionMatches(step);
        if (matches.size() == 0) {
            undefinedSteps.add(step);
            return new UndefinedStepRunner(stepStackTraceElement, step.getMatchedColumns());
        }
        if (matches.size() == 1) {
            return matches.get(0);
        } else {
            // TODO: Ambiguous for > 1
            throw new RuntimeException("TODO: Support ambiguous matches");
        }
    }

    private List<StepDefinitionMatch> stepDefinitionMatches(Step step) {
        List<StepDefinitionMatch> result = new ArrayList<StepDefinitionMatch>();
        for (Backend backend : backends) {
            for (StepDefinition stepDefinition : backend.getStepDefinitions()) {
                List<Argument> arguments = stepDefinition.matchedArguments(step);
                if (arguments != null) {
                    result.add(new StepDefinitionMatch(arguments, stepDefinition, step.getMatchedColumns()));
                }
            }
        }
        return result;
    }

    // TODO: Convert "And" and "But" to the Given/When/Then keyword above.
    // If there is none, use Given.
    // This is hard with current structure. Maybe we really need a full AST like thing,
    // where Scenario has *Step etc.
    private List<String> getSnippets() {
        Collections.sort(undefinedSteps, new Comparator<Step>() {
            public int compare(Step a, Step b) {
                int keyword = a.getKeyword().compareTo(b.getKeyword());
                if(keyword == 0) {
                    return a.getName().compareTo(b.getName());
                } else {
                    return keyword;
                }
            }
        });

        List<String> snippets = new ArrayList<String>();
        for (Step step : undefinedSteps) {
            for (Backend backend : backends) {
                String snippet = backend.getSnippet(step);
                if(!snippets.contains(snippet)) {
                    snippets.add(snippet);
                }
            }
        }
        return snippets;
    }

    public void reportSummary(SummaryReporter summaryReporter) {
        summaryReporter.snippets(getSnippets());
    }
}
