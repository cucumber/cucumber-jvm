package cucumber.runtime.formatter;

import cucumber.api.Argument;
import cucumber.api.Result;
import cucumber.api.TestStep;
import cucumber.api.TestCase;
import cucumber.api.PickleStepTestStep;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestCaseStarted;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestSourceRead;
import cucumber.api.event.TestStepFinished;
import cucumber.api.event.TestStepStarted;
import cucumber.api.event.WriteEvent;
import cucumber.api.formatter.ColorAware;
import cucumber.api.formatter.Formatter;
import cucumber.api.formatter.NiceAppendable;
import cucumber.api.formatter.NiceRetrievableAppendable;
import cucumber.util.FixJava;
import cucumber.util.Mapper;
import gherkin.ast.Background;
import gherkin.ast.Examples;
import gherkin.ast.Feature;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.ScenarioOutline;
import gherkin.ast.Step;
import gherkin.ast.Tag;
import gherkin.pickles.PickleTag;

import java.util.List;

final class PrettyFormatter implements Formatter, ColorAware {
    private static final String SCENARIO_INDENT = "  ";
    private static final String STEP_INDENT = "    ";
    private static final String EXAMPLES_INDENT = "    ";
    private final TestSourcesModel testSources = new TestSourcesModel();
    private final NiceAppendable globalOut;
    private final Object globalOutSyncObject = new Object();
    private Formats formats;

    private final ThreadLocal<NiceRetrievableAppendable> out = new ThreadLocal<NiceRetrievableAppendable>();
    private final ThreadLocal<CurrentFeature> featureUnderTest = new ThreadLocal<CurrentFeature>();

    private final Mapper<Tag, String> tagNameMapper = new Mapper<Tag, String>() {
        @Override
        public String map(Tag tag) {
            return tag.getName();
        }
    };
    private final Mapper<PickleTag, String> pickleTagNameMapper = new Mapper<PickleTag, String>() {
        @Override
        public String map(PickleTag pickleTag) {
            return pickleTag.getName();
        }
    };

    private final EventHandler<TestSourceRead> testSourceReadHandler = new EventHandler<TestSourceRead>() {
        @Override
        public void receive(TestSourceRead event) {
            handleTestSourceRead(event);
        }
    };
    private final EventHandler<TestCaseStarted> caseStartedHandler= new EventHandler<TestCaseStarted>() {
        @Override
        public void receive(TestCaseStarted event) {
            handleTestCaseStarted(event);
        }
    };
    private final EventHandler<TestCaseFinished> caseFinishedHandler= new EventHandler<TestCaseFinished>() {
        @Override
        public void receive(TestCaseFinished event) {
            handleTestCaseFinished(event);
        }
    };
    private final EventHandler<TestStepStarted> stepStartedHandler = new EventHandler<TestStepStarted>() {
        @Override
        public void receive(TestStepStarted event) {
            handleTestStepStarted(event);
        }
    };
    private final EventHandler<TestStepFinished> stepFinishedHandler = new EventHandler<TestStepFinished>() {
        @Override
        public void receive(TestStepFinished event) {
            handleTestStepFinished(event);
        }
    };
    private final EventHandler<WriteEvent> writeEventHandler = new EventHandler<WriteEvent>() {
        @Override
        public void receive(WriteEvent event) {
            handleWrite(event);
        }
    };
    private final EventHandler<TestRunFinished> runFinishedHandler = new EventHandler<TestRunFinished>() {
        @Override
        public void receive(TestRunFinished event) {
            finishReport();
        }
    };

