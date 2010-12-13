package cucumber.runtime;

import gherkin.formatter.Argument;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExecuteFormatter implements Formatter {
    private final Backend backend;
    private final Reporter reporter;
    private final List<Step> steps = new ArrayList<Step>();
    private List<CellResult> cellResults;

    private String uri;
    private Feature feature;
    private DescribedStatement featureElement;
    private StepResultHandler stepResultHandler;
    private Map<Step, List<CellResult>> matchedResultsByStep = new HashMap<Step, List<CellResult>>();
    private Row examplesHeaderRow;
    private String featureElementClassName;

    public ExecuteFormatter(Backend backend, Reporter reporter) {
        this.backend = backend;
        this.reporter = reporter;
    }

    public void uri(String uri) {
        this.uri = uri;
        reporter.uri(uri);
    }

    public void feature(Feature feature) {
        this.feature = feature;
        reporter.feature(feature);
    }

    public void background(Background background) {
        reporter.background(background);
    }

    public void scenario(Scenario scenario) {
        replayPreviousFeatureElement();
        featureElement = scenario;
        stepResultHandler = new ScenarioStepResultHandler(this);
    }

    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        replayPreviousFeatureElement();
        featureElement = scenarioOutline;
    }

    public void examples(Examples examples) {
        replayPreviousFeatureElement();
        examples.replay(reporter);
        reporter.table(examples.getRows());

        List<Row> rows = new ArrayList<Row>(examples.getRows());
        examplesHeaderRow = rows.remove(0);
        reporter.row(examplesHeaderRow.createResults("skipped_arg"));
        reporter.nextRow();

        for (Row example : rows) {
            cellResults = example.createResults("executing");
            executeExample(example);
        }
    }

    private void executeExample(Row example) {
        // TODO: maybe the handler holds a list to the cellResults too??
        stepResultHandler = new ExampleStepResultHandler(this);
        List<Step> Steps = createExampleSteps(example);
        boolean skip = false; // TODO: Add ability to instantiate entire runner with skip=false, for dry runs
        for (Step step : Steps) {
            skip = execute(step, skip);

            // TODO: print the row again for each step. A cell might have changed. A cell might even change twice.
            // :-) We have the cellResults array to help us with that yay!!
            //reporter.row(formats);
        }
        reporter.nextRow();
        // TODO: print all the exceptions.
    }

    private List<Step> createExampleSteps(Row example) {
        List<Step> result = new ArrayList<Step>();
        for (Step outlineStep : steps) {
            result.add(exampleStep(outlineStep, example));
        }
        return result;
    }

    private Step exampleStep(Step outlineStep, Row example) {
        List<CellResult> matchedResults = new ArrayList<CellResult>();
        String name = outlineStep.getName();

        List<String> headerCells = examplesHeaderRow.getCells();
        for (int i = 0; i < headerCells.size(); i++) {
            String headerCell = headerCells.get(i);
            String value = example.getCells().get(i);
            String token = "<" + headerCell + ">";
            if(name.contains(token)) {
                name = name.replace(token, value);
                matchedResults.add(cellResults.get(i));
            }
        }

        Step step = new Step(outlineStep.getComments(), outlineStep.getKeyword(), name, outlineStep.getLine());
        matchedResultsByStep.put(step, matchedResults);
        return step;
    }

    public void step(Step step) {
        steps.add(step);
    }

    public void eof() {
        replayPreviousFeatureElement();
        reporter.eof();
    }

    public void syntaxError(String state, String event, List<String> legalEvents, String uri, int line) {
        throw new UnsupportedOperationException();
    }

    private void replayPreviousFeatureElement() {
        if (featureElement != null) {
            featureElementClassName = feature.getName() + "." + featureElement.getName();
            
            if (featureElement instanceof Scenario) {
                replayScenario();
                steps.clear();
            } else if (featureElement instanceof ScenarioOutline) {
                replayScenarioOutline();
            } else {
                throw new CucumberException("Unexpected featureElement: " + featureElement);
            }
            featureElement = null;
        }
    }

    private void replayScenario() {
        reporter.steps(steps);
        featureElement.replay(reporter);

        backend.newScenario();
        boolean skip = false; // TODO: Add ability to instantiate entire runner with skip=false, for dry runs
        for (Step step : steps) {
            skip = execute(step, skip);
        }
    }

    private void replayScenarioOutline() {
        reporter.steps(steps);
        featureElement.replay(reporter);

        for (Step step : steps) {
            reporter.step(step);
            reporter.match(step.getOutlineMatch(uri + ":" + step.getLine()));
            reporter.result(Result.SKIPPED);
        }
    }

    private boolean execute(Step step, boolean skip) {
        reporter.step(step);
        StackTraceElement stepStackTraceElement = createStackTraceElement(step);
        StepRunner stepRunner = stepRunner(step, stepStackTraceElement);
        return stepRunner.execute(skip, stepResultHandler, stepStackTraceElement, step);
    }

    private StackTraceElement createStackTraceElement(Step step) {
        return new StackTraceElement(featureElementClassName, step.getKeyword() + step.getName(), uri, step.getLine());
    }

    private StepRunner stepRunner(Step step, StackTraceElement stepStackTraceElement) {
        List<CucumberMatch> matches = stepMatches(step);
        if (matches.size() == 0) {
            return new UndefinedStepRunner(stepStackTraceElement);
        }
        if (matches.size() == 1) {
            return matches.get(0);
        } else {
            // TODO: Ambiguous for > 1
            throw new RuntimeException("TODO: Support ambiguous matches");
        }
    }

    private List<CucumberMatch> stepMatches(Step step) {
        List<CucumberMatch> result = new ArrayList<CucumberMatch>();
        for (StepDefinition stepDefinition : backend.getStepDefinitions()) {
            List<Argument> arguments = stepDefinition.matchedArguments(step);
            if (arguments != null) {
                result.add(new CucumberMatch(arguments, stepDefinition));
            }
        }
        return result;
    }

    public void scenarioStepMatch(Match match) {
        reporter.match(match);
    }

    public void scenarioStepResult(Result result) {
        reporter.result(result);
    }

    public void exampleResult(Step step, Result result) {
        List<CellResult> matchedResults = matchedResultsByStep.get(step);
        for (CellResult matchedResult : matchedResults) {
            matchedResult.addResult(result);
        }
        reporter.row(cellResults);
    }
}
