package io.cucumber.core.plugin;

import io.cucumber.core.plugin.CucumberJvmJson.JvmArgument;
import io.cucumber.core.plugin.CucumberJvmJson.JvmDataTableRow;
import io.cucumber.core.plugin.CucumberJvmJson.JvmDocString;
import io.cucumber.core.plugin.CucumberJvmJson.JvmElement;
import io.cucumber.core.plugin.CucumberJvmJson.JvmElementType;
import io.cucumber.core.plugin.CucumberJvmJson.JvmFeature;
import io.cucumber.core.plugin.CucumberJvmJson.JvmLocation;
import io.cucumber.core.plugin.CucumberJvmJson.JvmLocationTag;
import io.cucumber.core.plugin.CucumberJvmJson.JvmMatch;
import io.cucumber.core.plugin.CucumberJvmJson.JvmResult;
import io.cucumber.core.plugin.CucumberJvmJson.JvmStatus;
import io.cucumber.core.plugin.CucumberJvmJson.JvmStep;
import io.cucumber.core.plugin.CucumberJvmJson.JvmTag;
import io.cucumber.messages.Convertor;
import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.DataTable;
import io.cucumber.messages.types.DocString;
import io.cucumber.messages.types.Exception;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Group;
import io.cucumber.messages.types.JavaMethod;
import io.cucumber.messages.types.JavaStackTraceElement;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.PickleStep;
import io.cucumber.messages.types.Rule;
import io.cucumber.messages.types.RuleChild;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.SourceReference;
import io.cucumber.messages.types.Step;
import io.cucumber.messages.types.StepDefinition;
import io.cucumber.messages.types.StepMatchArgumentsList;
import io.cucumber.messages.types.TableCell;
import io.cucumber.messages.types.Tag;
import io.cucumber.messages.types.TestCaseStarted;
import io.cucumber.messages.types.TestStep;
import io.cucumber.messages.types.TestStepFinished;
import io.cucumber.messages.types.TestStepResult;
import io.cucumber.messages.types.TestStepResultStatus;
import io.cucumber.messages.types.Timestamp;
import io.cucumber.query.NamingStrategy;
import io.cucumber.query.Query;

import java.net.URI;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static io.cucumber.core.plugin.TestSourcesModel.convertToId;
import static java.util.Locale.ROOT;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

class JsonReportWriter {
    private final Query query;

    JsonReportWriter(Query query) {
        this.query = query;
    }

    List<Object> writeJsonReport() {
        return query.findAllTestCaseStartedGroupedByFeature()
                .entrySet()
                .stream()
                .map(this::createFeatureMap)
                .collect(toList());
    }

    private JvmFeature createFeatureMap(Entry<Optional<Feature>, List<TestCaseStarted>> entry) {
        GherkinDocument document = getGherkinDocument(entry);
        Feature feature = entry.getKey().get();
        return new JvmFeature(
                TestSourcesModel.relativize(URI.create(document.getUri().get())).toString(), // TODO: Relativize, optional?, null?
                convertToId(feature.getName()),
                feature.getLocation().getLine(),
                feature.getKeyword(),
                feature.getName(),
                feature.getDescription() != null ? feature.getDescription() : "", // TODO: Can this be null?
                writeElementsReport(entry),
                feature.getTags().stream()
                        .map(JsonReportWriter::createLocationTag)
                        .collect(toList())
        );
    }

    private static JvmLocationTag createLocationTag(Tag tag) {
        return new JvmLocationTag(
                tag.getName(),
                "Tag",
                new JvmLocation(
                        tag.getLocation().getLine(),
                        tag.getLocation().getColumn().orElse(0L)
                )
        );
    }

    private GherkinDocument getGherkinDocument(Entry<Optional<Feature>, List<TestCaseStarted>> entry) {
        return entry.getValue().stream()
                .findFirst()
                .flatMap(query::findGherkinDocumentBy)
                .orElseThrow(() -> new IllegalArgumentException("No Gherkin document"));
    }

    private List<JvmElement> writeElementsReport(Entry<Optional<Feature>, List<TestCaseStarted>> entry) {
        return entry.getValue().stream()
                .map(this::createTestCaseAndBackGround)
                .flatMap(Collection::stream)
                .collect(toList());
    }