    @SuppressWarnings("WeakerAccess") // Used by PluginFactory
    public PrettyFormatter(Appendable out) {
        this.globalOut = new NiceAppendable(out);
        this.formats = new AnsiFormats();
        this.out.set(new NiceRetrievableAppendable(new StringBuilder()));
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, testSourceReadHandler);
        publisher.registerHandlerFor(TestCaseStarted.class, caseStartedHandler);
        publisher.registerHandlerFor(TestCaseFinished.class, caseFinishedHandler);
        publisher.registerHandlerFor(TestStepStarted.class, stepStartedHandler);
        publisher.registerHandlerFor(TestStepFinished.class, stepFinishedHandler);
        publisher.registerHandlerFor(WriteEvent.class, writeEventHandler);
        publisher.registerHandlerFor(TestRunFinished.class, runFinishedHandler);
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
        testSources.addTestSourceReadEvent(event.uri, event);
    }

    private void handleTestCaseStarted(TestCaseStarted event) {
        out.set(new NiceRetrievableAppendable(new StringBuilder()));
        handleStartOfFeature(event);
        handleScenarioOutline(event);
        CurrentFeature currentFeature = featureUnderTest.get();
        if (testSources.hasBackground(currentFeature.uri, event.testCase.getLine())) {
            printBackground(event.testCase);
            currentFeature.testCase = event.testCase;
        } else {
            printScenarioDefinition(event.testCase);
        }
    }

    private void handleTestCaseFinished(TestCaseFinished event) {
        synchronized (globalOutSyncObject) {
            globalOut.append(out.get().printAll());
        }
        out.get().close();
    }

    private void handleTestStepStarted(TestStepStarted event) {
        if (event.testStep instanceof PickleStepTestStep) {
            if (isFirstStepAfterBackground((PickleStepTestStep) event.testStep)) {
                CurrentFeature currentFeature = featureUnderTest.get();
                printScenarioDefinition(currentFeature.testCase);
                currentFeature.testCase = null;
            }
        }
    }

    private void handleTestStepFinished(TestStepFinished event) {
        if (event.testStep instanceof PickleStepTestStep) {
            printStep((PickleStepTestStep) event.testStep, event.result);
        }
        printError(event.result);
    }

    private void handleWrite(WriteEvent event) {
        out.get().println(event.text);
    }

    private void finishReport() {
        globalOut.close();
    }

    private void handleStartOfFeature(TestCaseStarted event) {
        CurrentFeature currentFeature = featureUnderTest.get();
        if (currentFeature == null || currentFeature.uri == null || !currentFeature.uri.equals(event.testCase.getUri())) {
            out.get().println();
            currentFeature = new CurrentFeature(event.testCase.getUri());
            featureUnderTest.set(currentFeature);
            printFeature(currentFeature.uri);
        }
    }

    private void handleScenarioOutline(TestCaseStarted event) {
        CurrentFeature currentFeature = featureUnderTest.get();
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeature.uri, event.testCase.getLine());
        if (TestSourcesModel.isScenarioOutlineScenario(astNode)) {
            ScenarioOutline scenarioOutline = (ScenarioOutline)TestSourcesModel.getScenarioDefinition(astNode);
            if (currentFeature.scenarioOutline == null || !currentFeature.scenarioOutline.equals(scenarioOutline)) {
                currentFeature.scenarioOutline = scenarioOutline;
                printScenarioOutline(currentFeature.scenarioOutline);
            }
            if (currentFeature.examples == null || !currentFeature.examples.equals(astNode.parent.node)) {
                currentFeature.examples = (Examples)astNode.parent.node;
                printExamples(currentFeature.examples);
            }
        } else {
            currentFeature.scenarioOutline = null;
            currentFeature.examples = null;
        }
    }

    private void printScenarioOutline(ScenarioOutline scenarioOutline) {
        CurrentFeature currentFeature = featureUnderTest.get();
        out.get().println();
        printTags(scenarioOutline.getTags(), SCENARIO_INDENT);
        out.get().println(SCENARIO_INDENT + getScenarioDefinitionText(scenarioOutline) + " " + getLocationText(currentFeature.uri, scenarioOutline.getLocation().getLine()));
        printDescription(scenarioOutline.getDescription());
        for (Step step : scenarioOutline.getSteps()) {
            out.get().println(STEP_INDENT + formats.get("skipped").text(step.getKeyword() + step.getText()));
        }
    }

    private void printExamples(Examples examples) {
        out.get().println();
        printTags(examples.getTags(), EXAMPLES_INDENT);
        out.get().println(EXAMPLES_INDENT + examples.getKeyword() + ": " + examples.getName());
        printDescription(examples.getDescription());
    }

    private void printStep(PickleStepTestStep testStep, Result result) {
        String keyword = getStepKeyword(testStep);
        String stepText = testStep.getStepText();
        String locationPadding = createPaddingToLocation(STEP_INDENT, keyword + stepText);
        String formattedStepText = formatStepText(keyword, stepText, formats.get(result.getStatus().lowerCaseName()), formats.get(result.getStatus().lowerCaseName() + "_arg"), testStep.getDefinitionArgument());
        out.get().println(STEP_INDENT + formattedStepText + locationPadding + getLocationText(testStep.getCodeLocation()));
    }

    String formatStepText(String keyword, String stepText, Format textFormat, Format argFormat, List<Argument> arguments) {
        int beginIndex = 0;
        StringBuilder result = new StringBuilder(textFormat.text(keyword));
        for (Argument argument : arguments) {
            // can be null if the argument is missing.
            if (argument.getOffset() != null) {
                int argumentOffset = argument.getOffset();
                // a nested argument starts before the enclosing argument ends; ignore it when formatting
                if (argumentOffset < beginIndex ) {
                    continue;
                }
                String text = stepText.substring(beginIndex, argumentOffset);
                result.append(textFormat.text(text));
            }
            // val can be null if the argument isn't there, for example @And("(it )?has something")
            if (argument.getVal() != null) {
                result.append(argFormat.text(argument.getVal()));
                // set beginIndex to end of argument
                beginIndex = argument.getOffset() + argument.getVal().length();
            }
        }
        if (beginIndex != stepText.length()) {
            String text = stepText.substring(beginIndex, stepText.length());
            result.append(textFormat.text(text));
        }
        return result.toString();
    }

    private String getScenarioDefinitionText(ScenarioDefinition definition) {
        return definition.getKeyword() + ": " + definition.getName();
    }

    private String getLocationText(String file, int line) {
        return getLocationText(file + ":" + line);
    }

    private String getLocationText(String location) {
        return formats.get("comment").text("# " + location);
    }

    private StringBuffer stepText(PickleStepTestStep testStep) {
        String keyword = getStepKeyword(testStep);
        return new StringBuffer(keyword + testStep.getStepText());
    }

    private String getStepKeyword(PickleStepTestStep testStep) {
        CurrentFeature currentFeature = featureUnderTest.get();
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeature.uri, testStep.getStepLine());
        if (astNode != null) {
            Step step = (Step) astNode.node;
            return step.getKeyword();
        } else {
            return "";
        }
    }

    private boolean isFirstStepAfterBackground(PickleStepTestStep testStep) {
        CurrentFeature currentFeature = featureUnderTest.get();
        return currentFeature.testCase != null && !isBackgroundStep(testStep);
    }

    private boolean isBackgroundStep(PickleStepTestStep testStep) {
        CurrentFeature currentFeature = featureUnderTest.get();
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeature.uri, testStep.getStepLine());
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testStep.getStepLine());
        if (astNode != null) {
            return TestSourcesModel.isBackgroundStep(astNode);
        }
        return false;
    }

    private void printFeature(String path) {
        Feature feature = testSources.getFeature(path);
        printTags(feature.getTags());
        out.get().println(feature.getKeyword() + ": " + feature.getName());
        printDescription(feature.getDescription());
    }

    private void printTags(List<Tag> tags) {
        printTags(tags, "");
    }
    private void printTags(List<Tag> tags, String indent) {
        if (!tags.isEmpty()) {
            out.get().println(indent + FixJava.join(FixJava.map(tags, tagNameMapper), " "));
        }
    }

    private void printPickleTags(List<PickleTag> tags, String indent) {
        if (!tags.isEmpty()) {
            out.get().println(indent + FixJava.join(FixJava.map(tags, pickleTagNameMapper), " "));
        }
    }

    private void printDescription(String description) {
        if (description != null) {
            out.get().println(description);
        }
    }

    private void printBackground(TestCase testCase) {
        CurrentFeature currentFeature = featureUnderTest.get();
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeature.uri, testCase.getLine());
        if (astNode != null) {
            Background background = TestSourcesModel.getBackgroundForTestCase(astNode);
            String backgroundText = getScenarioDefinitionText(background);
            boolean useBackgroundSteps = true;
            calculateLocationIndentation(SCENARIO_INDENT + backgroundText, testCase.getTestSteps(), useBackgroundSteps);
            String locationPadding = createPaddingToLocation(SCENARIO_INDENT, backgroundText);
            out.get().println();
            out.get().println(SCENARIO_INDENT + backgroundText + locationPadding + getLocationText(currentFeature.uri, background.getLocation().getLine()));
            printDescription(background.getDescription());
        }
    }

    private void printScenarioDefinition(TestCase testCase) {
        CurrentFeature currentFeature = featureUnderTest.get();
        ScenarioDefinition scenarioDefinition = testSources.getScenarioDefinition(currentFeature.uri, testCase.getLine());
        String definitionText = scenarioDefinition.getKeyword() + ": " + testCase.getName();
        calculateLocationIndentation(SCENARIO_INDENT + definitionText, testCase.getTestSteps());
        String locationPadding = createPaddingToLocation(SCENARIO_INDENT, definitionText);
        out.get().println();
        printPickleTags(testCase.getTags(), SCENARIO_INDENT);
        out.get().println(SCENARIO_INDENT + definitionText + locationPadding + getLocationText(currentFeature.uri, testCase.getLine()));
        printDescription(scenarioDefinition.getDescription());
    }

    private void printError(Result result) {
        if (result.getError() != null) {
            out.get().println("      " + formats.get(result.getStatus().lowerCaseName()).text(result.getErrorMessage()));
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
        CurrentFeature currentFeature = featureUnderTest.get();
        currentFeature.locationIndentation = maxTextLength + 1;
    }

    private String createPaddingToLocation(String indent, String text) {
        CurrentFeature currentFeature = featureUnderTest.get();
        StringBuffer padding = new StringBuffer();
        for (int i = indent.length() + text.length(); i < currentFeature.locationIndentation; ++i) {
            padding.append(' ');
        }
        return padding.toString();
    }

    private class CurrentFeature {
        private final String uri;
        private TestCase testCase;
        private ScenarioOutline scenarioOutline;
        private Examples examples;
        private int locationIndentation;

        CurrentFeature(final String uri) {
            this.uri = uri;
        }
    }
    
}
