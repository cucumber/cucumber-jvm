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

import java.io.BufferedWriter;
import java.io.Closeable;
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
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonInclude.Value.construct;
import static io.cucumber.query.Repository.RepositoryFeature.INCLUDE_GHERKIN_DOCUMENTS;
import static java.util.Locale.ROOT;

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

    @SuppressWarnings({ "unused", "RedundantThrows" }) // Used by PluginFactory
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
            writeTimeLineReport();
        }
    }

    private void writeTimeLineReport() {
        Map<String, GroupData> threadGroups = new HashMap<>();
        List<TestData> testData = query.findAllTestCaseFinished().stream()
                .map(handleTestCaseFinished(threadGroups))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        writeReport(threadGroups, testData);
    }

    private Function<TestCaseFinished, Optional<TestData>> handleTestCaseFinished(
            Map<String, GroupData> threadGroupsAccumulator
    ) {
        return testCaseFinished -> query.findTestCaseStartedBy(testCaseFinished)
                .map(testCaseStarted -> createTestData(testCaseFinished, testCaseStarted, threadGroupsAccumulator));

    }

    private final AtomicInteger nextGroupId = new AtomicInteger();

    private TestData createTestData(
            TestCaseFinished testCaseFinished, TestCaseStarted testCaseStarted,
            Map<String, GroupData> threadGroupsAccumulator
    ) {
        String workerId = testCaseStarted.getWorkerId().orElse("");
        GroupData groupData = threadGroupsAccumulator.computeIfAbsent(workerId, threadId -> {
            GroupData group = new GroupData();
            group.setContent(workerId);
            group.setId(nextGroupId.incrementAndGet());
            return group;
        });
        return createTestData(testCaseFinished, testCaseStarted, groupData);
    }

    private TestData createTestData(
            TestCaseFinished testCaseFinished, TestCaseStarted testCaseStarted, GroupData groupData
    ) {
        TestData data = new TestData();
        data.setId(testCaseStarted.getTestCaseId());
        data.setFeature(getFeatureName(testCaseStarted));
        data.setScenario(getPickleName(testCaseStarted));
        data.setStart(Convertor.toInstant(testCaseStarted.getTimestamp()).toEpochMilli());
        data.setTags(getTagsValue(testCaseStarted));
        data.setGroup(groupData.getId());
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
                        tags.append(tag.getName().toLowerCase()).append(",");
                    }
                    return tags.toString();
                }).orElse("");
    }

    private void writeReport(Map<String, GroupData> threadGroups, List<TestData> allTests) {
        writeReportJs(threadGroups, allTests);
        copyReportFiles();
    }

    private void writeReportJs(Map<String, GroupData> threadGroups, List<TestData> allTests) {
        File reportJsFile = new File(reportDir, "report.js");
        try (BufferedWriter reportJs = Files.newBufferedWriter(reportJsFile.toPath(), StandardCharsets.UTF_8)) {
            reportJs.append("$(document).ready(function() {");
            reportJs.append("\n");
            appendAsJsonToJs(reportJs, "timelineItems", allTests);
            reportJs.append("\n");
            // Need to sort groups by id, so can guarantee output of order in
            // rendered timeline
            appendAsJsonToJs(reportJs, "timelineGroups", new TreeMap<>(threadGroups).values());
            reportJs.append("\n");
            reportJs.append("});");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void appendAsJsonToJs(
            BufferedWriter out, String pushTo, Collection<?> content
    ) throws IOException {
        out.append("CucumberHTML.").append(pushTo).append(".pushArray(");
        objectMapper.writeValue(out, content);
        out.append(");");
    }

    private void copyReportFiles() {
        if (reportDir == null) {
            return;
        }
        File outputDir = new File(reportDir.getPath());
        for (String textAsset : TEXT_ASSETS) {
            InputStream textAssetStream = getClass().getResourceAsStream(textAsset);
            if (textAssetStream == null) {
                throw new CucumberException("Couldn't find " + textAsset);
            }
            String fileName = new File(textAsset).getName();
            copyFile(textAssetStream, new File(outputDir, fileName));
            closeQuietly(textAssetStream);
        }
    }

    private static void copyFile(InputStream source, File dest) throws CucumberException {
        OutputStream os = null;
        try {
            os = Files.newOutputStream(dest.toPath());
            byte[] buffer = new byte[1024];
            int length;
            while ((length = source.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (IOException e) {
            throw new CucumberException("Unable to write to report file item: ", e);
        } finally {
            closeQuietly(os);
        }
    }

    private static void closeQuietly(Closeable out) {
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException ignored) {
            // go gentle into that good night
        }
    }

    static class GroupData {

        private long id;
        private String content;

        public void setId(long id) {
            this.id = id;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public long getId() {
            return id;
        }

        public String getContent() {
            return content;
        }

    }

    static class TestData {

        private String id;
        private String feature;
        private String scenario;
        private long start;
        private long group;
        private String content = ""; // Replaced in JS file
        private String tags;
        private long end;
        private String className;

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

        public void setGroup(long group) {
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

        public String getId() {
            return id;
        }

        public String getFeature() {
            return feature;
        }

        public String getScenario() {
            return scenario;
        }

        public long getStart() {
            return start;
        }

        public long getGroup() {
            return group;
        }

        public String getContent() {
            return content;
        }

        public String getTags() {
            return tags;
        }

        public long getEnd() {
            return end;
        }

        public String getClassName() {
            return className;
        }

    }

}
