package io.cucumber.core.plugin;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.gherkin.DataTableArgument;
import io.cucumber.core.gherkin.DocStringArgument;
import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableFormatter;
import io.cucumber.docstring.DocString;
import io.cucumber.docstring.DocStringFormatter;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Exception;
import io.cucumber.messages.types.Group;
import io.cucumber.messages.types.JavaMethod;
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.PickleStep;
import io.cucumber.messages.types.PickleTableCell;
import io.cucumber.messages.types.PickleTag;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.SourceReference;
import io.cucumber.messages.types.Step;
import io.cucumber.messages.types.StepDefinition;
import io.cucumber.messages.types.StepMatchArgument;
import io.cucumber.messages.types.StepMatchArgumentsList;
import io.cucumber.messages.types.TestCaseStarted;
import io.cucumber.messages.types.TestStep;
import io.cucumber.messages.types.TestStepFinished;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EmbedEvent;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.StepArgument;
import io.cucumber.plugin.event.WriteEvent;
import io.cucumber.query.Lineage;
import io.cucumber.query.Query;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.cucumber.core.plugin.Formats.ansi;
import static io.cucumber.core.plugin.Formats.monochrome;
import static java.lang.Math.max;
import static java.util.Locale.ROOT;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * Prints a pretty report of the scenario execution as it happens.
 * <p>
 * When scenarios are executed concurrently the output will interleave. This is
 * to be expected.
 */
public final class PrettyFormatter implements ConcurrentEventListener, ColorAware {

    private static final String SCENARIO_INDENT = "";
    private static final String STEP_INDENT = SCENARIO_INDENT + "  ";
    private static final String STEP_SCENARIO_INDENT = STEP_INDENT + "  ";
    private static final String STACK_TRACE_INDENT = STEP_SCENARIO_INDENT + "  ";

    private final Map<String, Integer> commentStartIndexByTestCaseId = new HashMap<>();

    private final UTF8PrintWriter out;
    private Formats formats = ansi();
    private final Query query = new Query();
    private final Map<String, StepDefinition> stepDefinitionsById = new HashMap<>();

    public PrettyFormatter(OutputStream out) {
        this.out = new UTF8PrintWriter(out);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(Envelope.class, event -> {
            query.update(event);
            event.getStepDefinition().ifPresent(stepDefinition -> stepDefinitionsById.put(stepDefinition.getId(), stepDefinition));
            event.getTestCaseStarted().ifPresent(this::handleTestCaseStarted);
            event.getTestStepFinished().ifPresent(this::handleTestStepFinished);
            event.getTestRunFinished().ifPresent(this::handleTestRunFinished);
        });

        publisher.registerHandlerFor(WriteEvent.class, this::handleWrite);
        publisher.registerHandlerFor(EmbedEvent.class, this::handleEmbed);
    }

    private void handleTestCaseStarted(io.cucumber.messages.types.TestCaseStarted event) {
        out.println();
        preCalculateLocationIndent(event);
        printTags(event);
        printScenarioDefinition(event);
        out.flush();
    }

    private void handleTestStepFinished(io.cucumber.messages.types.TestStepFinished event) {
        printStep(event);
        printError(event);
        out.flush();
    }

    private void handleWrite(WriteEvent event) {
        out.println();
        printText(event);
        out.println();
        out.flush();
    }

    private void handleEmbed(EmbedEvent event) {
        out.println();
        printEmbedding(event);
        out.println();
        out.flush();
    }

    private void handleTestRunFinished(io.cucumber.messages.types.TestRunFinished event) {
        printError(event);
        out.close();
    }

    private void preCalculateLocationIndent(io.cucumber.messages.types.TestCaseStarted event) {
        query.findLineageBy(event)
                .flatMap(Lineage::scenario)
                .ifPresent(scenario -> {
                    query.findPickleBy(event).ifPresent(pickle -> {
                        int longestLine = calculateScenarioLineLength(pickle, scenario);

                        List<Step> steps = scenario.getSteps();
                        List<PickleStep> pickleSteps = pickle.getSteps();

                        int stepSize = pickleSteps.size();
                        for (int i = 0; i < stepSize; i++) {
                            Step step = steps.get(i);
                            PickleStep pickleStep = pickleSteps.get(i);
                            longestLine = Math.max(longestLine, calculateStepLineLength(step, pickleStep));
                        }

                        commentStartIndexByTestCaseId.put(event.getTestCaseId(), longestLine + 1);
                    });
                });
    }

    private static int calculateStepLineLength(Step step, PickleStep pickleStep) {
        String keyword = step.getKeyword();
        String text = pickleStep.getText();
        // The ": " add 2
        return STEP_INDENT.length() + keyword.length() + text.length() + 2;
    }

    private static int calculateScenarioLineLength(Pickle pickle, Scenario scenario) {
        String pickleName = pickle.getName();
        String pickleKeyword = scenario.getKeyword();
        // The ": " add 2
        return SCENARIO_INDENT.length() + pickleName.length() + pickleKeyword.length() + 2;
    }

