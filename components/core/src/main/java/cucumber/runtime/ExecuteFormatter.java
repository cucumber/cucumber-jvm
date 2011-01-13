package cucumber.runtime;

import gherkin.formatter.Argument;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;

import java.util.ArrayList;
import java.util.List;

public class ExecuteFormatter implements Formatter {
    private List<Backend> backends;
    private final Reporter reporter;

    private String uri;
    private Feature feature;
    private DescribedStatement featureElement;
    private List<Step> undefinedSteps = new ArrayList<Step>();
    private List<Step> backgroundSteps = new ArrayList<Step>();
    private List<Step> steps = new ArrayList<Step>();
    private boolean inBackground;
    private String featureElementClassName;

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
        setFeatureElement(background);
        inBackground = true;
        reporter.background(background);
    }

    public void scenario(Scenario scenario) {
        executePrevious();
        setFeatureElement(scenario);
        inBackground = false;
        reporter.scenario(scenario);
    }

    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        executePrevious();
        setFeatureElement(scenarioOutline);
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

    // TODO: Move to prettyformatter...
    private void replayScenarioOutline() {
        if(featureElement != null) {
            featureElement.replay(reporter);
            for (Step step : steps) {
                reporter.step(step);
            }
            for (Step step : steps) {
                reporter.match(step.getOutlineMatch(uri + ":" + step.getLine()));
                reporter.result(Result.SKIPPED);
            }
            featureElement = null;
        }
    }

    public void examples(Examples examples) {
//        replayScenarioOutline();
        reporter.examples(examples);

        List<Row> rows = new ArrayList<Row>(examples.getRows());
        Row headerRow = rows.remove(0);
        for (Row example : rows) {
            executeExample(headerRow, example);
        }
    }

    private void executeExample(Row headerRow, Row example) {
        List<Step> exampleSteps = createExampleSteps(headerRow, example);
        boolean skip = false; // TODO: Add ability to instantiate entire runner with skip=false, for dry runs
        for (Step step : exampleSteps) {
            skip = execute(step, skip);
        }
    }

    private List<Step> createExampleSteps(Row headerRow, Row example) {
        List<Step> result = new ArrayList<Step>();
        for (Step outlineStep : steps) {
            result.add(outlineStep.createExampleStep(headerRow, example));
        }
        return result;
    }

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

    private boolean execute(Step step, boolean skip) {
        StackTraceElement stepStackTraceElement = createStackTraceElement(step);
        StepRunner stepRunner = stepRunner(step, stepStackTraceElement);
        return stepRunner.execute(skip, reporter, stepStackTraceElement);
    }

    private StackTraceElement createStackTraceElement(Step step) {
        return new StackTraceElement(featureElementClassName, step.getKeyword() + step.getName(), uri, step.getLine());
    }

    private void setFeatureElement(DescribedStatement featureElement) {
        featureElementClassName =  feature.getName() + "." + featureElement.getName();
        this.featureElement = featureElement;
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
