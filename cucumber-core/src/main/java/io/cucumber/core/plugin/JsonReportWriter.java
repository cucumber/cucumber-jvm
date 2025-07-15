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
import io.cucumber.messages.types.Attachment;
import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.DataTable;
import io.cucumber.messages.types.DocString;
import io.cucumber.messages.types.Exception;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Group;
import io.cucumber.messages.types.Hook;
import io.cucumber.messages.types.HookType;
import io.cucumber.messages.types.JavaMethod;
import io.cucumber.messages.types.JavaStackTraceElement;
import io.cucumber.messages.types.Location;
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
import io.cucumber.query.Lineage;
import io.cucumber.query.LineageReducer;
import io.cucumber.query.Query;

import java.net.URI;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static io.cucumber.core.plugin.TestSourcesModel.convertToId;
import static io.cucumber.messages.types.AttachmentContentEncoding.BASE64;
import static io.cucumber.messages.types.AttachmentContentEncoding.IDENTITY;
import static java.util.Collections.emptyList;
import static java.util.Locale.ROOT;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

class JsonReportWriter {
    private final Query query;

    JsonReportWriter(Query query) {
        this.query = query;
    }

    private static JvmLocationTag createLocationTag(Tag tag) {
        return new JvmLocationTag(
            tag.getName(),
            "Tag",
            new JvmLocation(
                tag.getLocation().getLine(),
                tag.getLocation().getColumn().orElse(0L)));
    }

    private static Optional<Background> findBackgroundBy(List<Background> backgrounds, PickleStep pickleStep) {
        return backgrounds.stream()
                .filter(background -> background.getSteps().stream()
                        .map(Step::getId)
                        .anyMatch(step -> pickleStep.getAstNodeIds().contains(step)))
                .findFirst();
    }

    private static List<JvmTag> createTags(Pickle pickle) {
        return pickle.getTags().stream().map(pickleTag -> new JvmTag(pickleTag.getName())).collect(toList());
    }

    private static String renderLocationString(SourceReference sourceReference, String uri) {
        String locationLine = sourceReference.getLocation().map(location -> ":" + location.getLine()).orElse("");
        return uri + locationLine;
    }

    private static String renderLocationString(
            SourceReference sourceReference, JavaStackTraceElement javaStackTraceElement
    ) {
        String locationLine = sourceReference.getLocation().map(location -> ":" + location.getLine()).orElse("");
        String argumentList = String.join(",", javaStackTraceElement.getFileName());
        return String.format(
            "%s#%s(%s%s)",
            javaStackTraceElement.getClassName(),
            javaStackTraceElement.getMethodName(),
            argumentList,
            locationLine);
    }

    private static String renderLocationString(JavaMethod javaMethod) {
        return String.format(
            "%s#%s(%s)",
            javaMethod.getClassName(),
            javaMethod.getMethodName(),
            String.join(",", javaMethod.getMethodParameterTypes()));
    }

    List<Object> writeJsonReport() {
        return query.findAllTestCaseStarted()
                .stream()
                .map(this::createTestCaseStartedData)
                .sorted(Comparator.comparing((TestCaseData data) -> data.pickle.getUri())
                        .thenComparing(data -> data.location.getLine()))
                .collect(groupingBy(data -> data.pickle.getUri(), LinkedHashMap::new, toList()))
                .values()
                .stream()
                .map(this::createFeatureMap)
                .collect(toList());
    }

    private TestCaseData createTestCaseStartedData(TestCaseStarted testCaseStarted) {
        Pickle pickle = query.findPickleBy(testCaseStarted)
                .orElseThrow(
                    () -> new IllegalStateException("No Pickle for testCaseStarted " + testCaseStarted.getId()));
        Lineage lineage = query.findLineageBy(pickle)
                .orElseThrow(
                    () -> new IllegalStateException("No Lineage for testCaseStarted " + testCaseStarted.getId()));
        Location location = query.findLocationOf(pickle)
                .orElseThrow(
                    () -> new IllegalStateException("No Location for testCaseStarted " + testCaseStarted.getId()));

        TestStepData testStepData = createTestStepData(testCaseStarted);

        return new TestCaseData(
            testCaseStarted,
            lineage,
            pickle,
            location,
            testStepData
        );

    }