    private List<JvmElement> createTestCaseAndBackGround(TestCaseStarted testCaseStarted) {
        return query.findTestStepsFinishedBy(testCaseStarted)
                .stream()
                .collect(groupByBackground(testCaseStarted))
                .entrySet().stream()
                .map(entry -> entry.getKey().map(background -> createBackground(background, entry.getValue()))
                        .orElseGet(() -> createTestCase(testCaseStarted, entry.getValue()))).collect(toList());
    }

    private Collector<TestStepFinished, ?, Map<Optional<Background>, List<TestStepFinished>>> groupByBackground(TestCaseStarted testCaseStarted) {
        List<Background> backgrounds = query.findFeatureBy(testCaseStarted)
                .map(JsonReportWriter::getBackgroundForTestCase)
                .orElseGet(Collections::emptyList);

        Function<TestStepFinished, Optional<Background>> grouping = testStepFinished -> query.findTestStepBy(testStepFinished)
                .flatMap(query::findPickleStepBy)
                .flatMap(pickleStep -> findBackgroundBy(backgrounds, pickleStep));

        return groupingBy(grouping, LinkedHashMap::new, toList());
    }

    private static Optional<Background> findBackgroundBy(List<Background> backgrounds, PickleStep pickleStep) {
        return backgrounds.stream()
                .filter(background -> background.getSteps().stream()
                        .map(Step::getId)
                        .anyMatch(step -> pickleStep.getAstNodeIds().contains(step)))
                .findFirst();
    }

    static List<Background> getBackgroundForTestCase(Feature feature) {
        return feature.getChildren()
                .stream()
                .map(featureChild -> {
                    List<Background> backgrounds = new ArrayList<>();
                    featureChild.getBackground().ifPresent(backgrounds::add);
                    featureChild.getRule()
                            .map(Rule::getChildren)
                            .map(Collection::stream)
                            .orElseGet(Stream::empty)
                            .map(RuleChild::getBackground)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .forEach(backgrounds::add);
                    return backgrounds;
                })
                .flatMap(Collection::stream)
                .collect(toList());
    }

    private JvmElement createBackground(Background background, List<TestStepFinished> testStepsFinished) {
        //String start_timestamp, Integer line, String id, JvmElementType type, String keyword, String name, String description, List<JvmStep> steps, List<JvmHook> before, List<JvmHook> after, List<JvmTag> tags
        return new JvmElement(
                null,
                background.getLocation().getLine(),
                null,
                JvmElementType.background,
                background.getKeyword(),
                background.getName(),
                background.getDescription() != null ? background.getDescription() : "",
                createTestSteps(testStepsFinished),
                null,
                null,
                null
        );
    }


    private JvmElement createTestCase(TestCaseStarted event, List<TestStepFinished> testStepsFinished) {
        Pickle pickle = query.findPickleBy(event).orElseThrow();
        Scenario scenario = query.findScenarioBy(event).orElseThrow();
        NamingStrategy idStrategy = NamingStrategy.strategy(NamingStrategy.Strategy.LONG).delimiter(";").namingVisitor(new IdNamingVisitor()).build();
        return new JvmElement(
                getDateTimeFromTimeStamp(event.getTimestamp()),
                query.findLocationOf(pickle).orElseThrow().getLine(),
                query.findNameOf(pickle, idStrategy),
                JvmElementType.scenario,
                scenario.getKeyword(),
                pickle.getName(),
                scenario.getDescription() != null ? scenario.getDescription() : "",
                createTestSteps(testStepsFinished),
                null, // TODO: Hooks
                null, // TODO: Hooks
                pickle.getTags().isEmpty() ? null : createTags(pickle)
        );
    }

