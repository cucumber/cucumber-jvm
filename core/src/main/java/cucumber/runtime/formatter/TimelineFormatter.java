package cucumber.runtime.formatter;

import cucumber.api.event.EventHandler;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestCaseStarted;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestSourceRead;
import cucumber.api.formatter.Formatter;
import cucumber.api.formatter.NiceAppendable;
import cucumber.runtime.CucumberException;
import cucumber.runtime.io.URLOutputStream;
import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;
import gherkin.deps.com.google.gson.annotations.SerializedName;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class TimelineFormatter implements Formatter {
    
    private static final Comparator<TestData> TEST_DATA_COMPARATOR = new Comparator<TestData>() {
        @Override
        public int compare(final TestData o1, final TestData o2) {
            return o1.id.compareTo(o2.id);
        }
    };

    //TODO: if accepted then should move resources out into own project as per HTML report
    private static final String[] TEXT_ASSETS = new String[]{
        "/cucumber/formatter/timeline/index.html",
        "/cucumber/formatter/timeline/formatter.js",
        "/cucumber/formatter/timeline/jquery-3.3.1.min.js",
        "/cucumber/formatter/timeline/vis.min.css",
        "/cucumber/formatter/timeline/vis.min.js",
        "/cucumber/formatter/timeline/vis.override.css"};

    private EventHandler<TestSourceRead> testSourceReadHandler = new EventHandler<TestSourceRead>() {
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
    private final List<TestData> allTests = new LinkedList<TestData>();
    private final Map<Long, GroupData> allGroups = new ConcurrentHashMap<Long, GroupData>();
    private final URL reportDir;
    private final NiceAppendable reportJs;

    private ThreadLocal<TestData> currentTest = new ThreadLocal<TestData>();

    public TimelineFormatter(final URL reportDir) {
        this(reportDir, createJsonOut(reportDir, "report.js"));
    }

    TimelineFormatter(final URL reportDir, final NiceAppendable reportJs) {
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
        final Long threadId = Thread.currentThread().getId();
        final TestData test = new TestData(event, threadId);
        currentTest.set(test);
        allTests.add(test);
        if (!allGroups.containsKey(threadId)) {
            allGroups.put(threadId, new GroupData(threadId));
        }
    }

    private void handleTestCaseFinished(final TestCaseFinished event) {
        currentTest.get().end(event);
    }

    private void finishReport(final TestRunFinished event) {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        reportJs.append("$(document).ready(function() {");
        reportJs.println();
        //Sort results into feature, so can guarantee output of order
        Collections.sort(allTests, TEST_DATA_COMPARATOR);
        appendAsJsonToJs(gson, reportJs, "timelineItems", allTests);
        reportJs.println();
        //Sort results into feature, so can guarantee output of order
        appendAsJsonToJs(gson, reportJs, "timelineGroups", new TreeMap<Long, GroupData>(allGroups).values());
        reportJs.println();
        reportJs.append("});");
        reportJs.close();
        copyReportFiles();
    }

    private void appendAsJsonToJs(final Gson gson, final NiceAppendable out, final String pushTo, final Collection<?> content) {
        out.append("CucumberHTML." + pushTo + ".pushArray(");
        out.append(gson.toJson(content));
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

    class TestData {
        @SerializedName("id")
        final String id;
        @SerializedName("feature")
        final String feature;
        @SerializedName("start")
        final long startTime;
        @SerializedName("end")
        long endTime;
        @SerializedName("group")
        final long threadId;
        @SerializedName("content")
        final String content;
        @SerializedName("className")
        String className;

        TestData(final TestCaseStarted started, long threadId) {
            final String uri = started.testCase.getUri();
            final TestSourcesModel.AstNode astNode = testSources.getAstNode(uri, started.testCase.getLine());

            this.id = TestSourcesModel.calculateId(astNode);
            this.feature = TestSourcesModel.convertToId(TimelineFormatter.this.testSources.getFeature(uri).getName());
            this.startTime = System.currentTimeMillis();
            this.threadId = threadId;
            this.content = "Scenario: " + started.testCase.getName();
        }

        public void end(final TestCaseFinished event) {
            this.endTime = System.currentTimeMillis();;
            this.className = event.result.getStatus().lowerCaseName();
        }
    }

    class GroupData {
        @SerializedName("id")
        final long id;
        @SerializedName("content")
        final String content;
        
        GroupData(long threadId) {
            id = threadId;
            content = "Thread " + threadId;
        }
    }

}
