package io.cucumber.core.plugin;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.messages.Convertor;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.PickleTag;
import io.cucumber.messages.types.TestCaseFinished;
import io.cucumber.messages.types.TestCaseStarted;
import io.cucumber.messages.types.TestStepResult;
import io.cucumber.messages.types.TestStepResultStatus;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.query.Lineage;
import io.cucumber.query.Query;
import io.cucumber.query.Repository;
import org.jspecify.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonInclude.Value.construct;
import static io.cucumber.query.Repository.RepositoryFeature.INCLUDE_GHERKIN_DOCUMENTS;
import static java.util.Comparator.comparing;
import static java.util.Locale.ROOT;

/**
 * Writes a timeline of scenario execution.
 * <p>
 * Note: The report is only written once the test run is finished.
 */
public final class TimelineFormatter implements ConcurrentEventListener {

    private static final String[] TEXT_ASSETS = new String[] {
            "/io/cucumber/core/plugin/timeline/index.html",
            "/io/cucumber/core/plugin/timeline/formatter.js",
            "/io/cucumber/core/plugin/timeline/report.css",
            "/io/cucumber/core/plugin/timeline/jquery-3.5.1.min.js",
            "/io/cucumber/core/plugin/timeline/vis-timeline-graph2d.min.css",
            "/io/cucumber/core/plugin/timeline/vis-timeline-graph2d.min.js",
            "/io/cucumber/core/plugin/timeline/vis-timeline-graph2d.override.css",
            "/io/cucumber/core/plugin/timeline/chosen.jquery.min.js",
            "/io/cucumber/core/plugin/timeline/chosen.min.css",
            "/io/cucumber/core/plugin/timeline/chosen.override.css",
            "/io/cucumber/core/plugin/timeline/chosen-sprite.png"
    };

    private final Repository repository = Repository.builder()
            .feature(INCLUDE_GHERKIN_DOCUMENTS, true)
            .build();
    private final Query query = new Query(repository);

