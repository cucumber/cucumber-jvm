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
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.PickleStep;
import io.cucumber.messages.types.PickleTag;
import io.cucumber.messages.types.Rule;
import io.cucumber.messages.types.RuleChild;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.SourceReference;
import io.cucumber.messages.types.Step;
import io.cucumber.messages.types.StepDefinition;
import io.cucumber.messages.types.StepMatchArgument;
import io.cucumber.messages.types.StepMatchArgumentsList;
import io.cucumber.messages.types.TableCell;
import io.cucumber.messages.types.Tag;
import io.cucumber.messages.types.TestCaseStarted;
import io.cucumber.messages.types.TestStep;
import io.cucumber.messages.types.TestStepFinished;
import io.cucumber.messages.types.TestStepResult;
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
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static io.cucumber.messages.types.AttachmentContentEncoding.BASE64;
import static io.cucumber.messages.types.AttachmentContentEncoding.IDENTITY;
import static java.util.Collections.emptyList;
import static java.util.Locale.ROOT;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

class JsonReportWriter {
    private static final Pattern replacementPattern = Pattern.compile("[\\s'_,!]");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            .withZone(ZoneOffset.UTC);
    private final Query query;
    private final SourceReferenceFormatter sourceReferenceFormatter = new SourceReferenceFormatter();
    private final Function<URI, String> uriFormatter;

    JsonReportWriter(Query query, Function<URI, String> uriFormatter) {
        this.query = requireNonNull(query);
        this.uriFormatter = requireNonNull(uriFormatter);
    }

    private static <T> List<T> nullIfEmpty(List<T> list) {
        return list.isEmpty() ? null : list;
    }

    private String formatId(String name) {
        return replacementPattern.matcher(name).replaceAll("-").toLowerCase(Locale.ROOT);
    }

    private String formatTimeStamp(Timestamp instant) {
        return dateTimeFormatter.format(Convertor.toInstant(instant));
    }

    List<Object> writeJsonReport() {
        return query.findAllTestCaseStarted()
                .stream()
                .map(this::createJvmElementData)
                .sorted(new JvmFeatureDataComparator())
                // Preserve order with linked hashmap
                .collect(groupingBy(data -> data.pickle.getUri(), LinkedHashMap::new, toList()))
                .values()
                .stream()
                .map(this::createJvmFeature)
                .collect(toList());
    }

    private JvmElementData createJvmElementData(TestCaseStarted testCaseStarted) {
        Pickle pickle = query.findPickleBy(testCaseStarted)
                .orElseThrow(
                    () -> new IllegalStateException("No Pickle for testCaseStarted " + testCaseStarted.getId()));
        Lineage lineage = query.findLineageBy(pickle)
                .orElseThrow(
                    () -> new IllegalStateException("No Lineage for testCaseStarted " + testCaseStarted.getId()));
        Location location = query.findLocationOf(pickle)
                .orElseThrow(
                    () -> new IllegalStateException("No Location for testCaseStarted " + testCaseStarted.getId()));

        return new JvmElementData(
            testCaseStarted,
            lineage,
            pickle,
            location,
            createTestStepData(testCaseStarted));
    }

    private TestStepData createTestStepData(TestCaseStarted testCaseStarted) {
        List<TestStepFinished> testStepsFinished = query.findTestStepsFinishedBy(testCaseStarted);

        List<TestStepFinished> beforeTestCase = new ArrayList<>();
        List<TestStepFinished> afterTestCase = new ArrayList<>();
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
        return new TestStepData(beforeTestCase, afterTestCase, beforeTestStepByStep, afterTestStepByStep);
    }

    private JvmFeature createJvmFeature(List<JvmElementData> elements) {
        GherkinDocument document = elements.get(0).lineage.document();
        Feature feature = elements.get(0).lineage.feature().orElseThrow(() -> new IllegalStateException("No feature?"));
        return new JvmFeature(
            document.getUri()
                    .map(URI::create)
                    .map(uriFormatter)
                    .orElse(null),
            formatId(feature.getName()),
            feature.getLocation().getLine(),
            feature.getKeyword(),
            feature.getName(),
            // TODO: Can this be null?
            feature.getDescription() != null ? feature.getDescription() : "",
            createJvmElements(elements),
            createJvmLocationTags(feature));
    }

    private List<JvmElement> createJvmElements(List<JvmElementData> entries) {
        return entries.stream()
                .map(this::createJvmElement)
                .flatMap(Collection::stream)
                .collect(toList());
    }

