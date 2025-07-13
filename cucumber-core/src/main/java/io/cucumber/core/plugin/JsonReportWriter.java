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
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import static io.cucumber.messages.types.HookType.AFTER_TEST_STEP;
import static io.cucumber.messages.types.HookType.BEFORE_TEST_STEP;
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

    private static Predicate<Hook> include(HookType... hookTypes) {
        List<HookType> keep = Arrays.asList(hookTypes);
        return hook -> hook.getType().map(keep::contains).orElse(false);
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

    private static int findIndexOf(
            List<Entry<Optional<HookType>, TestStepFinished>> withHookType, TestStepFinished testStepFinished
    ) {
        for (int i = 0; i < withHookType.size(); i++) {
            TestStepFinished candidate = withHookType.get(i).getValue();
            if (testStepFinished.equals(candidate)) {
                return i;
            }

        }
        return -1;
    }

    private static List<TestStepFinished> findAfterStepHooksAfter(
            List<Entry<Optional<HookType>, TestStepFinished>> withHookType, int stepIndex
    ) {
        List<TestStepFinished> afterStepHooks = new ArrayList<>();
        // after step hooks, come after the step, so search forward.
        for (int hookIndex = stepIndex + 1; hookIndex < withHookType.size(); hookIndex++) {
            Entry<Optional<HookType>, TestStepFinished> candidate = withHookType.get(hookIndex);
            boolean isAfterStepHook = candidate.getKey().map(AFTER_TEST_STEP::equals).orElse(false);
            // We found the end of this sequence
            if (!isAfterStepHook) {
                break;
            }
            afterStepHooks.add(candidate.getValue());
        }
        return afterStepHooks;
    }

    private static List<TestStepFinished> findBeforeStepHooksBefore(
            List<Entry<Optional<HookType>, TestStepFinished>> withHookType, int stepIndex
    ) {
        List<TestStepFinished> beforeStepHooks = new ArrayList<>();
        // before step hooks, come before the step, so search in reverse.
        for (int hookIndex = stepIndex - 1; hookIndex >= 0; hookIndex--) {
            Entry<Optional<HookType>, TestStepFinished> candidate = withHookType.get(hookIndex);
            boolean isBeforeStepHook = candidate.getKey().map(BEFORE_TEST_STEP::equals).orElse(false);
            // We found the end of this sequence
            if (!isBeforeStepHook) {
                break;
            }
            beforeStepHooks.add(candidate.getValue());
        }
        return beforeStepHooks;
    }

    List<Object> writeJsonReport() {
        return query.findAllTestCaseStarted()
                .stream()
                .map(this::createTestCaseStartedData)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing((TestCaseData data) -> data.pickle.getUri())
                        .thenComparing(data -> data.location.getLine()))
                .collect(groupingBy(data -> data.pickle.getUri(), LinkedHashMap::new, toList()))
                .values()
                .stream()
                .map(this::createFeatureMap)
                .collect(toList());
    }

    private Optional<TestCaseData> createTestCaseStartedData(TestCaseStarted testCaseStarted) {
        return query.findPickleBy(testCaseStarted)
                .flatMap(pickle -> query.findLineageBy(pickle)
                        .flatMap(lineage -> query.findLocationOf(pickle)
                                .map(location -> {
                                    List<Entry<TestStepFinished, TestStep>> testStepFinishedAndTestStep = query
                                            .findTestStepFinishedAndTestStepBy(testCaseStarted);
                                    return new TestCaseData(testCaseStarted, lineage, pickle, location,
                                        testStepFinishedAndTestStep);
                                })));
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
        Map<Optional<Background>, List<TestStepFinished>> stepsByBackground = data.testStepFinishedAndTestStep
                .stream()
                .collect(groupByBackground(data));

        // There can be multiple backgrounds, but historically the json format
        // only ever had one. So we group all other backgrounds steps with the
        // first.
        Optional<JvmElement> background = stepsByBackground.entrySet().stream()
                .filter(isBackGround)
                .reduce(mergeSteps)
                .flatMap(entry -> entry.getKey().map(bg -> createBackground(bg, entry.getValue())));

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

    private JvmElement createBackground(Background background, List<TestStepFinished> testStepsFinished) {
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
            null);
    }

    private JvmElement createTestCase(TestCaseData data, List<TestStepFinished> testStepsFinished) {
        Scenario scenario = data.lineage.scenario().orElseThrow(() -> new IllegalStateException("No scenario?"));
        LineageReducer<String> idStrategy = LineageReducer.descending(IdNamingVisitor::new);
        List<CucumberJvmJson.JvmHook> beforeHooks = createHookSteps(testStepsFinished,
            include(HookType.BEFORE_TEST_CASE));
        List<CucumberJvmJson.JvmHook> afterHooks = createHookSteps(testStepsFinished,
            include(HookType.AFTER_TEST_CASE));
        return new JvmElement(
            getDateTimeFromTimeStamp(data.testCaseStarted.getTimestamp()),
            data.location.getLine(),
            idStrategy.reduce(data.lineage),
            JvmElementType.scenario,
            scenario.getKeyword(),
            data.pickle.getName(),
            scenario.getDescription() == null ? "" : scenario.getDescription(),
            createTestSteps(testStepsFinished),
            beforeHooks.isEmpty() ? null : beforeHooks,
            afterHooks.isEmpty() ? null : afterHooks,
            data.pickle.getTags().isEmpty() ? null : createTags(data.pickle));
    }

    private List<CucumberJvmJson.JvmHook> createHookSteps(
            List<TestStepFinished> testStepsFinished, Predicate<Hook> predicate
    ) {
        return testStepsFinished.stream()
                .map(testStepFinished -> query.findTestStepBy(testStepFinished)
                        .flatMap(testStep -> query.findHookBy(testStep)
                                .filter(predicate)
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

    private List<JvmStep> createTestSteps(List<TestStepFinished> testStepsFinished) {
        List<Entry<Optional<HookType>, TestStepFinished>> testStepsFinishedWithHookType = mapTestStepsFinishedToHookType(
            testStepsFinished);
        return testStepsFinishedWithHookType
                .stream()
                .filter(testStepFinished -> !testStepFinished.getKey().isPresent())
                .map(Entry::getValue)
                .map(testStepFinished1 -> {
                    List<TestStepFinished> beforeStepHooks = findHooksFor(testStepsFinishedWithHookType,
                        testStepFinished1, BEFORE_TEST_STEP);
                    List<TestStepFinished> afterStepHooks = findHooksFor(testStepsFinishedWithHookType,
                        testStepFinished1, AFTER_TEST_STEP);
                    return createTestStep(testStepFinished1, beforeStepHooks, afterStepHooks);
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private List<TestStepFinished> findHooksFor(
            List<Entry<Optional<HookType>, TestStepFinished>> withHookType, TestStepFinished testStepFinished,
            HookType targetHookType
    ) {
        int stepIndex = findIndexOf(withHookType, testStepFinished);
        if (stepIndex < 0) {
            return Collections.emptyList();
        }
        if (targetHookType == BEFORE_TEST_STEP) {
            return findBeforeStepHooksBefore(withHookType, stepIndex);
        }
        if (targetHookType == AFTER_TEST_STEP) {
            return findAfterStepHooksAfter(withHookType, stepIndex);
        }
        return Collections.emptyList();
    }

    private List<Entry<Optional<HookType>, TestStepFinished>> mapTestStepsFinishedToHookType(
            List<TestStepFinished> testStepsFinished
    ) {
        return testStepsFinished.stream()
                .map(testStepFinished -> new SimpleEntry<>(query.findTestStepBy(testStepFinished)
                        .flatMap(query::findHookBy)
                        .flatMap(Hook::getType),
                    testStepFinished))
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
                                    List<CucumberJvmJson.JvmHook> jvmBeforeStepHooks = createHookSteps(beforeStepHooks,
                                        include(BEFORE_TEST_STEP));
                                    List<CucumberJvmJson.JvmHook> jvmAfterStepHooks = createHookSteps(afterStepHooks,
                                        include(AFTER_TEST_STEP));
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

    private static class TestCaseData {
        private final TestCaseStarted testCaseStarted;
        private final Lineage lineage;
        private final Pickle pickle;
        private final Location location;
        private final List<Entry<TestStepFinished, TestStep>> testStepFinishedAndTestStep;

        private TestCaseData(
                TestCaseStarted testCaseStarted, Lineage lineage, Pickle pickle, Location location,
                List<Entry<TestStepFinished, TestStep>> testStepFinishedAndTestStep
        ) {
            this.testCaseStarted = testCaseStarted;
            this.lineage = lineage;
            this.pickle = pickle;
            this.location = location;
            this.testStepFinishedAndTestStep = testStepFinishedAndTestStep;
        }
    }

}
