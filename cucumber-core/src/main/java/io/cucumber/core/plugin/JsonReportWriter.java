package io.cucumber.core.plugin;

import io.cucumber.messages.Convertor;
import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.DataTable;
import io.cucumber.messages.types.DocString;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Group;
import io.cucumber.messages.types.JavaMethod;
import io.cucumber.messages.types.JavaStackTraceElement;
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.PickleStep;
import io.cucumber.messages.types.PickleTag;
import io.cucumber.messages.types.Rule;
import io.cucumber.messages.types.RuleChild;
import io.cucumber.messages.types.SourceReference;
import io.cucumber.messages.types.Step;
import io.cucumber.messages.types.StepDefinition;
import io.cucumber.messages.types.StepMatchArgumentsList;
import io.cucumber.messages.types.TableCell;
import io.cucumber.messages.types.TableRow;
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
import java.util.HashMap;
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
import static java.util.Objects.requireNonNull;
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
        LinkedHashMap<String, Object> featureMap = new LinkedHashMap<>();
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
        return entry.getValue().stream().findAny().flatMap(query::findGherkinDocumentBy).orElseThrow(() -> new IllegalArgumentException("No Gherkin document"));
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
                "background",
                background.getKeyword(),
                background.getName(),
                background.getDescription() != null ? background.getDescription() : "",
                testStepsFinished.stream().map(this::createTestStep).collect(toList()),
                null,
                null,
                null
        );
    }


    private JvmElement createTestCase(TestCaseStarted event, List<TestStepFinished> testStepsFinished) {
        Map<String, Object> testCaseMap = new HashMap<>();

        testCaseMap.put("start_timestamp", getDateTimeFromTimeStamp(event.getTimestamp()));

        Pickle pickle = query.findPickleBy(event).orElseThrow();
        Location location = query.findLocationOf(pickle).orElseThrow();

        testCaseMap.put("name", pickle.getName());
        testCaseMap.put("line", location.getLine());
        testCaseMap.put("type", "scenario");

        query.findScenarioBy(event).ifPresent(scenario -> {
            testCaseMap.put("id", query.findNameOf(pickle, NamingStrategy.strategy(NamingStrategy.Strategy.LONG).delimiter(";").namingVisitor(new IdNamingVisitor()).build()));
            testCaseMap.put("keyword", scenario.getKeyword());
            // TODO: Should not be nullable?
            testCaseMap.put("description", scenario.getDescription() != null ? scenario.getDescription() : "");
        });

        // TODO: Create steps
        testCaseMap.put("steps", testStepsFinished.stream()
                .map(this::createTestStep)
                .collect(toList()));

        if (!pickle.getTags().isEmpty()) {
            testCaseMap.put("tags", createTags(pickle));
        }
        return testCaseMap;
    }

    private static List<Map<String, Object>> createTags(Pickle pickle) {
        List<Map<String, Object>> tagList = new ArrayList<>();
        for (PickleTag tag : pickle.getTags()) {
            Map<String, Object> tagMap = new HashMap<>();
            tagMap.put("name", tag.getName());
            tagList.add(tagMap);
        }
        return tagList;
    }

    private JvmStep createTestStep(TestStepFinished testStepFinished) {
        TestStep testStep = query.findTestStepBy(testStepFinished).orElseThrow();
        return query.findPickleStepBy(testStep)
                .map(pickleStep -> {
                    Step step = query.findStepBy(pickleStep).orElseThrow();
                    //String keyword, Long line, JvmMatch match, String name, JvmResult result, JvmDocString doc_string, List<JvmDataTableRow> rows
                    return new JvmStep(
                            step.getKeyword(),
                            step.getLocation().getLine(),
                            createMatchMap(testStep, testStepFinished.getTestStepResult()),
                            step.getText(),
                            createResultMap(testStepFinished.getTestStepResult()),
                            step.getDocString().map(this::createDocStringMap).orElse(null),
                            step.getDataTable().map(this::createDataTableList).orElse(null)
                    );
                }).get();
        // TODO: Hook steps
    }

    private Map<String, Object> createMatchMap(TestStep step, TestStepResult result) {
        Map<String, Object> matchMap = new HashMap<>();

        step.getStepMatchArgumentsLists()
                .map(argumentsLists -> argumentsLists.stream()
                        .map(StepMatchArgumentsList::getStepMatchArguments)
                        .flatMap(Collection::stream)
                        .map(argument -> {
                            Map<String, Object> argumentMap = new HashMap<>();
                            Group group = argument.getGroup();
                            group.getValue().ifPresent(value -> argumentMap.put("val", value));
                            group.getStart().ifPresent(offset -> argumentMap.put("offset", offset));
                            return argumentMap;
                        }).collect(toList()))
                .filter(maps -> !maps.isEmpty())
                .ifPresent(argumentList -> matchMap.put("arguments", argumentList));

        if (result.getStatus() != TestStepResultStatus.UNDEFINED) {
            Optional<SourceReference> source = query.findStepDefinitionBy(step)
                    .stream()
                    .findFirst()
                    .map(StepDefinition::getSourceReference);
            source
                    .ifPresent(sourceReference -> {
                        sourceReference.getUri()
                                .map(uri -> renderLocationString(sourceReference, uri))
                                .ifPresent(location -> matchMap.put("location", location));
                        sourceReference.getJavaMethod()
                                .map(JsonReportWriter::renderLocationString)
                                .ifPresent(location -> matchMap.put("location", location));
                        sourceReference.getJavaStackTraceElement()
                                .map(javaStackTraceElement -> renderLocationString(sourceReference, javaStackTraceElement))
                                .ifPresent(location -> matchMap.put("location", location));
                    });
        }
        return matchMap;
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

    private Map<String, Object> createResultMap(TestStepResult result) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("status", result.getStatus().name().toLowerCase(ROOT));
        result.getException().ifPresent(exception -> {
            resultMap.put("error_message", exception.getStackTrace());
        });

        Duration duration = Convertor.toDuration(result.getDuration());
        if (!duration.isZero()) {
            resultMap.put("duration", duration.toNanos());
        }
        return resultMap;
    }

    private JvmDocString createDocStringMap(DocString docString) {
        Map<String, Object> docStringMap = new HashMap<>();
        docStringMap.put("value", docString.getContent());
        docStringMap.put("line", docString.getLocation().getLine());
        docStringMap.put("content_type", docString.getMediaType());
        return docStringMap;
    }

    private List<JvmDataTableRow> createDataTableList(DataTable argument) {
        List<Map<String, List<String>>> rowList = new ArrayList<>();
        for (TableRow row : argument.getRows()) {
            Map<String, List<String>> rowMap = new HashMap<>();
            rowMap.put("cells", row.getCells().stream().map(TableCell::getValue).collect(toList()));
            rowList.add(rowMap);
        }
        return rowList;
    }

    private String getDateTimeFromTimeStamp(Timestamp instant) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                .withZone(ZoneOffset.UTC);
        return formatter.format(Convertor.toInstant(instant));
    }

    static class JvmFeature {
        private final String uri;
        private final String id;
        private final Long line;
        private final String keyword;
        private final String name;
        private final String description;
        private final List<JvmElement> elements;
        private final List<JvmLocationTag> tags;

        JvmFeature(String uri, String id, Long line, String keyword, String name, String description, List<JvmElement> elements, List<JvmLocationTag> tags) {
            this.uri = requireNonNull(uri);
            this.id = requireNonNull(id);
            this.line = requireNonNull(line);
            this.keyword = requireNonNull(keyword);
            this.name = requireNonNull(name);
            this.description = requireNonNull(description);
            this.elements = requireNonNull(elements);
            this.tags = tags;
        }

        public String getUri() {
            return uri;
        }

        public String getId() {
            return id;
        }

        public Long getLine() {
            return line;
        }

        public String getKeyword() {
            return keyword;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public List<JvmElement> getElements() {
            return elements;
        }

        public List<JvmLocationTag> getTags() {
            return tags;
        }
    }

    enum JvmElementType {
        background, scenario
    }

    static class JvmElement {
        private final String start_timestamp;
        private final Long line;
        private final String id;
        private final JvmElementType type;
        private final String keyword;
        private final String name;
        private final String description;
        private final List<JvmStep> steps;
        private final List<JvmHook> before;
        private final List<JvmHook> after;
        private final List<JvmTag> tags;

        JvmElement(String start_timestamp, Long line, String id, JvmElementType type, String keyword, String name, String description, List<JvmStep> steps, List<JvmHook> before, List<JvmHook> after, List<JvmTag> tags) {
            this.start_timestamp = start_timestamp;
            this.line = requireNonNull(line);
            this.id = id;
            this.type = requireNonNull(type);
            this.keyword = requireNonNull(keyword);
            this.name = requireNonNull(name);
            this.description = requireNonNull(description);
            this.steps = requireNonNull(steps);
            this.before = before;
            this.after = after;
            this.tags = tags;
        }

        public String getStart_timestamp() {
            return start_timestamp;
        }

        public Long getLine() {
            return line;
        }

        public String getId() {
            return id;
        }

        public JvmElementType getType() {
            return type;
        }

        public String getKeyword() {
            return keyword;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public List<JvmStep> getSteps() {
            return steps;
        }

        public List<JvmHook> getBefore() {
            return before;
        }

        public List<JvmHook> getAfter() {
            return after;
        }

        public List<JvmTag> getTags() {
            return tags;
        }
    }

    static class JvmStep {
        private final String keyword;
        private final Long line;
        private final JvmMatch match;
        private final String name;
        private final JvmResult result;
        private final JvmDocString doc_string;
        private final List<JvmDataTableRow> rows;

        JvmStep(String keyword, Long line, JvmMatch match, String name, JvmResult result, JvmDocString doc_string, List<JvmDataTableRow> rows) {
            this.keyword = requireNonNull(keyword);
            this.line = requireNonNull(line);
            this.match = match;
            this.name = requireNonNull(name);
            this.result = requireNonNull(result);
            this.doc_string = doc_string;
            this.rows = rows;
        }

        public String getKeyword() {
            return keyword;
        }

        public Long getLine() {
            return line;
        }

        public JvmMatch getMatch() {
            return match;
        }

        public String getName() {
            return name;
        }

        public JvmResult getResult() {
            return result;
        }

        public JvmDocString getDoc_string() {
            return doc_string;
        }

        public List<JvmDataTableRow> getRows() {
            return rows;
        }
    }

    static class JvmMatch {
        private final String location;
        private final List<JvmArgument> arguments;

        JvmMatch(String location, List<JvmArgument> arguments) {
            this.location = location;
            this.arguments = arguments;
        }

        public String getLocation() {
            return location;
        }

        public List<JvmArgument> getArguments() {
            return arguments;
        }
    }

    static class JvmArgument {
        private final String val;
        private final Number offset;

        JvmArgument(String val, Number offset) {
            this.val = requireNonNull(val);
            this.offset = requireNonNull(offset);
        }

        public String getVal() {
            return val;
        }

        public Number getOffset() {
            return offset;
        }
    }

    static class JvmResult {
        private final Integer duration;
        private final JvmStatus status;
        private final String error_message;

        JvmResult(Integer duration, JvmStatus status, String error_message) {
            this.duration = duration;
            this.status = requireNonNull(status);
            this.error_message = error_message;
        }

        public Integer getDuration() {
            return duration;
        }

        public JvmStatus getStatus() {
            return status;
        }

        public String getError_message() {
            return error_message;
        }
    }

    enum JvmStatus {
        passed,
        failed,
        skipped,
        undefined,
        pending
    }


    static class JvmDocString {
        private final Long line;
        private final String value;
        private final String content_type;

        JvmDocString(Long line, String value, String content_type) {
            this.line = requireNonNull(line);
            this.value = requireNonNull(value);
            this.content_type = content_type;
        }

        public Long getLine() {
            return line;
        }

        public String getValue() {
            return value;
        }

        public String getContent_type() {
            return content_type;
        }
    }

    static class JvmDataTableRow {
        private final List<String> cells;

        JvmDataTableRow(List<String> cells) {
            this.cells = requireNonNull(cells);
        }

        public List<String> getCells() {
            return cells;
        }
    }

    static class JvmHook {
        private final JvmMatch match;
        private final JvmResult result;

        JvmHook(JvmMatch match, JvmResult result) {
            this.match = requireNonNull(match);
            this.result = requireNonNull(result);
        }

        public JvmMatch getMatch() {
            return match;
        }

        public JvmResult getResult() {
            return result;
        }
    }

    static class JvmTag {
        private final String name;

        JvmTag(String name) {
            this.name = requireNonNull(name);
        }

        public String getName() {
            return name;
        }
    }

    static class JvmLocationTag {
        private final String name;
        private final String type;
        private final JvmLocation location;

        JvmLocationTag(String name, String type, JvmLocation location) {
            this.name = requireNonNull(name);
            this.type = requireNonNull(type);
            this.location = requireNonNull(location);
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public JvmLocation getLocation() {
            return location;
        }
    }

    static class JvmLocation {
        private final Long line;
        private final Long column;

        JvmLocation(Long line, Long column) {
            this.line = requireNonNull(line);
            this.column = requireNonNull(column);
        }

        public Long getLine() {
            return line;
        }

        public Long getColumn() {
            return column;
        }
    }


}
