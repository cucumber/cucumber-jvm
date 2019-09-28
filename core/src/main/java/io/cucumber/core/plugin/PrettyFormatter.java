package io.cucumber.core.plugin;

import gherkin.ast.Background;
import gherkin.ast.Examples;
import gherkin.ast.Feature;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.ScenarioOutline;
import gherkin.ast.Step;
import gherkin.ast.Tag;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.Argument;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestSourceRead;
import io.cucumber.plugin.event.TestStep;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import io.cucumber.plugin.event.WriteEvent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.List;

import static java.util.Locale.ROOT;
import static java.util.stream.Collectors.joining;

public final class PrettyFormatter implements EventListener, ColorAware {
    private static final String SCENARIO_INDENT = "  ";
    private static final String STEP_INDENT = "    ";
    private static final String EXAMPLES_INDENT = "    ";
    private final TestSourcesModel testSources = new TestSourcesModel();
    private final NiceAppendable out;
    private Formats formats;
    private URI currentFeatureFile;
    private TestCase currentTestCase;
    private ScenarioOutline currentScenarioOutline;
    private Examples currentExamples;
    private int locationIndentation;

    @SuppressWarnings("WeakerAccess") // Used by PluginFactory
    public PrettyFormatter(Appendable out) {
        this.out = new NiceAppendable(out);
        this.formats = new AnsiFormats();
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, this::handleTestSourceRead);
        publisher.registerHandlerFor(TestCaseStarted.class, this::handleTestCaseStarted);
        publisher.registerHandlerFor(TestStepStarted.class, this::handleTestStepStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::handleTestStepFinished);
        publisher.registerHandlerFor(WriteEvent.class, this::handleWrite);
        publisher.registerHandlerFor(TestRunFinished.class, event -> finishReport());
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        if (monochrome) {
            formats = new MonochromeFormats();
        } else {
            formats = new AnsiFormats();
        }
    }

    private void handleTestSourceRead(TestSourceRead event) {
        testSources.addTestSourceReadEvent(event.getUri(), event);
    }

    private void handleTestCaseStarted(TestCaseStarted event) {
        handleStartOfFeature(event);
        handleScenarioOutline(event);
        if (testSources.hasBackground(currentFeatureFile, event.getTestCase().getLine())) {
            printBackground(event.getTestCase());
            currentTestCase = event.getTestCase();
        } else {
            printScenarioDefinition(event.getTestCase());
        }
    }

    private void handleTestStepStarted(TestStepStarted event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            if (isFirstStepAfterBackground((PickleStepTestStep) event.getTestStep())) {
                printScenarioDefinition(currentTestCase);
                currentTestCase = null;
            }
        }
    }

    private void handleTestStepFinished(TestStepFinished event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            printStep((PickleStepTestStep) event.getTestStep(), event.getResult());
        }
        printError(event.getResult());
    }

    private void handleWrite(WriteEvent event) {
        out.println(event.getText());
    }

    private void finishReport() {
        out.close();
    }

    private void handleStartOfFeature(TestCaseStarted event) {
        if (currentFeatureFile == null || !currentFeatureFile.equals(event.getTestCase().getUri())) {
            if (currentFeatureFile != null) {
                out.println();
            }
            currentFeatureFile = event.getTestCase().getUri();
            printFeature(currentFeatureFile);
        }
    }

    private void handleScenarioOutline(TestCaseStarted event) {
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, event.getTestCase().getLine());
        if (TestSourcesModel.isScenarioOutlineScenario(astNode)) {
            ScenarioOutline scenarioOutline = (ScenarioOutline)TestSourcesModel.getScenarioDefinition(astNode);
            if (currentScenarioOutline == null || !currentScenarioOutline.equals(scenarioOutline)) {
                currentScenarioOutline = scenarioOutline;
                printScenarioOutline(currentScenarioOutline);
            }
            if (currentExamples == null || !currentExamples.equals(astNode.parent.node)) {
                currentExamples = (Examples)astNode.parent.node;
                printExamples(currentExamples);
            }
        } else {
            currentScenarioOutline = null;
            currentExamples = null;
        }
    }

    private void printScenarioOutline(ScenarioOutline scenarioOutline) {
        out.println();
        printTags(scenarioOutline.getTags(), SCENARIO_INDENT);
        out.println(SCENARIO_INDENT + getScenarioDefinitionText(scenarioOutline) + " " + getLocationText(currentFeatureFile, scenarioOutline.getLocation().getLine()));
        printDescription(scenarioOutline.getDescription());
        for (Step step : scenarioOutline.getSteps()) {
            out.println(STEP_INDENT + formats.get("skipped").text(step.getKeyword() + step.getText()));
        }
    }

    private void printExamples(Examples examples) {
        out.println();
        printTags(examples.getTags(), EXAMPLES_INDENT);
        out.println(EXAMPLES_INDENT + examples.getKeyword() + ": " + examples.getName());
        printDescription(examples.getDescription());
    }

    private void printStep(PickleStepTestStep testStep, Result result) {
        String keyword = getStepKeyword(testStep);
        String stepText = testStep.getStepText();
        String locationPadding = createPaddingToLocation(STEP_INDENT, keyword + stepText);
        String status = result.getStatus().name().toLowerCase(ROOT);
        String formattedStepText = formatStepText(keyword, stepText, formats.get(status), formats.get(status + "_arg"), testStep.getDefinitionArgument());
        out.println(STEP_INDENT + formattedStepText + locationPadding + getLocationText(testStep.getCodeLocation()));
    }

    String formatStepText(String keyword, String stepText, Format textFormat, Format argFormat, List<Argument> arguments) {
        int beginIndex = 0;
        StringBuilder result = new StringBuilder(textFormat.text(keyword));
        for (Argument argument : arguments) {
            // can be null if the argument is missing.
            if (argument.getValue() != null) {
                int argumentOffset = argument.getStart();
                // a nested argument starts before the enclosing argument ends; ignore it when formatting
                if (argumentOffset < beginIndex) {
                    continue;
                }
                String text = stepText.substring(beginIndex, argumentOffset);
                result.append(textFormat.text(text));
            }
            // val can be null if the argument isn't there, for example @And("(it )?has something")
            if (argument.getValue() != null) {
                String text = stepText.substring(argument.getStart(), argument.getEnd());
                result.append(argFormat.text(text));
                // set beginIndex to end of argument
                beginIndex = argument.getEnd();
            }
        }
        if (beginIndex != stepText.length()) {
            String text = stepText.substring(beginIndex);
            result.append(textFormat.text(text));
        }
        return result.toString();
    }

    private String getScenarioDefinitionText(ScenarioDefinition definition) {
        return definition.getKeyword() + ": " + definition.getName();
    }

    private String getLocationText(URI file, int line) {
        String path = file.getSchemeSpecificPart();
        return getLocationText(path + ":" + line);
    }

    private String getLocationText(String location) {
        return formats.get("comment").text("# " + location);
    }

    private StringBuffer stepText(PickleStepTestStep testStep) {
        String keyword = getStepKeyword(testStep);
        return new StringBuffer(keyword + testStep.getStepText());
    }

    private String getStepKeyword(PickleStepTestStep testStep) {
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testStep.getStepLine());
        if (astNode != null) {
            Step step = (Step) astNode.node;
            return step.getKeyword();
        } else {
            return "";
        }
    }

    private boolean isFirstStepAfterBackground(PickleStepTestStep testStep) {
        return currentTestCase != null && !isBackgroundStep(testStep);
    }

    private boolean isBackgroundStep(PickleStepTestStep testStep) {
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testStep.getStepLine());
        if (astNode != null) {
            return TestSourcesModel.isBackgroundStep(astNode);
        }
        return false;
    }

    private void printFeature(URI path) {
        Feature feature = testSources.getFeature(path);
        printTags(feature.getTags());
        out.println(feature.getKeyword() + ": " + feature.getName());
        printDescription(feature.getDescription());
    }

    private void printTags(List<Tag> tags) {
        printTags(tags, "");
    }
    private void printTags(List<Tag> tags, String indent) {
        if (!tags.isEmpty()) {
            out.println(indent + tags.stream().map(Tag::getName).collect(joining(" ")));
        }
    }

    private void printPickleTags(List<String> tags, String indent) {
        if (!tags.isEmpty()) {
            out.println(indent + String.join(" ", tags));
        }
    }

    private void printDescription(String description) {
        if (description != null) {
            out.println(description);
        }
    }

    private void printBackground(TestCase testCase) {
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testCase.getLine());
        if (astNode != null) {
            Background background = TestSourcesModel.getBackgroundForTestCase(astNode);
            String backgroundText = getScenarioDefinitionText(background);
            boolean useBackgroundSteps = true;
            calculateLocationIndentation(SCENARIO_INDENT + backgroundText, testCase.getTestSteps(), useBackgroundSteps);
            String locationPadding = createPaddingToLocation(SCENARIO_INDENT, backgroundText);
            out.println();
            out.println(SCENARIO_INDENT + backgroundText + locationPadding + getLocationText(currentFeatureFile, background.getLocation().getLine()));
            printDescription(background.getDescription());
        }
    }

    private void printScenarioDefinition(TestCase testCase) {
        ScenarioDefinition scenarioDefinition = testSources.getScenarioDefinition(currentFeatureFile, testCase.getLine());
        String definitionText = scenarioDefinition.getKeyword() + ": " + testCase.getName();
        calculateLocationIndentation(SCENARIO_INDENT + definitionText, testCase.getTestSteps());
        String locationPadding = createPaddingToLocation(SCENARIO_INDENT, definitionText);
        out.println();
        printPickleTags(testCase.getTags(), SCENARIO_INDENT);
        out.println(SCENARIO_INDENT + definitionText + locationPadding + getLocationText(currentFeatureFile, testCase.getLine()));
        printDescription(scenarioDefinition.getDescription());
    }

    private void printError(Result result) {
        if (result.getError() != null) {
            String name = result.getStatus().name().toLowerCase(ROOT);
            out.println("      " + formats.get(name).text(printStackTrace(result.getError())));
        }
    }

    private void calculateLocationIndentation(String definitionText, List<TestStep> testSteps) {
        boolean useBackgroundSteps = false;
        calculateLocationIndentation(definitionText, testSteps, useBackgroundSteps);
    }

    private void calculateLocationIndentation(String definitionText, List<TestStep> testSteps, boolean useBackgroundSteps) {
        int maxTextLength = definitionText.length();
        for (TestStep step : testSteps) {
            if (step instanceof PickleStepTestStep) {
                PickleStepTestStep testStep = (PickleStepTestStep) step;
                if (isBackgroundStep(testStep) == useBackgroundSteps) {
                    StringBuffer stepText = stepText(testStep);
                    maxTextLength = Math.max(maxTextLength, STEP_INDENT.length() + stepText.length());
                }
            }
        }
        locationIndentation = maxTextLength + 1;
    }

    private String createPaddingToLocation(String indent, String text) {
        StringBuilder padding = new StringBuilder();
        for (int i = indent.length() + text.length(); i < locationIndentation; ++i) {
            padding.append(' ');
        }
        return padding.toString();
    }

    private static String printStackTrace(Throwable error) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        error.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