    private TestStepData createTestStepData(TestCaseStarted testCaseStarted) {
        List<TestStepFinished> testStepsFinished = query.findTestStepsFinishedBy(testCaseStarted);

        List<TestStepFinished> beforeTestCase = new ArrayList<>();
        List<TestStepFinished> afterTestCase = new ArrayList<>();
        List<TestStepFinished> testSteps = new ArrayList<>();
        List<TestStepFinished> beforeTestStep = new ArrayList<>();
        List<TestStepFinished> afterTestStep = new ArrayList<>();
        Map<TestStepFinished, List<TestStepFinished>> beforeTestStepByStep = new HashMap<>();
        Map<TestStepFinished, List<TestStepFinished>> afterTestStepByStep = new HashMap<>();

        for (TestStepFinished testStepFinished : testStepsFinished) {
            HookType hook = query.findTestStepBy(testStepFinished)
                    .flatMap(query::findHookBy)
                    .flatMap(Hook::getType)
                    .orElse(null);

            if (hook == null) {
                beforeTestStepByStep.put(testStepFinished, beforeTestStep);
                beforeTestStep = new ArrayList<>();
                testSteps.add(testStepFinished);
                afterTestStep = new ArrayList<>();
                afterTestStepByStep.put(testStepFinished, afterTestStep);
                continue;
            }

            switch (hook) {
                case BEFORE_TEST_RUN:
                case AFTER_TEST_RUN:
                    break;
                case BEFORE_TEST_CASE:
                    beforeTestCase.add(testStepFinished);
                    break;
                case AFTER_TEST_CASE:
                    afterTestCase.add(testStepFinished);
                    break;
                case BEFORE_TEST_STEP:
                    beforeTestStep.add(testStepFinished);
                    break;
                case AFTER_TEST_STEP:
                    afterTestStep.add(testStepFinished);
                    break;
            }

        }
        return new TestStepData(testSteps, beforeTestCase, afterTestCase, beforeTestStepByStep, afterTestStepByStep);
    }

    private JvmFeature createFeatureMap(List<TestCaseData> entries) {
        GherkinDocument document = entries.get(0).lineage.document();
        Feature feature = entries.get(0).lineage.feature().orElseThrow(() -> new IllegalStateException("No feature?"));
        return new JvmFeature(
            // TODO: Relativize, optional?, null?
            TestSourcesModel.relativize(URI.create(document.getUri().get())).toString(),
            convertToId(feature.getName()),
            feature.getLocation().getLine(),
            feature.getKeyword(),
            feature.getName(),
            // TODO: Can this be null?
            feature.getDescription() != null ? feature.getDescription() : "",
            writeElementsReport(entries),
            feature.getTags().stream()
                    .map(JsonReportWriter::createLocationTag)
                    .collect(toList()));
    }

    private List<JvmElement> writeElementsReport(List<TestCaseData> entries) {
        return entries.stream()
                .map(this::createTestCaseAndBackGround)
                .flatMap(Collection::stream)
                .collect(toList());
    }

