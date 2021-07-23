package io.cucumber.core.plugin;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.messages.internal.com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.cucumber.messages.internal.com.fasterxml.jackson.core.JsonGenerator.Feature;
import io.cucumber.messages.internal.com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.messages.internal.com.fasterxml.jackson.databind.SerializationFeature;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseEvent;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestSourceParsed;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Predicate;

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

    private final Map<String, TestData> allTests = new HashMap<>();
    private final Map<Long, GroupData> threadGroups = new HashMap<>();

    private final File reportDir;
    private final UTF8OutputStreamWriter reportJs;
    private final Map<URI, Collection<Node>> parsedTestSources = new HashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(Include.NON_NULL)
            .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
            .disable(Feature.AUTO_CLOSE_TARGET);

    @SuppressWarnings("unused") // Used by PluginFactory
    public TimelineFormatter(File reportDir) throws FileNotFoundException {
        reportDir.mkdirs();
        if (!reportDir.isDirectory()) {
            throw new CucumberException(String.format("The %s needs an existing directory. Not a directory: %s",
                    getClass().getName(), reportDir.getAbsolutePath()));
        }

        this.reportDir = reportDir;
        this.reportJs = new UTF8OutputStreamWriter(new FileOutputStream(new File(reportDir, "report.js")));
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceParsed.class, this::handleTestSourceParsed);
        publisher.registerHandlerFor(TestCaseStarted.class, this::handleTestCaseStarted);
        publisher.registerHandlerFor(TestCaseFinished.class, this::handleTestCaseFinished);
        publisher.registerHandlerFor(TestRunFinished.class, this::finishReport);
    }

    private void handleTestSourceParsed(TestSourceParsed event) {
        parsedTestSources.put(event.getUri(), event.getNodes());
    }

    private void handleTestCaseStarted(TestCaseStarted event) {
        Thread thread = Thread.currentThread();
        threadGroups.computeIfAbsent(thread.getId(), threadId -> {
            GroupData group = new GroupData();
            group.setContent(thread.toString());
            group.setId(threadId);
            return group;
        });

        TestCase testCase = event.getTestCase();
        TestData data = new TestData();
        data.setId(getId(event));
        data.setFeature(findRootNodeName(testCase));
        data.setScenario(testCase.getName());
        data.setStart(event.getInstant().toEpochMilli());
        data.setTags(buildTagsValue(testCase));
        data.setGroup(thread.getId());
        allTests.put(data.getId(), data);
    }

    private String buildTagsValue(TestCase testCase) {
        StringBuilder tags = new StringBuilder();
        for (String tag : testCase.getTags()) {
            tags.append(tag.toLowerCase()).append(",");
        }
        return tags.toString();
    }

    private void handleTestCaseFinished(TestCaseFinished event) {
        TestData data = allTests.get(getId(event));
        data.setEnd(event.getInstant().toEpochMilli());
        data.setClassName(event.getResult().getStatus().name().toLowerCase(ROOT));
    }

    private String findRootNodeName(TestCase testCase) {
        Location location = testCase.getLocation();
        Predicate<Node> withLocation = candidate -> candidate.getLocation().equals(location);
        return parsedTestSources.get(testCase.getUri())
                .stream()
                .map(node -> node.findPathTo(withLocation))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .map(nodes -> nodes.get(0))
                .flatMap(Node::getName)
                .orElse("Unknown");
    }

    private void finishReport(TestRunFinished event) {

        try {
            reportJs.append("$(document).ready(function() {");
            reportJs.append("\n");
            appendAsJsonToJs(reportJs, "timelineItems", allTests.values());
            reportJs.append("\n");
            // Need to sort groups by id, so can guarantee output of order in
            // rendered timeline
            appendAsJsonToJs(reportJs, "timelineGroups", new TreeMap<>(threadGroups).values());
            reportJs.append("\n");
            reportJs.append("});");
            reportJs.close();
            copyReportFiles();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getId(TestCaseEvent testCaseEvent) {
        return testCaseEvent.getTestCase().getId().toString();
    }

    private void appendAsJsonToJs(
            UTF8OutputStreamWriter out, String pushTo, Collection<?> content
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
            os = new FileOutputStream(dest);
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