    private void printTags(io.cucumber.messages.types.TestCaseStarted event) {
        query.findPickleBy(event)
                .map(Pickle::getTags)
                .filter(pickleTags -> !pickleTags.isEmpty())
                .map(pickleTags -> pickleTags.stream()
                        .map(PickleTag::getName)
                        .collect(joining(" ")))
                .ifPresent(tags -> out.println(SCENARIO_INDENT + tags));
    }

    private void printScenarioDefinition(TestCaseStarted event) {
        query.findLineageBy(event)
                .flatMap(Lineage::scenario)
                .ifPresent(scenario -> {
                    query.findPickleBy(event)
                            .ifPresent(pickle -> {
                                String definitionText = formatScenarioDefinition(scenario, pickle);
                                String path = relativize(pickle.getUri()).getSchemeSpecificPart();
                                String locationIndent = calculateLocationIndent(event.getTestCaseId(), definitionText);
                                String pathWithLine = query.findLocationOf(pickle).map(Location::getLine).map(line -> path + ":" + line).orElse(path);
                                out.println(definitionText + locationIndent + formatLocation(pathWithLine));
                            });
                });
    }

    private static String formatScenarioDefinition(Scenario scenario, Pickle pickle) {
        return SCENARIO_INDENT + scenario.getKeyword() + ": " + pickle.getName();
    }

    private void printStep(io.cucumber.messages.types.TestStepFinished event) {
        query.findTestStepBy(event)
                .ifPresent(testStep -> {
                    query.findPickleStepBy(testStep).ifPresent(pickleStep -> {
                        query.findStepBy(pickleStep).ifPresent(step -> {
                            printStep(event, testStep, pickleStep, step);
                            printArgument(pickleStep);
                        });
                    });
                });
    }

    private void printStep(TestStepFinished event, TestStep testStep, PickleStep pickleStep, Step step) {
        String keyword = step.getKeyword();
        String stepText = pickleStep.getText();
        // TODO: Use proper enum map.
        String status = event.getTestStepResult().getStatus().toString().toLowerCase(ROOT);
        List<StepMatchArgument> stepMatchArgumentsLists = testStep.getStepMatchArgumentsLists()
                .map(stepMatchArgumentsLists1 -> stepMatchArgumentsLists1.stream().map(StepMatchArgumentsList::getStepMatchArguments).flatMap(Collection::stream).collect(toList()))
                .orElseGet(Collections::emptyList);// TODO: Create separate _arg map

        String formattedStepText = STEP_INDENT + formatStepText(keyword, stepText, formats.get(status), formats.get(status + "_arg"), stepMatchArgumentsLists);
        String locationComment = formatLocationComment(event, testStep, keyword, stepText);
        out.println(formattedStepText + locationComment);
    }
    
    private void printArgument(PickleStep pickleStep) {
        pickleStep.getArgument().ifPresent(pickleStepArgument -> {
            
            pickleStepArgument.getDataTable().ifPresent(pickleTable -> {
                List<List<String>> cells = pickleTable.getRows().stream()
                        .map(pickleTableRow -> pickleTableRow.getCells().stream().map(PickleTableCell::getValue).collect(toList()))
                        .collect(toList());
                DataTableFormatter tableFormatter = DataTableFormatter.builder()
                        .prefixRow(STEP_SCENARIO_INDENT)
                        .escapeDelimiters(false)
                        .build();
                DataTable table = DataTable.create(cells);
                try {
                    tableFormatter.formatTo(table, out);
                } catch (IOException e) {
                    throw new CucumberException(e);
                }
            });
            pickleStepArgument.getDocString().ifPresent(pickleDocString -> {
                DocStringFormatter docStringFormatter = DocStringFormatter
                        .builder()
                        .indentation(STEP_SCENARIO_INDENT)
                        .build();
                DocString docString = DocString.create(pickleDocString.getContent(), pickleDocString.getMediaType().orElse(null));
                try {
                    docStringFormatter.formatTo(docString, out);
                } catch (IOException e) {
                    throw new CucumberException(e);
                }
            });
        });
    }

    private String formatLocationComment(
            TestStepFinished event, TestStep testStep, String keyword, String stepText
    ) {
        return testStep.getStepDefinitionIds()
                .filter(ids -> !ids.isEmpty())
                .map(ids -> {
                    String id = ids.get(0);
                    StepDefinition stepDefinition = this.stepDefinitionsById.get(id);
                    return stepDefinition.getSourceReference();
                })
                .flatMap(PrettyFormatter::formatSourceReference)
                .map(codeLocation -> query.findTestCaseBy(event).map(testCase -> {
                    String locationIndent = calculateLocationIndent(testCase.getId(), formatPlainStep(keyword, stepText));
                    return locationIndent + formatLocation(codeLocation);

                }).orElse("")).orElse("");

    }

    private static Optional<String> formatSourceReference(SourceReference sourceReference) {
        return formatJavaMethod(sourceReference)
                .map(Optional::of)
                .orElseGet(() -> formatStackTraceElement(sourceReference));
    }

