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
    private List<Backend> backends;
    private final Reporter reporter;
//    private final List<Step> scenarioSteps = new ArrayList<Step>();
//    private List<CellResult> cellResults;

    private String uri;
    private Feature feature;
    private DescribedStatement featureElement;
    private Map<Step, List<CellResult>> matchedResultsByStep = new HashMap<Step, List<CellResult>>();
//    private Row examplesHeaderRow;
    private List<Step> undefinedSteps = new ArrayList<Step>();
    private List<Step> backgroundSteps = new ArrayList<Step>();
    private List<Step> steps = new ArrayList<Step>();
    private boolean inBackground;

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
        inBackground = false;
        backgroundSteps.clear();
        reporter.feature(feature);
    }

    public void background(Background background) {
        this.featureElement = background;
        inBackground = true;
        reporter.background(background);
    }

    public void scenario(Scenario scenario) {
        executePrevious();
        this.featureElement = scenario;
        inBackground = false;
        reporter.scenario(scenario);
    }

    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        executePrevious();
        this.featureElement = scenarioOutline;
        inBackground = false;
        reporter.scenarioOutline(scenarioOutline);
    }

    private void executePrevious() {
        for (Backend backend : backends) {
            backend.newScenario();
        }
        boolean skip = false;
        for (Step step : backgroundSteps) {
            skip = execute(step, skip);
        }
        for (Step step : steps) {
            skip = execute(step, skip);
        }
        steps.clear();
    }

    public void examples(Examples examples) {
        reporter.examples(examples);
//        replayPreviousFeatureElement();
//        examples.replay(reporter);
//        reporter.table(examples.getRows());
//
//        List<Row> rows = new ArrayList<Row>(examples.getRows());
//        examplesHeaderRow = rows.remove(0);
//        reporter.row(examplesHeaderRow.createResults("skipped_arg"));
//        reporter.nextRow();
//
//        for (Row example : rows) {
//            cellResults = example.createResults("executing");
//            executeExample(example);
//        }
    }

//    private void executeExample(Row example) {
//        List<Step> Steps = createExampleSteps(example);
//        boolean skip = false; // TODO: Add ability to instantiate entire runner with skip=false, for dry runs
//        for (Step step : Steps) {
//            skip = execute(step, skip);
//        }
//        reporter.nextRow();
//    }

//    private List<Step> createExampleSteps(Row example) {
//        List<Step> result = new ArrayList<Step>();
//        for (Step outlineStep : scenarioSteps) {
//            result.add(exampleStep(outlineStep, example));
//        }
//        return result;
//    }

//    private Step exampleStep(Step outlineStep, Row example) {
//        List<CellResult> matchedResults = new ArrayList<CellResult>();
//        String name = outlineStep.getName();
//
//        List<String> headerCells = examplesHeaderRow.getCells();
//        for (int i = 0; i < headerCells.size(); i++) {
//            String headerCell = headerCells.get(i);
//            String value = example.getCells().get(i);
//            String token = "<" + headerCell + ">";
//            if (name.contains(token)) {
//                name = name.replace(token, value);
//                matchedResults.add(cellResults.get(i));
//            }
//        }
//
//        Step step = new Step(outlineStep.getComments(), outlineStep.getKeyword(), name, outlineStep.getLine());
//        matchedResultsByStep.put(step, matchedResults);
//        return step;
//    }

    public void step(Step step) {
        if(inBackground) {
            backgroundSteps.add(step);
        } else {
            steps.add(step);
        }
        reporter.step(step);
    }

    public void eof() {
        executePrevious();

//        replayPreviousFeatureElement();
        reporter.eof();
    }

    public void syntaxError(String state, String event, List<String> legalEvents, String uri, int line) {
        throw new UnsupportedOperationException();
    }

//    private void replayPreviousFeatureElement() {
//        if (featureElement != null) {
//            featureElementClassName = feature.getName() + "." + featureElement.getName();
//
//            if (featureElement instanceof Scenario) {
//                replayScenario();
//                scenarioSteps.clear();
//            } else if (featureElement instanceof ScenarioOutline) {
//                replayScenarioOutline();
//            } else {
//                throw new CucumberException("Unexpected featureElement: " + featureElement);
//            }
//            featureElement = null;
//        }
//    }

//    private void replayScenario() {
//        if (hasPreviousScenario) {
//            for (Backend backend : backends) {
//                backend.disposeScenario();
//            }
//        }
//        hasPreviousScenario = true;
//
//        for (Backend backend : backends) {
//            backend.newScenario();
//        }
//
//        boolean skip = false; // TODO: Add ability to instantiate entire runner with skip=false, for dry runs
//
//        if (background != null) {
//            for (Step step : backgroundSteps) {
//                reporter.step(step);
//            }
//            background.replay(reporter);
//            background = null;
//            stepResultHandler = new ReportingStepResultHandler(reporter);
//        } else {
//            stepResultHandler = new OnlyOnFailureReportingStepResultHandler(reporter);
//        }
//        for (Step step : backgroundSteps) {
//            skip = execute(step, skip);
//        }
//
//        featureElement.replay(reporter);
//        stepResultHandler = new ReportingStepResultHandler(reporter);
//        for (Step step : scenarioSteps) {
//            skip = execute(step, skip);
//        }
//    }

//    private void replayScenarioOutline() {
//        featureElement.replay(reporter);
//
//        for (Step step : scenarioSteps) {
//            reporter.step(step);
//            reporter.match(step.getOutlineMatch(uri + ":" + step.getLine()));
//            reporter.result(Result.SKIPPED);
//        }
//    }

    private boolean execute(Step step, boolean skip) {
        StackTraceElement stepStackTraceElement = createStackTraceElement(step);
        StepRunner stepRunner = stepRunner(step, stepStackTraceElement);
        return stepRunner.execute(skip, reporter, stepStackTraceElement);
    }

    private StackTraceElement createStackTraceElement(Step step) {
        return new StackTraceElement(featureElementClassName(), step.getKeyword() + step.getName(), uri, step.getLine());
    }

    private String featureElementClassName() {
        return feature.getName() + "." + featureElement.getName();
    }

    private StepRunner stepRunner(Step step, StackTraceElement stepStackTraceElement) {
        List<CucumberMatch> matches = stepMatches(step);
        if (matches.size() == 0) {
            undefinedSteps.add(step);
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
        for (Backend backend : backends) {
            for (StepDefinition stepDefinition : backend.getStepDefinitions()) {
                List<Argument> arguments = stepDefinition.matchedArguments(step);
                if (arguments != null) {
                    result.add(new CucumberMatch(arguments, stepDefinition));
                }
            }
        }
        return result;
    }

//    public void exampleResult(Step step, Result result) {
//        List<CellResult> matchedResults = matchedResultsByStep.get(step);
//        for (CellResult matchedResult : matchedResults) {
//            matchedResult.addResult(result);
//        }
//        reporter.row(cellResults);
//    }
//
    private List<String> getSnippets() {
        List<String> snippets = new ArrayList<String>();
        for (Step step : undefinedSteps) {
            for (Backend backend : backends) {
                String snippet = backend.getSnippet(step);
                snippets.add(snippet);
            }
        }
        return snippets;
    }

    public void reportSummary(SummaryReporter summaryReporter) {
        summaryReporter.snippets(getSnippets());
    }
}
