package cucumber.runtime.formatter;

import cucumber.api.TestCase;
import cucumber.api.event.ConcurrentEventListener;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestCaseEvent;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestCaseStarted;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestSourceRead;
import cucumber.api.formatter.NiceAppendable;
import cucumber.runtime.CucumberException;
import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;
import gherkin.deps.com.google.gson.annotations.SerializedName;
import gherkin.pickles.PickleTag;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class TimelineFormatter implements ConcurrentEventListener {

    //TODO: if accepted then should move resources out into own project as per HTML report
    private static final String[] TEXT_ASSETS = new String[]{
        "/io/cucumber/formatter/timeline/index.html",
        "/io/cucumber/formatter/timeline/formatter.js",
        "/io/cucumber/formatter/timeline/report.css",
        "/io/cucumber/formatter/timeline/jquery-3.3.1.min.js",
        "/io/cucumber/formatter/timeline/vis.min.css",
        "/io/cucumber/formatter/timeline/vis.min.js",
        "/io/cucumber/formatter/timeline/vis.override.css",
        "/io/cucumber/formatter/timeline/chosen.jquery.min.js",
        "/io/cucumber/formatter/timeline/chosen.min.css",
        "/io/cucumber/formatter/timeline/chosen.override.css",
        "/io/cucumber/formatter/timeline/chosen-sprite.png"
    };

    private final EventHandler<TestSourceRead> testSourceReadHandler = new EventHandler<TestSourceRead>() {
        @Override
        public void receive(TestSourceRead event) {
            testSources.addTestSourceReadEvent(event.uri, event);
        }
    };
    private final EventHandler<TestCaseStarted> caseStartedHandler = new EventHandler<TestCaseStarted>() {
        @Override
        public void receive(TestCaseStarted event) {
            handleTestCaseStarted(event);
        }
    };
    private final EventHandler<TestCaseFinished> caseFinishedHandler = new EventHandler<TestCaseFinished>() {
        @Override
        public void receive(final TestCaseFinished event) {
            handleTestCaseFinished(event);
        }
    };
    private final EventHandler<TestRunFinished> runFinishedHandler = new EventHandler<TestRunFinished>() {
        @Override
        public void receive(final TestRunFinished event) {
            finishReport(event);
        }
    };

    private final TestSourcesModel testSources = new TestSourcesModel();
    private final Map<String, TestData> allTests = new HashMap<>();
    private final Map<Long, GroupData> allGroups = new HashMap<>();
    private final URL reportDir;
    private final NiceAppendable reportJs;

    @SuppressWarnings("WeakerAccess") // Used by PluginFactory
    public TimelineFormatter(final URL reportDir) {
        this(reportDir, createJsonOut(reportDir, "report.js"));
    }

    private TimelineFormatter(final URL reportDir, final NiceAppendable reportJs) {
        this.reportDir = reportDir;
        this.reportJs = reportJs;
    }

    @Override
    public void setEventPublisher(final EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, testSourceReadHandler);
        publisher.registerHandlerFor(TestCaseStarted.class, caseStartedHandler);
        publisher.registerHandlerFor(TestCaseFinished.class, caseFinishedHandler);
        publisher.registerHandlerFor(TestRunFinished.class, runFinishedHandler);
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
    }

    private void appendAsJsonToJs(final Gson gson, final NiceAppendable out, final String pushTo, final Collection<?> content) {
        out.append("CucumberHTML." + pushTo + ".pushArray(");
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

    private static NiceAppendable createJsonOut(final URL dir, final String file) {
        final File outDir = new File(dir.getPath());
        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new CucumberException("Failed to create dir: " + dir.getPath());
        }
        try {
            final OutputStream out = new URLOutputStream(new URL(dir, file));
            return new NiceAppendable(new OutputStreamWriter(out, "UTF-8"));
        }
        catch (IOException e) {
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
        }
        catch (IOException e) {
            throw new CucumberException("Unable to write to report file item: ", e);
        }
        finally {
            closeQuietly(os);
        }
    }

    private static void closeQuietly(Closeable out) {
        try {
            if (out != null) {
                out.close();
            }
        }
        catch (IOException ignored) {
            // go gentle into that good night
        }
    }

    private String getId(final TestCaseEvent testCaseEvent) {
        final TestCase testCase = testCaseEvent.getTestCase();
        final String uri = testCase.getUri();
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
            final String uri = testCase.getUri();
            this.feature = TimelineFormatter.this.testSources.getFeatureName(uri);
            this.scenario = testCase.getName();
            this.startTime = started.getTimeStampMillis();
            this.threadId = threadId;
            this.tags = buildTagsValue(testCase);
        }

        private String buildTagsValue(final TestCase testCase) {
            final StringBuilder tags = new StringBuilder();
            for (final PickleTag tag : testCase.getTags()) {
                tags.append(tag.getName().toLowerCase()).append(",");
            }
            return tags.toString();
        }

        public void end(final TestCaseFinished event) {
            this.endTime = event.getTimeStampMillis();
            this.className = event.result.getStatus().lowerCaseName();
        }
    }

    class GroupData {
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