    private List<JvmStep> createTestSteps(List<TestStepFinished> testStepsFinished) {
        return testStepsFinished.stream()
                .map(this::createTestStep)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private static List<JvmTag> createTags(Pickle pickle) {
        return pickle.getTags().stream().map(pickleTag -> new JvmTag(pickleTag.getName())).collect(toList());
    }

    private Optional<JvmStep> createTestStep(TestStepFinished testStepFinished) {
        return query.findTestStepBy(testStepFinished)
                .flatMap(testStep -> query.findPickleStepBy(testStep)
                        .flatMap(query::findStepBy)
                        .map(step -> new JvmStep(
                                step.getKeyword(),
                                step.getLocation().getLine(),
                                createMatchMap(testStep, testStepFinished.getTestStepResult()),
                                step.getText(),
                                createResultMap(testStepFinished.getTestStepResult()),
                                step.getDocString().map(this::createDocStringMap).orElse(null),
                                step.getDataTable().map(this::createDataTableList).orElse(null)
                        ))
                );
    }

    private JvmMatch createMatchMap(TestStep step, TestStepResult result) {
        Optional<SourceReference> source = query.findStepDefinitionBy(step)
                .stream()
                .findFirst()
                .map(StepDefinition::getSourceReference);

        Optional<String> location = source.flatMap(sourceReference -> {
            Optional<String> fromUri = sourceReference.getUri()
                    .map(uri -> renderLocationString(sourceReference, uri));

            Optional<String> fromJavaMethod = sourceReference.getJavaMethod()
                    .map(JsonReportWriter::renderLocationString);

            Optional<String> fromStackTrace = sourceReference.getJavaStackTraceElement()
                    .map(javaStackTraceElement -> renderLocationString(sourceReference, javaStackTraceElement));

            return Stream.of(fromStackTrace, fromJavaMethod, fromUri).filter(Optional::isPresent).map(Optional::get).findFirst();
        });

        Optional<List<JvmArgument>> argumentList = step.getStepMatchArgumentsLists()
                .map(argumentsLists -> argumentsLists.stream()
                        .map(StepMatchArgumentsList::getStepMatchArguments)
                        .flatMap(Collection::stream)
                        .map(argument -> {
                            Group group = argument.getGroup();
                            return new JvmArgument(
                                    // TODO: Nullable
                                    group.getValue().get(),
                                    group.getStart().get()
                            );
                        }).collect(toList()))
                .filter(maps -> !maps.isEmpty());

        return new JvmMatch(
                result.getStatus() != TestStepResultStatus.UNDEFINED ? location.orElse(null) : null,
                argumentList.orElse(null)
        );
    }

    private static String renderLocationString(SourceReference sourceReference, String uri) {
        String locationLine = sourceReference.getLocation().map(location -> ":" + location.getLine()).orElse("");
        return uri + locationLine;
    }

    private static String renderLocationString(SourceReference sourceReference, JavaStackTraceElement javaStackTraceElement) {
        String locationLine = sourceReference.getLocation().map(location -> ":" + location.getLine()).orElse("");
        String argumentList = String.join(",", javaStackTraceElement.getFileName());
        return String.format(
                "%s#%s(%s%s)",
                javaStackTraceElement.getClassName(),
                javaStackTraceElement.getMethodName(),
                argumentList,
                locationLine
        );
    }

    private static String renderLocationString(JavaMethod javaMethod) {
        return String.format(
                "%s#%s(%s)",
                javaMethod.getClassName(),
                javaMethod.getMethodName(),
                String.join(",", javaMethod.getMethodParameterTypes())
        );
    }

    private JvmResult createResultMap(TestStepResult result) {
        Duration duration = Convertor.toDuration(result.getDuration());
        return new JvmResult(
                duration.toNanos(),
                JvmStatus.valueOf(result.getStatus().name().toLowerCase(ROOT)),
                result.getException().flatMap(Exception::getStackTrace).orElse(null)
        );
    }

    private JvmDocString createDocStringMap(DocString docString) {
        return new JvmDocString(
                docString.getLocation().getLine(),
                docString.getContent(),
                docString.getMediaType().orElse(null)
        );
    }

    private List<JvmDataTableRow> createDataTableList(DataTable argument) {
        return argument.getRows().stream()
                .map(row -> new JvmDataTableRow(row.getCells().stream()
                        .map(TableCell::getValue)
                        .collect(toList())))
                .collect(toList());
    }

    private String getDateTimeFromTimeStamp(Timestamp instant) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                .withZone(ZoneOffset.UTC);
        return formatter.format(Convertor.toInstant(instant));
    }


}