    private static Optional<String> formatJavaMethod(SourceReference sourceReference) {
        return sourceReference.getJavaMethod()
                .map(PrettyFormatter::formatJavaMethod);
    }

    private static Optional<String> formatStackTraceElement(SourceReference sourceReference) {
        String location = sourceReference.getLocation().map(Location::getLine).map(line -> ":" + line).orElse("");
        return sourceReference.getJavaStackTraceElement()
                .map(javaStackTraceElement -> String.format("%s.%s(%s%s)",
                        javaStackTraceElement.getClassName(),
                        javaStackTraceElement.getMethodName(),
                        javaStackTraceElement.getFileName(),
                        location
                ));
    }

    private static String formatJavaMethod(JavaMethod javaMethod) {
        return javaMethod.getClassName() + "." + javaMethod.getMethodName() + "(" + javaMethod.getMethodParameterTypes().stream().collect(joining(",")) + ")";
    }

    private void printError(io.cucumber.messages.types.TestStepFinished event) {
        event.getTestStepResult()
                .getException()
                .ifPresent(exception -> {
                    String name = event.getTestStepResult().getStatus().name().toLowerCase(ROOT);
                    printError(STACK_TRACE_INDENT, exception, formats.get(name));
                });
    }

    private void printError(io.cucumber.messages.types.TestRunFinished event) {
        event.getException()
                .ifPresent(exception -> printError(SCENARIO_INDENT, exception, formats.get("failed")));
    }

    private void printError(String scenarioIndent, Exception exception, Format format) {
        String text = exception.getStackTrace().orElse("");
        // TODO: Java 12+ use String.indent
        String indented = text.replaceAll("(\r\n|\r|\n)", "$1" + scenarioIndent).trim();
        out.println(scenarioIndent + format.text(indented));
    }

    private void printText(WriteEvent event) {
        // Prevent interleaving when multiple threads write to System.out
        StringBuilder builder = new StringBuilder();
        try (BufferedReader lines = new BufferedReader(new StringReader(event.getText()))) {
            String line;
            while ((line = lines.readLine()) != null) {
                builder.append(STEP_SCENARIO_INDENT)
                        .append(line)
                        // Add system line separator - \n won't do it!
                        .append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new CucumberException(e);
        }
        out.append(builder);
    }

    private void printEmbedding(EmbedEvent event) {
        String line = "Embedding " + event.getName() + " [" + event.getMediaType() + " " + event.getData().length
                + " bytes]";
        out.println(STEP_SCENARIO_INDENT + line);
    }

    private String formatPlainStep(String keyword, String stepText) {
        return STEP_INDENT + keyword + stepText;
    }
//
//    private String formatScenarioDefinition(io.cucumber.messages.types.TestCase testCase) {
//        return testCase.getKeyword() + ": " + testCase.getName();
//    }

    static URI relativize(String uri) {
        return relativize(URI.create(uri));
    }

    static URI relativize(URI uri) {
        if (!"file".equals(uri.getScheme())) {
            return uri;
        }
        if (!uri.isAbsolute()) {
            return uri;
        }

        try {
            URI root = new File("").toURI();
            URI relative = root.relativize(uri);
            // Scheme is lost by relativize
            return new URI("file", relative.getSchemeSpecificPart(), relative.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private String calculateLocationIndent(String testCaseId, String prefix) {
        Integer commentStartAt = commentStartIndexByTestCaseId.getOrDefault(testCaseId, 0);
        int padding = commentStartAt - prefix.length();

        if (padding < 0) {
            return " ";
        }
        StringBuilder builder = new StringBuilder(padding);
        for (int i = 0; i < padding; i++) {
            builder.append(" ");
        }
        return builder.toString();
    }

    private String formatLocation(String location) {
        return formats.get("comment").text("# " + location);
    }

    String formatStepText(
            String keyword, String stepText, Format textFormat, Format argFormat, List<StepMatchArgument> arguments
    ) {
        int beginIndex = 0;
        StringBuilder result = new StringBuilder(textFormat.text(keyword));
        for (StepMatchArgument argument : arguments) {
            // can be null if the argument is missing.
            Group group = argument.getGroup();
            Optional<String> value = group.getValue();
            if (value.isPresent()) {
                // TODO: Messages are silly
                int argumentOffset = (int) (long) group.getStart().orElse(-1L);
                // a nested argument starts before the enclosing argument ends;
                // ignore it when formatting
                if (argumentOffset < beginIndex) {
                    continue;
                }
                String text = stepText.substring(beginIndex, argumentOffset);
                result.append(textFormat.text(text));
                int argumentEndIndex = argumentOffset + value.get().length();
                result.append(argFormat.text(stepText.substring(argumentOffset, argumentEndIndex)));
                beginIndex = argumentEndIndex;
            }
        }
        if (beginIndex != stepText.length()) {
            String text = stepText.substring(beginIndex);
            result.append(textFormat.text(text));
        }
        return result.toString();
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        formats = monochrome ? monochrome() : ansi();
    }

}
