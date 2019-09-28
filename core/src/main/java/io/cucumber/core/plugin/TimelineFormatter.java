package io.cucumber.core.plugin;

import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;
import gherkin.deps.com.google.gson.annotations.SerializedName;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseEvent;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestSourceRead;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.ROOT;

public final class TimelineFormatter implements ConcurrentEventListener {

    private static final String[] TEXT_ASSETS = new String[]{
        "/io/cucumber/core/plugin/timeline/index.html",
        "/io/cucumber/core/plugin/timeline/formatter.js",
        "/io/cucumber/core/plugin/timeline/report.css",
        "/io/cucumber/core/plugin/timeline/jquery-3.4.1.min.js",
        "/io/cucumber/core/plugin/timeline/vis.min.css",
        "/io/cucumber/core/plugin/timeline/vis.min.js",
        "/io/cucumber/core/plugin/timeline/vis.override.css",
        "/io/cucumber/core/plugin/timeline/chosen.jquery.min.js",
        "/io/cucumber/core/plugin/timeline/chosen.min.css",
        "/io/cucumber/core/plugin/timeline/chosen.override.css",
        "/io/cucumber/core/plugin/timeline/chosen-sprite.png"
    };

    private final TestSourcesModel testSources = new TestSourcesModel();
    private final Map<String, TestData> allTests = new HashMap<>();
    private final Map<Long, GroupData> allGroups = new HashMap<>();
    private final URL reportDir;
    private final NiceAppendable reportJs;

    @SuppressWarnings("unused") // Used by PluginFactory
    public TimelineFormatter(final URL reportDir) {
        this(reportDir, createOutput(reportDir, "report.js"));
    }

    private TimelineFormatter(final URL reportDir, final NiceAppendable reportJs) {
        this.reportDir = reportDir;
        this.reportJs = reportJs;
    }

    @Override
    public void setEventPublisher(final EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, this::handleTestSourceRead);
        publisher.registerHandlerFor(TestCaseStarted.class, this::handleTestCaseStarted);
        publisher.registerHandlerFor(TestCaseFinished.class, this::handleTestCaseFinished);
        publisher.registerHandlerFor(TestRunFinished.class, this::finishReport);
    }

    private void handleTestSourceRead(TestSourceRead event) {
        testSources.addTestSourceReadEvent(event.getUri(), event);
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
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        reportJs.append("$(document).ready(function() {");
        reportJs.println();
        appendAsJsonToJs(gson, reportJs, "timelineItems", allTests.values());
        reportJs.println();
        //Need to sort groups by id, so can guarantee output of order in rendered timeline
        appendAsJsonToJs(gson, reportJs, "timelineGroups", new TreeMap<>(allGroups).values());
        reportJs.println();
        reportJs.append("});");
        reportJs.close();
        copyReportFiles();

        // TODO: Enable this warning when cucumber-html-formatter is ready to be used
//        System.err.println("" +
//            "\n" +
//            "****************************************\n" +
//            "* WARNING: The timeline formatter will *\n" +
//            "* be removed in cucumber-jvm 6.0.0 and *\n" +
//            "* be replaced by the standalone        *\n" +
//            "* cucumber-html-formatter.             *\n" +
//            "****************************************\n");
    }

    private void appendAsJsonToJs(final Gson gson, final NiceAppendable out, final String pushTo, final Collection<?> content) {
        out.append("CucumberHTML.").append(pushTo).append(".pushArray(");
        gson.toJson(content, out);
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

    private static NiceAppendable createOutput(final URL dir, final String file) {
        final File outDir = new File(dir.getPath());
        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new CucumberException("Failed to create dir: " + dir.getPath());
        }
        try {
            final OutputStream out = new URLOutputStream(new URL(dir, file));
            return new NiceAppendable(new OutputStreamWriter(out, UTF_8));
        } catch (IOException e) {
            throw new CucumberException(e);
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

    private String getId(final TestCaseEvent testCaseEvent) {
        final TestCase testCase = testCaseEvent.getTestCase();
        final URI uri = testCase.getUri();
        final TestSourcesModel.AstNode astNode = testSources.getAstNode(uri, testCase.getLine());
        return TestSourcesModel.calculateId(astNode);
    }

    class TestData {
        @SerializedName("id")
        final String id;
        @SerializedName("feature")
        final String feature;
        @SerializedName("scenario")
        final String scenario;
        @SerializedName("start")
        final long startTime;
        @SerializedName("end")
        long endTime;
        @SerializedName("group")
        final long threadId;
        @SerializedName("content")
        final String content = ""; //Replaced in JS file
        @SerializedName("className")
        String className;
        @SerializedName("tags")
        final String tags;

        TestData(final TestCaseStarted started, final Long threadId) {
            this.id = getId(started);
            final TestCase testCase = started.getTestCase();
            final URI uri = testCase.getUri();
            this.feature = TimelineFormatter.this.testSources.getFeatureName(uri);
            this.scenario = testCase.getName();
            this.startTime = started.getInstant().toEpochMilli();
            this.threadId = threadId;
            this.tags = buildTagsValue(testCase);
        }

        private String buildTagsValue(final TestCase testCase) {
            final StringBuilder tags = new StringBuilder();
            for (final String tag : testCase.getTags()) {
                tags.append(tag.toLowerCase()).append(",");
            }
            return tags.toString();
        }

        void end(final TestCaseFinished event) {
            this.endTime = event.getInstant().toEpochMilli();
            this.className = event.getResult().getStatus().name().toLowerCase(ROOT);
        }
    }

    static class GroupData {
        @SerializedName("id")
        final long id;
        @SerializedName("content")
        final String content;

        GroupData(Thread thread) {
            id = thread.getId();
            content = thread.toString();
        }
    }

}