    private List<JvmElement> createJvmElement(JvmElementData data) {
        Predicate<Entry<Optional<Background>, List<TestStepFinished>>> isBackGround = entry -> entry.getKey()
                .isPresent();
        Predicate<Entry<Optional<Background>, List<TestStepFinished>>> isTestCase = isBackGround.negate();
        BinaryOperator<Entry<Optional<Background>, List<TestStepFinished>>> mergeEntries = (a, b) -> {
            a.getValue().addAll(b.getValue());
            return a;
        };

        Map<Optional<Background>, List<TestStepFinished>> stepsByBackground = query
                .findTestStepFinishedAndTestStepBy(data.testCaseStarted)
                .stream()
                .collect(groupTestStepsByBackground(data));

        // There can be multiple backgrounds, but historically the json format
        // only ever had one. So we group all other backgrounds steps with the
        // first.
        Optional<JvmElement> background = stepsByBackground.entrySet().stream()
                .filter(isBackGround)
                .reduce(mergeEntries)
                .flatMap(entry -> entry.getKey()
                        .map(bg -> createBackground(data, bg, entry.getValue())));

        Optional<JvmElement> testCase = stepsByBackground.entrySet().stream()
                .filter(isTestCase)
                .reduce(mergeEntries)
                .map(Entry::getValue)
                .map(scenarioTestStepsFinished -> createTestCase(data, scenarioTestStepsFinished));

        return Stream.of(background, testCase)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private JvmElement createTestCase(JvmElementData data, List<TestStepFinished> scenarioTestStepsFinished) {
        Scenario scenario = data.lineage.scenario().orElseThrow(() -> new IllegalStateException("No scenario?"));
        LineageReducer<String> idStrategy = LineageReducer.descending(IdNamingVisitor::new);
        return new JvmElement(
            formatTimeStamp(data.testCaseStarted.getTimestamp()),
            data.location.getLine(),
            idStrategy.reduce(data.lineage),
            JvmElementType.scenario,
            scenario.getKeyword(),
            data.pickle.getName(),
            scenario.getDescription() == null ? "" : scenario.getDescription(),
            createTestSteps(data, scenarioTestStepsFinished),
            nullIfEmpty(createHookSteps(data.testStepData.beforeTestCaseSteps)),
            nullIfEmpty(createHookSteps(data.testStepData.afterTestCaseSteps)),
            nullIfEmpty(createJvmTags(data.pickle)));
    }

    private List<JvmTag> createJvmTags(Pickle pickle) {
        return pickle.getTags()
                .stream()
                .map(this::createJvmTag)
                .collect(toList());
    }

    private JvmTag createJvmTag(PickleTag pickleTag) {
        return new JvmTag(pickleTag.getName());
    }

    private List<CucumberJvmJson.JvmHook> createHookSteps(List<TestStepFinished> testStepsFinished) {
        return testStepsFinished.stream()
                .map(testStepFinished -> query.findTestStepBy(testStepFinished)
                        .flatMap(testStep -> query.findHookBy(testStep)
                                .map(hook -> new CucumberJvmJson.JvmHook(
                                    createJvmMatch(testStep),
                                    createJvmResult(testStepFinished.getTestStepResult()),
                                    createEmbeddings(query.findAttachmentsBy(testStepFinished)),
                                    createOutput(query.findAttachmentsBy(testStepFinished))))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private JvmResult createJvmResult(TestStepResult result) {
        Duration duration = Convertor.toDuration(result.getDuration());
        return new JvmResult(
            duration.isZero() ? null : duration.toNanos(),
            JvmStatus.valueOf(result.getStatus().name().toLowerCase(ROOT)),
            result.getException().flatMap(Exception::getStackTrace).orElse(null));
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

    private List<JvmLocationTag> createJvmLocationTags(Feature feature) {
        return feature.getTags().stream()
                .map(this::createJvmLocationTag)
                .collect(toList());
    }

    private JvmLocationTag createJvmLocationTag(Tag tag) {
        return new JvmLocationTag(
            tag.getName(),
            "Tag",
            new JvmLocation(
                tag.getLocation().getLine(),
                tag.getLocation().getColumn().orElse(0L)));
    }

    private JvmElement createBackground(
            JvmElementData data, Background background, List<TestStepFinished> backgroundTestStepsFinished
    ) {
        return new JvmElement(
            null,
            background.getLocation().getLine(),
            null,
            JvmElementType.background,
            background.getKeyword(),
            background.getName(),
            background.getDescription() != null ? background.getDescription() : "",
            createTestSteps(data, backgroundTestStepsFinished),
            null,
            null,
            null);
    }

    private List<JvmStep> createTestSteps(JvmElementData data, List<TestStepFinished> testStepsFinished) {
        return testStepsFinished.stream()
                .map(testStepFinished -> createTestStep(data, testStepFinished))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private Optional<JvmStep> createTestStep(JvmElementData data, TestStepFinished testStepFinished) {
        List<TestStepFinished> beforeStepHooks = data.testStepData.beforeStepStepsByStep
                .getOrDefault(testStepFinished, emptyList());
        List<TestStepFinished> afterStepHooks = data.testStepData.afterStepStepsByStep
                .getOrDefault(testStepFinished, emptyList());

        return query.findTestStepBy(testStepFinished)
                .flatMap(testStep -> query.findPickleStepBy(testStep)
                        .flatMap(pickleStep -> query.findStepBy(pickleStep)
                                .map(step -> new JvmStep(
                                    step.getKeyword(),
                                    step.getLocation().getLine(),
                                    createJvmMatch(testStep),
                                    pickleStep.getText(),
                                    createJvmResult(testStepFinished.getTestStepResult()),
                                    createJvmDocString(step),
                                    createJvmDataTableRows(step),
                                    nullIfEmpty(createHookSteps(beforeStepHooks)),
                                    nullIfEmpty(createHookSteps(afterStepHooks))))));
    }

    private JvmMatch createJvmMatch(TestStep testStep) {
        return new JvmMatch(
            createLocation(testStep),
            createJvmArguments(testStep));
    }

    private List<JvmArgument> createJvmArguments(TestStep step) {
        return step.getStepMatchArgumentsLists()
                .map(argumentsLists -> argumentsLists.stream()
                        .map(StepMatchArgumentsList::getStepMatchArguments)
                        .flatMap(Collection::stream)
                        .map(JsonReportWriter::createJvmArgument)
                        .collect(toList()))
                .filter(jvmArguments -> !jvmArguments.isEmpty())
                .orElse(null);
    }

    private static JvmArgument createJvmArgument(StepMatchArgument argument) {
        Group group = argument.getGroup();
        return new JvmArgument(
            group.getValue().orElse(null),
            group.getStart().orElse(-1L));
    }

    private List<JvmDataTableRow> createJvmDataTableRows(Step step) {
        return step.getDataTable().map(this::createJvmDataTableRows).orElse(null);
    }

    private List<JvmDataTableRow> createJvmDataTableRows(DataTable argument) {
        return argument.getRows().stream()
                .map(row -> new JvmDataTableRow(row.getCells().stream()
                        .map(TableCell::getValue)
                        .collect(toList())))
                .collect(toList());
    }

    private JvmDocString createJvmDocString(Step step) {
        return step.getDocString().map(this::createJvmDocString).orElse(null);
    }

    private JvmDocString createJvmDocString(DocString docString) {
        return new JvmDocString(
            docString.getLocation().getLine(),
            docString.getContent(),
            docString.getMediaType().orElse(null));
    }

    private String createLocation(TestStep step) {
        return findSourceReference(step)
                .flatMap(sourceReferenceFormatter::format)
                .orElse(null);
    }

    private Collector<Entry<TestStepFinished, TestStep>, ?, Map<Optional<Background>, List<TestStepFinished>>> groupTestStepsByBackground(
            JvmElementData data
    ) {
        List<Background> backgrounds = data.lineage.feature()
                .map(this::findBackgroundsBy)
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

    private List<Background> findBackgroundsBy(Feature feature) {
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

    private Optional<Background> findBackgroundBy(List<Background> backgrounds, PickleStep pickleStep) {
        return backgrounds.stream()
                .filter(background -> background.getSteps().stream()
                        .map(Step::getId)
                        .anyMatch(step -> pickleStep.getAstNodeIds().contains(step)))
                .findFirst();
    }

    private Optional<SourceReference> findSourceReference(TestStep step) {
        Optional<SourceReference> source = query.findUnambiguousStepDefinitionBy(step)
                .map(StepDefinition::getSourceReference);

        if (source.isPresent()) {
            return source;
        }

        return query.findHookBy(step)
                .map(Hook::getSourceReference);
    }

    private static class TestStepData {
        private final List<TestStepFinished> beforeTestCaseSteps;
        private final List<TestStepFinished> afterTestCaseSteps;
        private final Map<TestStepFinished, List<TestStepFinished>> beforeStepStepsByStep;
        private final Map<TestStepFinished, List<TestStepFinished>> afterStepStepsByStep;

        private TestStepData(
                List<TestStepFinished> beforeTestCaseSteps,
                List<TestStepFinished> afterTestCaseSteps,
                Map<TestStepFinished, List<TestStepFinished>> beforeStepStepsByStep,
                Map<TestStepFinished, List<TestStepFinished>> afterStepStepsByStep
        ) {
            this.beforeTestCaseSteps = beforeTestCaseSteps;
            this.afterTestCaseSteps = afterTestCaseSteps;
            this.beforeStepStepsByStep = beforeStepStepsByStep;
            this.afterStepStepsByStep = afterStepStepsByStep;
        }
    }

    private static class JvmElementData {
        private final TestCaseStarted testCaseStarted;
        private final Lineage lineage;
        private final Pickle pickle;
        private final Location location;
        private final TestStepData testStepData;

        private JvmElementData(
                TestCaseStarted testCaseStarted, Lineage lineage, Pickle pickle, Location location,
                TestStepData testStepData
        ) {
            this.testCaseStarted = requireNonNull(testCaseStarted);
            this.lineage = requireNonNull(lineage);
            this.pickle = requireNonNull(pickle);
            this.location = requireNonNull(location);
            this.testStepData = requireNonNull(testStepData);
        }
    }

    private static class JvmFeatureDataComparator implements Comparator<JvmElementData> {

        @Override
        public int compare(JvmElementData o1, JvmElementData o2) {
            int c = o1.pickle.getUri().compareTo(o2.pickle.getUri());
            if (c != 0) {
                return c;
            }
            return o1.location.getLine().compareTo(o2.location.getLine());
        }
    }

}