    private final File reportDir;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .setDefaultPropertyInclusion(construct(Include.NON_ABSENT, Include.NON_ABSENT))
            .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
            .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);

    // Used by PluginFactory
    @SuppressWarnings({ "unused", "RedundantThrows", "ResultOfMethodCallIgnored" })
    public TimelineFormatter(File reportDir) throws FileNotFoundException {
        reportDir.mkdirs();
        if (!reportDir.isDirectory()) {
            throw new CucumberException(String.format("The %s needs an existing directory. Not a directory: %s",
                getClass().getName(), reportDir.getAbsolutePath()));
        }
        this.reportDir = reportDir;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(Envelope.class, this::write);
    }

    private void write(Envelope event) {
        repository.update(event);

        // TODO: Plugins should implement the closable interface
        // and be closed by Cucumber
        if (event.getTestRunFinished().isPresent()) {
            try {
                writeTimeLineReport();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private void writeTimeLineReport() throws IOException {
        Map<String, TimeLineGroup> timeLineGroupsById = new HashMap<>();
        List<TimeLineItem> timeLineItems = query.findAllTestCaseFinished().stream()
                .map(testCaseFinished -> query.findTestCaseStartedBy(testCaseFinished)
                        .map(testCaseStarted -> createTestData(
                            testCaseFinished, //
                            testCaseStarted, //
                            workerId -> timeLineGroupsById.computeIfAbsent(workerId, id -> new TimeLineGroup(id, id)) //
                        )))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        List<TimeLineGroup> timeLineGroups = timeLineGroupsById.values().stream()
                .sorted(comparing(TimeLineGroup::getId))
                .collect(Collectors.toList());

        writeTimeLineReport(timeLineGroups, timeLineItems);
    }

    private TimeLineItem createTestData(
            TestCaseFinished testCaseFinished, TestCaseStarted testCaseStarted,
            Function<String, TimeLineGroup> timeLineGroupCreator
    ) {
        String workerId = testCaseStarted.getWorkerId().orElse("");
        TimeLineGroup timeLineGroup = timeLineGroupCreator.apply(workerId);
        return createTestData(testCaseFinished, testCaseStarted, timeLineGroup);
    }

    private TimeLineItem createTestData(
            TestCaseFinished testCaseFinished, TestCaseStarted testCaseStarted, TimeLineGroup timeLineGroup
    ) {
        TimeLineItem data = new TimeLineItem();
        data.setId(testCaseStarted.getTestCaseId());
        data.setFeature(getFeatureName(testCaseStarted));
        data.setScenario(getPickleName(testCaseStarted));
        data.setStart(Convertor.toInstant(testCaseStarted.getTimestamp()).toEpochMilli());
        data.setTags(getTagsValue(testCaseStarted));
        data.setGroup(timeLineGroup.getId());
        data.setEnd(Convertor.toInstant(testCaseFinished.getTimestamp()).toEpochMilli());
        data.setClassName(getTestStepStatusResult(testCaseFinished));
        return data;
    }

    private String getTestStepStatusResult(TestCaseFinished event) {
        return query.findMostSevereTestStepResultBy(event)
                .map(TestStepResult::getStatus)
                .map(TestStepResultStatus::value)
                .map(s -> s.toLowerCase(ROOT))
                // By definition
                .orElse("passed");
    }

    private String getPickleName(TestCaseStarted testCaseStarted) {
        return query.findPickleBy(testCaseStarted)
                .map(Pickle::getName)
                .orElse("");
    }

    private String getFeatureName(TestCaseStarted testCaseStarted) {
        return query.findLineageBy(testCaseStarted)
                .flatMap(Lineage::feature)
                .map(Feature::getName)
                .orElse("");
    }

    private String getTagsValue(TestCaseStarted testCaseStarted) {
        return query.findPickleBy(testCaseStarted)
                .map(pickle -> {
                    StringBuilder tags = new StringBuilder();
                    for (PickleTag tag : pickle.getTags()) {
                        tags.append(tag.getName().toLowerCase(Locale.US)).append(",");
                    }
                    return tags.toString();
                }).orElse("");
    }

    private void writeTimeLineReport(List<TimeLineGroup> timeLineGroups, List<TimeLineItem> timeLineItems)
            throws IOException {
        writeReportJs(timeLineGroups, timeLineItems);
        copyReportFiles();
    }

    private void writeReportJs(List<TimeLineGroup> timeLineGroups, List<TimeLineItem> timeLineItems)
            throws IOException {
        File reportJsFile = new File(reportDir, "report.js");
        try (BufferedWriter reportJs = Files.newBufferedWriter(reportJsFile.toPath(), StandardCharsets.UTF_8)) {
            reportJs.append("$(document).ready(function() {");
            reportJs.append("\n");
            appendAsJsonToJs(reportJs, "timelineItems", timeLineItems);
            reportJs.append("\n");
            // Need to sort groups by id, so can guarantee output of order in
            // rendered timeline
            appendAsJsonToJs(reportJs, "timelineGroups", timeLineGroups);
            reportJs.append("\n");
            reportJs.append("});");
        }
    }

    private void appendAsJsonToJs(
            BufferedWriter out, String pushTo, Collection<?> content
    ) throws IOException {
        out.append("CucumberHTML.").append(pushTo).append(".pushArray(");
        objectMapper.writeValue(out, content);
        out.append(");");
    }

    private void copyReportFiles() throws IOException {
        File outputDir = new File(reportDir.getPath());
        for (String textAsset : TEXT_ASSETS) {
            try (InputStream textAssetStream = getClass().getResourceAsStream(textAsset)) {
                if (textAssetStream == null) {
                    throw new CucumberException("Couldn't find " + textAsset);
                }
                String fileName = new File(textAsset).getName();
                copyFile(textAssetStream, new File(outputDir, fileName));
            }
        }
    }

    private static void copyFile(InputStream source, File dest) throws IOException {
        try (OutputStream os = Files.newOutputStream(dest.toPath())) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = source.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }

    public static final class TimeLineGroup {

        private @Nullable String id;
        private @Nullable String content;

        @SuppressWarnings("RedundantModifier")
        public TimeLineGroup() {

        }

        @SuppressWarnings("RedundantModifier")
        public TimeLineGroup(String id, String content) {
            this.id = id;
            this.content = content;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setContent(@Nullable String content) {
            this.content = content;
        }

        public @Nullable String getId() {
            return id;
        }

        public @Nullable String getContent() {
            return content;
        }

    }

    public static final class TimeLineItem {

        private @Nullable String id;
        private @Nullable String feature;
        private @Nullable String scenario;
        private long start;
        private @Nullable String group;
        // Replaced in JS file
        private String content = "";
        private @Nullable String tags;
        private long end;
        private @Nullable String className;

        @SuppressWarnings("RedundantModifier")
        public TimeLineItem() {
            /* no-op */
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setFeature(String feature) {
            this.feature = feature;
        }

        public void setScenario(String scenario) {
            this.scenario = scenario;
        }

        public void setStart(long start) {
            this.start = start;
        }

        public void setGroup(@Nullable String group) {
            this.group = group;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public void setTags(String tags) {
            this.tags = tags;
        }

        public void setEnd(long end) {
            this.end = end;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public @Nullable String getId() {
            return id;
        }

        public @Nullable String getFeature() {
            return feature;
        }

        public @Nullable String getScenario() {
            return scenario;
        }

        public long getStart() {
            return start;
        }

        public @Nullable String getGroup() {
            return group;
        }

        public @Nullable String getContent() {
            return content;
        }

        public @Nullable String getTags() {
            return tags;
        }

        public long getEnd() {
            return end;
        }

        public @Nullable String getClassName() {
            return className;
        }

    }

}