    private List<JvmElement> createTestCaseAndBackGround(TestCaseData data) {
        // TODO: Clean up
        Predicate<Entry<Optional<Background>, List<TestStepFinished>>> isBackGround = entry -> entry.getKey()
                .isPresent();
        Predicate<Entry<Optional<Background>, List<TestStepFinished>>> isTestCase = isBackGround.negate();
        BinaryOperator<Entry<Optional<Background>, List<TestStepFinished>>> mergeSteps = (a, b) -> {
            a.getValue().addAll(b.getValue());
            return a;
        };
        Map<Optional<Background>, List<TestStepFinished>> stepsByBackground = query
                .findTestStepFinishedAndTestStepBy(data.testCaseStarted)
                .stream()
                .collect(groupByBackground(data));

        // There can be multiple backgrounds, but historically the json format
        // only ever had one. So we group all other backgrounds steps with the
        // first.
        Optional<JvmElement> background = stepsByBackground.entrySet().stream()
                .filter(isBackGround)
                .reduce(mergeSteps)
                .flatMap(entry -> entry.getKey().map(bg -> createBackground(data, bg, entry.getValue())));

        Optional<JvmElement> testCase = stepsByBackground.entrySet().stream()
                .filter(isTestCase)
                .reduce(mergeSteps)
                .map(Entry::getValue)
                .map(testStepFinished -> createTestCase(data, testStepFinished));

        return Stream.of(background, testCase)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private Collector<Entry<TestStepFinished, TestStep>, ?, Map<Optional<Background>, List<TestStepFinished>>> groupByBackground(
            TestCaseData testCaseData
    ) {
        List<Background> backgrounds = testCaseData.lineage.feature()
                .map(this::getBackgroundsBy)
                .orElseGet(Collections::emptyList);

        Function<Entry<TestStepFinished, TestStep>, Optional<Background>> grouping = entry -> query
                .findPickleStepBy(entry.getValue())
                .flatMap(pickleStep -> findBackgroundBy(backgrounds, pickleStep));

        Function<List<Entry<TestStepFinished, TestStep>>, List<TestStepFinished>> extractKey = entries -> entries
                .stream()
                .map(Entry::getKey)
                .collect(toList());

        return groupingBy(grouping, LinkedHashMap::new, collectingAndThen(toList(), extractKey));
    }

    private List<Background> getBackgroundsBy(Feature feature) {
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

    private JvmElement createBackground(
            TestCaseData data, Background background, List<TestStepFinished> testStepsFinished
    ) {
        return new JvmElement(
            null,
            background.getLocation().getLine(),
            null,
            JvmElementType.background,
            background.getKeyword(),
            background.getName(),
            background.getDescription() != null ? background.getDescription() : "",
            createTestSteps(data, testStepsFinished),
            null,
            null,
            null);
    }

    private JvmElement createTestCase(TestCaseData data, List<TestStepFinished> testStepsFinished) {
        Scenario scenario = data.lineage.scenario().orElseThrow(() -> new IllegalStateException("No scenario?"));
        LineageReducer<String> idStrategy = LineageReducer.descending(IdNamingVisitor::new);
        List<CucumberJvmJson.JvmHook> beforeHooks = createHookSteps(data.testStepData.beforeTestCaseSteps);
        List<CucumberJvmJson.JvmHook> afterHooks = createHookSteps(data.testStepData.afterTestCaseSteps);
        return new JvmElement(
            getDateTimeFromTimeStamp(data.testCaseStarted.getTimestamp()),
            data.location.getLine(),
            idStrategy.reduce(data.lineage),
            JvmElementType.scenario,
            scenario.getKeyword(),
            data.pickle.getName(),
            scenario.getDescription() == null ? "" : scenario.getDescription(),
            createTestSteps(data, testStepsFinished),
            beforeHooks.isEmpty() ? null : beforeHooks,
            afterHooks.isEmpty() ? null : afterHooks,
            data.pickle.getTags().isEmpty() ? null : createTags(data.pickle));
    }

    private List<CucumberJvmJson.JvmHook> createHookSteps(List<TestStepFinished> testStepsFinished) {
        return testStepsFinished.stream()
                .map(testStepFinished -> query.findTestStepBy(testStepFinished)
                        .flatMap(testStep -> query.findHookBy(testStep)
                                .map(hook -> new CucumberJvmJson.JvmHook(
                                    createMatchMap(testStep, testStepFinished.getTestStepResult()),
                                    createResultMap(testStepFinished.getTestStepResult()),
                                    createEmbeddings(query.findAttachmentsBy(testStepFinished)),
                                    createOutput(query.findAttachmentsBy(testStepFinished))))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private List<CucumberJvmJson.JvmEmbedding> createEmbeddings(List<Attachment> attachments) {
        if (attachments.isEmpty()) {
            return null;
        }
        List<CucumberJvmJson.JvmEmbedding> embeddings = attachments.stream()
                .filter(attachment -> attachment.getContentEncoding() == BASE64)
                .map(attachment -> new CucumberJvmJson.JvmEmbedding(
                    attachment.getMediaType(),
                    attachment.getBody(),
                    attachment.getFileName().orElse(null)))
                .collect(toList());

        if (embeddings.isEmpty()) {
            return null;
        }
        return embeddings;
    }

    private List<String> createOutput(List<Attachment> attachments) {
        if (attachments.isEmpty()) {
            return null;
        }
        List<String> outputs = attachments.stream()
                .filter(attachment -> attachment.getContentEncoding() == IDENTITY)
                .map(Attachment::getBody)
                .collect(toList());

        if (outputs.isEmpty()) {
            return null;
        }
        return outputs;
    }

    private List<JvmStep> createTestSteps(TestCaseData data, List<TestStepFinished> testStepsFinished) {
        return testStepsFinished.stream()
                .map(testStepFinished -> {
                    List<TestStepFinished> beforeStepHooks = data.testStepData.beforeStepStepsByStep
                            .getOrDefault(testStepFinished, emptyList());
                    List<TestStepFinished> afterStepHooks = data.testStepData.afterStepStepsByStep
                            .getOrDefault(testStepFinished, emptyList());
                    return createTestStep(testStepFinished, beforeStepHooks, afterStepHooks);
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private Optional<JvmStep> createTestStep(
            TestStepFinished testStepFinished, List<TestStepFinished> beforeStepHooks,
            List<TestStepFinished> afterStepHooks
    ) {
        return query.findTestStepBy(testStepFinished)
                .flatMap(testStep -> query.findPickleStepBy(testStep)
                        .flatMap(pickleStep -> query.findStepBy(pickleStep)
                                .map(step -> {
                                    List<CucumberJvmJson.JvmHook> jvmBeforeStepHooks = createHookSteps(beforeStepHooks);
                                    List<CucumberJvmJson.JvmHook> jvmAfterStepHooks = createHookSteps(afterStepHooks);
                                    return new JvmStep(
                                        step.getKeyword(),
                                        step.getLocation().getLine(),
                                        createMatchMap(testStep, testStepFinished.getTestStepResult()),
                                        pickleStep.getText(),
                                        createResultMap(testStepFinished.getTestStepResult()),
                                        step.getDocString().map(this::createDocStringMap).orElse(null),
                                        step.getDataTable().map(this::createDataTableList).orElse(null),
                                        jvmBeforeStepHooks.isEmpty() ? null : jvmBeforeStepHooks,
                                        jvmAfterStepHooks.isEmpty() ? null : jvmAfterStepHooks);
                                })));
    }

    private JvmMatch createMatchMap(TestStep step, TestStepResult result) {
        Optional<SourceReference> source = query.findUnambiguousStepDefinitionBy(step)
                .map(StepDefinition::getSourceReference);

        Optional<SourceReference> hookSource = query.findHookBy(step)
                .map(Hook::getSourceReference);

        Optional<String> location = Stream.of(source, hookSource)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst().flatMap(sourceReference -> {
                    Optional<String> fromUri = sourceReference.getUri()
                            .map(uri -> renderLocationString(sourceReference, uri));

                    Optional<String> fromJavaMethod = sourceReference.getJavaMethod()
                            .map(JsonReportWriter::renderLocationString);

                    Optional<String> fromStackTrace = sourceReference.getJavaStackTraceElement()
                            .map(javaStackTraceElement -> renderLocationString(sourceReference, javaStackTraceElement));

                    return Stream.of(fromStackTrace, fromJavaMethod, fromUri).filter(Optional::isPresent)
                            .map(Optional::get)
                            .findFirst();
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
                                group.getStart().get());
                        }).collect(toList()))
                .filter(maps -> !maps.isEmpty());

        return new JvmMatch(
            result.getStatus() != TestStepResultStatus.UNDEFINED ? location.orElse(null) : null,
            argumentList.orElse(null));
    }

    private JvmResult createResultMap(TestStepResult result) {
        Duration duration = Convertor.toDuration(result.getDuration());
        return new JvmResult(
            duration.isZero() ? null : duration.toNanos(),
            JvmStatus.valueOf(result.getStatus().name().toLowerCase(ROOT)),
            result.getException().flatMap(Exception::getStackTrace).orElse(null));
    }

    private JvmDocString createDocStringMap(DocString docString) {
        return new JvmDocString(
            docString.getLocation().getLine(),
            docString.getContent(),
            docString.getMediaType().orElse(null));
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

    private static class TestStepData {
        private final List<TestStepFinished> testSteps;
        private final List<TestStepFinished> beforeTestCaseSteps;
        private final List<TestStepFinished> afterTestCaseSteps;
        private final Map<TestStepFinished, List<TestStepFinished>> beforeStepStepsByStep;
        private final Map<TestStepFinished, List<TestStepFinished>> afterStepStepsByStep;

        private TestStepData(
                List<TestStepFinished> testSteps, List<TestStepFinished> beforeTestCaseSteps,
                List<TestStepFinished> afterTestCaseSteps,
                Map<TestStepFinished, List<TestStepFinished>> beforeStepStepsByStep,
                Map<TestStepFinished, List<TestStepFinished>> afterStepStepsByStep
        ) {
            this.testSteps = testSteps;
            this.beforeTestCaseSteps = beforeTestCaseSteps;
            this.afterTestCaseSteps = afterTestCaseSteps;
            this.beforeStepStepsByStep = beforeStepStepsByStep;
            this.afterStepStepsByStep = afterStepStepsByStep;
        }
    }

    private static class TestCaseData {
        private final TestCaseStarted testCaseStarted;
        private final Lineage lineage;
        private final Pickle pickle;
        private final Location location;
        private final TestStepData testStepData;

        private TestCaseData(
                TestCaseStarted testCaseStarted, Lineage lineage, Pickle pickle, Location location,
                TestStepData testStepData
        ) {
            this.testCaseStarted = testCaseStarted;
            this.lineage = lineage;
            this.pickle = pickle;
            this.location = location;
            this.testStepData = testStepData;
        }
    }

}
