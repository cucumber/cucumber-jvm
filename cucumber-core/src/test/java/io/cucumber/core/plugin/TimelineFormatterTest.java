package io.cucumber.core.plugin;

import io.cucumber.core.backend.StubStepDefinition;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.plugin.TimelineFormatter.TimeLineGroup;
import io.cucumber.core.plugin.TimelineFormatter.TimeLineItem;
import io.cucumber.core.runner.StepDurationTimeService;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.StubBackendSupplier;
import io.cucumber.core.runtime.StubFeatureSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.messages.ndjson.internal.com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimelineFormatterTest {

    private static final Comparator<TimeLineItem> TEST_DATA_COMPARATOR = Comparator
            .comparing(TimeLineItem::getScenario);

    private static final Path REPORT_TEMPLATE_RESOURCE_DIR = Path
            .of("src/main/resources/io/cucumber/core/plugin/timeline");

    private final Feature failingFeature = TestFeatureParser.parse("some/path/failing.feature", """
            Feature: Failing Feature
             Background:
             Given bg_1
             When bg_2
             Then bg_3
             @TagA
             Scenario: Scenario 1
             Given step_01
             When step_02
             Then step_03
             Scenario: Scenario 2
             Given step_01
             When step_02
             Then step_03""");

    private final Feature successfulFeature = TestFeatureParser.parse("some/path/successful.feature", """
            Feature: Successful Feature
             Background:
             Given bg_1
             When bg_2
             Then bg_3
             @TagB @TagC
             Scenario: Scenario 3
             Given step_10
             When step_20
             Then step_30""");

    private final Feature pendingFeature = TestFeatureParser.parse("some/path/pending.feature", """
            Feature: Pending Feature
             Background:
             Given bg_1
             When bg_2
             Then bg_3
             Scenario: Scenario 4
             Given step_10
             When step_20
             Then step_50""");

    @TempDir
    Path reportDir;
    Path reportJsFile;
    RuntimeOptionsBuilder runtimeOptionsBuilder;

    @BeforeEach
    void setUp() {
        reportJsFile = reportDir.resolve("report.js");
        runtimeOptionsBuilder = new RuntimeOptionsBuilder()
                .addPluginName("timeline:" + reportDir.toAbsolutePath());
    }

    @Test
    void shouldWriteAllRequiredFilesToOutputDirectory() throws IOException {
        runFormatterWithPlugin();

        assertThat(Files.exists(reportJsFile), is(equalTo(true)));

        List<String> files = Arrays.asList("index.html", "formatter.js",
            "jquery-3.5.1.min.js",
            "vis-timeline-graph2d.min.css", "vis-timeline-graph2d.min.js",
            "vis-timeline-graph2d.override.css");
        for (String resource : files) {
            Path actualFile = reportDir.resolve(resource);
            assertTrue(Files.exists(actualFile), resource + ": did not exist in output dir");
            String actual = readFileContents(actualFile);
            String expected = readFileContents(REPORT_TEMPLATE_RESOURCE_DIR.resolve(resource));
            assertThat(resource + " differs", actual, is(equalTo(expected)));
        }
    }

    private void runFormatterWithPlugin() {
        StepDurationTimeService timeService = new StepDurationTimeService(Duration.ofSeconds(1));

        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(failingFeature,
                    successfulFeature, pendingFeature))
                .withAdditionalPlugins(timeService)
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("bg_1"),
                    new StubStepDefinition("bg_2"),
                    new StubStepDefinition("bg_3"),
                    new StubStepDefinition("step_01"),
                    new StubStepDefinition("step_02"),
                    new StubStepDefinition("step_03", new StubException()),
                    new StubStepDefinition("step_10"),
                    new StubStepDefinition("step_20"),
                    new StubStepDefinition("step_30")))
                .withRuntimeOptions(runtimeOptionsBuilder.build())
                .build()
                .run();
    }

    private String readFileContents(Path outputPath) throws IOException {
        return String.join("\n", Files.readAllLines(outputPath));
    }

    @Test
    void shouldWriteItemsCorrectlyToReportJsWhenRunInParallel() throws Throwable {
        runtimeOptionsBuilder.setThreads(2);
        runFormatterWithPlugin();

        // Have to ignore actual thread id and just checknot null
        final TimeLineItem[] expectedTests = getExpectedTestData();

        final ActualReportOutput actualOutput = readReport();

        // Cannot verify size / contents of Groups as multi threading not
        // guaranteed in Travis CI
        assertThat(actualOutput.groups, not(empty()));
        for (int i = 0; i < actualOutput.groups.size(); i++) {
            final TimeLineGroup actual = actualOutput.groups.get(i);

            final int idx = i;
            assertAll(
                () -> assertThat("id on group %s, was not as expected".formatted(idx),
                    actual.getId(),
                    is(notNullValue())),
                () -> assertThat("content on group %s, was not as expected"
                        .formatted(idx),
                    actual.getContent(),
                    is(notNullValue())));
        }

        // Sort the tests, output order is not a problem but obviously asserting
        // it is
        actualOutput.tests.sort(TEST_DATA_COMPARATOR);
        assertTimelineTestDataIsAsExpected(expectedTests, actualOutput.tests, false,
            false);
    }

    private TimeLineItem[] getExpectedTestData() throws JsonProcessingException {
        String expectedJson = """
                [
                 {
                 "feature": "Failing Feature",
                 "scenario": "Scenario 1",
                 "start": 0,
                 "end": 6000,
                 "group": "main",
                 "content": "",
                 "tags": "@taga,",
                 "className": "failed"
                 },
                 {
                 "feature": "Failing Feature",
                 "scenario": "Scenario 2",
                 "start": 6000,
                 "end": 12000,
                 "group": "main",
                 "content": "",
                 "tags": "",
                 "className": "failed"
                 },
                 {
                 "feature": "Successful Feature",
                 "scenario": "Scenario 3",
                 "start": 18000,
                 "end": 24000,
                 "group": "main",
                 "content": "",
                 "tags": "@tagb,@tagc,",
                 "className": "passed"
                 },
                 {
                 "scenario": "Scenario 4",
                 "feature": "Pending Feature",
                 "start": 12000,
                 "end": 18000,
                 "group": "main",
                 "content": "",
                 "tags": "",
                 "className": "undefined"
                 }
                ]""";

        return Jackson.OBJECT_MAPPER.readValue(expectedJson, TimeLineItem[].class);
    }

    private ActualReportOutput readReport() throws IOException {
        String itemLines = "";
        String groupLines = "";

        for (String line : Files.readAllLines(reportJsFile)) {
            if (line.startsWith("CucumberHTML.timelineItems.pushArray(")) {
                itemLines = line.substring("CucumberHTML.timelineItems.pushArray(".length(), line.length() - 2);
            } else if (line.startsWith("CucumberHTML.timelineGroups")) {
                groupLines = line.substring("CucumberHTML.timelineGroups.pushArray(".length(), line.length() - 2);
            }
        }
        TimeLineItem[] tests = Jackson.OBJECT_MAPPER.readValue(itemLines, TimeLineItem[].class);
        TimeLineGroup[] groups = Jackson.OBJECT_MAPPER.readValue(groupLines, TimeLineGroup[].class);
        return new ActualReportOutput(tests, groups);
    }

    private void assertTimelineTestDataIsAsExpected(
            final TimeLineItem[] expectedTests,
            final List<TimeLineItem> actualOutput,
            final boolean checkActualThreadData,
            final boolean checkActualTimeStamps
    ) {
        assertThat("Number of tests was not as expected", actualOutput.size(),
            is(equalTo(expectedTests.length)));
        for (int i = 0; i < expectedTests.length; i++) {
            final TimeLineItem expected = expectedTests[i];
            final TimeLineItem actual = actualOutput.get(i);
            final int idx = i;

            assertAll(
                () -> assertThat("feature on item %d, was not as expected".formatted(idx),
                    actual.getFeature(),
                    is(equalTo(expected.getFeature()))),
                () -> assertThat("className on item %d, was not as expected".formatted(idx),
                    actual.getClassName(),
                    is(equalTo(expected.getClassName()))),
                () -> assertThat("content on item %d, was not as expected".formatted(idx),
                    actual.getContent(),
                    is(equalTo(expected.getContent()))),
                () -> assertThat("tags on item %d, was not as expected".formatted(idx),
                    actual.getTags(),
                    is(equalTo(expected.getTags()))),
                () -> {
                    if (checkActualTimeStamps) {
                        assertAll(
                            () -> assertThat("startTime on item %d, was not as expected".formatted(idx),
                                actual.getStart(), is(equalTo(expected.getStart()))),
                            () -> assertThat("endTime on item %d, was not as expected".formatted(idx),
                                actual.getEnd(), is(equalTo(expected.getEnd()))));
                    } else {
                        assertAll(
                            () -> assertThat("startTime on item %d, was not as expected".formatted(idx),
                                actual.getStart(), is(notNullValue())),
                            () -> assertThat("endTime on item %d, was not as expected".formatted(idx),
                                actual.getEnd(), is(notNullValue())));
                    }
                },
                () -> {
                    if (checkActualThreadData) {
                        assertThat("threadId on item %d, was not as expected".formatted(idx),
                            actual.getGroup(),
                            is(equalTo(expected.getGroup())));
                    } else {
                        assertThat("threadId on item %d, was not as expected".formatted(idx),
                            actual.getGroup(),
                            is(notNullValue()));
                    }
                });
        }
    }

    @Test
    void shouldWriteItemsAndGroupsCorrectlyToReportJs() throws Throwable {
        runFormatterWithPlugin();

        assertTrue(Files.exists(reportJsFile));

        String groupName = Thread.currentThread().getName();

        TimeLineItem[] expectedTests = getExpectedTestData();

        TimeLineGroup[] expectedGroups = Jackson.OBJECT_MAPPER.readValue(
            """
                    [
                     {
                     "id": "main",
                     "content": "groupName"
                     }
                    ]"""
                    .replaceAll("groupName", groupName),
            TimeLineGroup[].class);

        ActualReportOutput actualOutput = readReport();

        // Sort the tests, output order is not a problem but obviously asserting
        // it is
        actualOutput.tests.sort(TEST_DATA_COMPARATOR);

        assertAll(
            () -> assertTimelineTestDataIsAsExpected(expectedTests, actualOutput.tests,
                true, true),
            () -> assertTimelineGroupDataIsAsExpected(expectedGroups,
                actualOutput.groups));
    }

    private void assertTimelineGroupDataIsAsExpected(
            TimeLineGroup[] expectedGroups,
            List<TimeLineGroup> actualOutput
    ) {
        assertThat("Number of groups was not as expected", actualOutput.size(),
            is(equalTo(expectedGroups.length)));
        for (int i = 0; i < expectedGroups.length; i++) {
            TimeLineGroup expected = expectedGroups[i];
            TimeLineGroup actual = actualOutput.get(i);

            int idx = i;
            assertAll(
                () -> assertThat("id on group %s, was not as expected"
                        .formatted(idx),
                    actual.getId(), is(equalTo(expected.getId()))),
                () -> assertThat("content on group %s, was not as expected"
                        .formatted(idx),
                    actual.getContent(), is(equalTo(expected.getContent()))));
        }
    }

    private static class ActualReportOutput {

        private final List<TimeLineItem> tests;
        private final List<TimeLineGroup> groups;

        ActualReportOutput(TimeLineItem[] tests, TimeLineGroup[] groups) {
            this.tests = Arrays.asList(tests);
            this.groups = Arrays.asList(groups);
        }

    }

}
