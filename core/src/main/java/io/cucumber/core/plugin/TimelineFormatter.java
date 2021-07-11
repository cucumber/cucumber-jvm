package io.cucumber.core.plugin;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.messages.JSON;
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
    private final Map<Long, GroupData> allGroups = new HashMap<>();
    private final File reportDir;
    private final UTF8OutputStreamWriter reportJs;
    private final Map<URI, Collection<Node>> parsedTestSources = new HashMap<>();

    @SuppressWarnings("unused") // Used by PluginFactory
    public TimelineFormatter(final File reportDir) throws FileNotFoundException {
        reportDir.mkdirs();
        if (!reportDir.isDirectory()) {
            throw new CucumberException(String.format("The %s needs an existing directory. Not a directory: %s",
                getClass().getName(), reportDir.getAbsolutePath()));
        }

        this.reportDir = reportDir;
        this.reportJs = new UTF8OutputStreamWriter(new FileOutputStream(new File(reportDir, "report.js")));
    }

    @Override
    public void setEventPublisher(final EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceParsed.class, this::handleTestSourceParsed);
        publisher.registerHandlerFor(TestCaseStarted.class, this::handleTestCaseStarted);
        publisher.registerHandlerFor(TestCaseFinished.class, this::handleTestCaseFinished);
        publisher.registerHandlerFor(TestRunFinished.class, this::finishReport);
    }

    private void handleTestSourceParsed(TestSourceParsed event) {
        parsedTestSources.put(event.getUri(), event.getNodes());
    }

    private void handleTestCaseStarted(final TestCaseStarted event) {
        Thread currentThread = Thread.currentThread();
        final Long threadId = currentThread.getId();
        final TestData test = new TestData(event, threadId);
        allTests.put(getId(event), test);
        if (!allGroups.containsKey(threadId)) {
            allGroups.put(threadId, new GroupData(currentThread));
        }
    }

    private void handleTestCaseFinished(final TestCaseFinished event) {
        final String id = getId(event);
        allTests.get(id).end(event);
    }

    private void finishReport(final TestRunFinished event) {
        try {
            reportJs.append("$(document).ready(function() {");
            reportJs.append("\n");
            appendAsJsonToJs(reportJs, "timelineItems", allTests.values());
            reportJs.append("\n");
            // Need to sort groups by id, so can guarantee output of order in
            // rendered timeline
            appendAsJsonToJs(reportJs, "timelineGroups", new TreeMap<>(allGroups).values());
            reportJs.append("\n");
            reportJs.append("});");
            reportJs.close();
            copyReportFiles();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getId(final TestCaseEvent testCaseEvent) {
        return testCaseEvent.getTestCase().getId().toString();
    }

    private void appendAsJsonToJs(
            final UTF8OutputStreamWriter out, final String pushTo, final Collection<?> content
    ) throws IOException {
        out.append("CucumberHTML.").append(pushTo).append(".pushArray(");
        JSON.writeValue(out, content);
        out.append(");");
    }

    private void copyReportFiles() {
        if (reportDir == null) {
            return;
        }
        final File outputDir = new File(reportDir.getPath());
        for (String textAsset : TEXT_ASSETS) {
            final InputStream textAssetStream = getClass().getResourceAsStream(textAsset);
            if (textAssetStream == null) {
                throw new CucumberException("Couldn't find " + textAsset);
            }
            final String fileName = new File(textAsset).getName();
            copyFile(textAssetStream, new File(outputDir, fileName));
            closeQuietly(textAssetStream);
        }
    }

    private static void copyFile(final InputStream source, final File dest) throws CucumberException {
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

        final long id;
        final String content;

        GroupData(Thread thread) {
            id = thread.getId();
            content = thread.toString();
        }

    }

    class TestData {

        final String id;
        final String feature;
        final String scenario;
        final long startTime;
        final long group;
        final String content = ""; // Replaced in JS file
        final String tags;
        long end;
        String className;

        TestData(final TestCaseStarted started, final Long group) {
            this.id = TimelineFormatter.this.getId(started);
            final TestCase testCase = started.getTestCase();
            this.feature = findRootNodeName(testCase);
            this.scenario = testCase.getName();
            this.startTime = started.getInstant().toEpochMilli();
            this.group = group;
            this.tags = buildTagsValue(testCase);
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

        private String buildTagsValue(final TestCase testCase) {
            final StringBuilder tags = new StringBuilder();
            for (final String tag : testCase.getTags()) {
                tags.append(tag.toLowerCase()).append(",");
            }
            return tags.toString();
        }

        void end(final TestCaseFinished event) {
            this.end = event.getInstant().toEpochMilli();
            this.className = event.getResult().getStatus().name().toLowerCase(ROOT);
        }
    }

}
