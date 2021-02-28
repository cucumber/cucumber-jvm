package io.cucumber.core.plugin;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.options.PluginOption;
import io.cucumber.core.runner.ClockStub;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestStepFinished;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static io.cucumber.core.options.TestPluginOption.parse;
import static java.nio.file.Files.readAllLines;
import static java.time.Duration.ZERO;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class PluginFactoryTest {

    private PluginFactory fc = new PluginFactory();

    private Object plugin;

    @TempDir
    Path tmp;

    @AfterEach
    void cleanUp() {
        if (plugin != null) {
            releaseResources(plugin);
        }
    }

    @Test
    void instantiates_junit_plugin_with_file_arg() {
        PluginOption option = parse("junit:" + tmp.resolve("cucumber.xml"));
        plugin = fc.create(option);
        assertThat(plugin.getClass(), is(equalTo(JUnitFormatter.class)));
    }

    @Test
    void instantiates_rerun_plugin_with_file_arg() {
        PluginOption option = parse("rerun:" + tmp.resolve("rerun.txt"));
        plugin = fc.create(option);
        assertThat(plugin.getClass(), is(equalTo(RerunFormatter.class)));
    }

    @Test
    void creates_parent_directories() {
        Path file = tmp.resolve("target/cucumber/reports/rerun.txt");
        PluginOption option = parse("rerun:" + file);
        assertAll(
            () -> assertThat(Files.exists(file), is(false)),
            () -> assertDoesNotThrow(() -> {
                Object plugin = fc.create(option);
                releaseResources(plugin);
            }),
            () -> assertThat(Files.exists(file), is(true)));
    }

    @Test
    void cant_create_plugin_when_parent_directory_is_a_file() {
        Path htmlReport = tmp.resolve("target/cucumber/reports");
        PluginOption htmlOption = parse("html:" + htmlReport);
        plugin = fc.create(htmlOption);

        Path jsonReport = tmp.resolve("target/cucumber/reports/cucumber.json");
        PluginOption jsonOption = parse("json:" + jsonReport);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> fc.create(jsonOption));
        assertThat(exception.getMessage(), is(equalTo(
            "Couldn't create parent directories of '" + jsonReport + "'.\n" +
                    "Make sure the the parent directory '" + jsonReport.getParent() + "' isn't a file.\n" +
                    "\n" +
                    "Note: This usually happens when plugins write to colliding paths.\n" +
                    "For example: 'html:target/cucumber, json:target/cucumber/report.json'\n" +
                    "You can fix this by making the paths do no collide.\n" +
                    "For example: 'html:target/cucumber/report.html, json:target/cucumber/report.json'\n" +
                    "The details are in the stack trace below:")));
    }

    @Test
    void cant_create_plugin_when_file_is_a_directory() {
        Path jsonReport = tmp.resolve("target/cucumber/reports/cucumber.json");
        PluginOption jsonOption = parse("json:" + jsonReport);
        plugin = fc.create(jsonOption);

        Path htmlReport = tmp.resolve("target/cucumber/reports");
        PluginOption htmlOption = parse("html:" + htmlReport);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> fc.create(htmlOption));
        assertThat(exception.getMessage(), is(equalTo(
            "Couldn't create a file output stream for '" + htmlReport + "'.\n" +
                    "Make sure the the file isn't a directory.\n" +
                    "\n" +
                    "Note: This usually happens when plugins write to colliding paths.\n" +
                    "For example: 'json:target/cucumber/report.json, html:target/cucumber'\n" +
                    "You can fix this by making the paths do no collide.\n" +
                    "For example: 'json:target/cucumber/report.json, html:target/cucumber/report.html'\n" +
                    "The details are in the stack trace below:")));
    }

    @Test
    void fails_to_instantiates_html_plugin_with_dir_arg() {
        PluginOption option = parse("html:" + tmp.toAbsolutePath());
        assertThrows(IllegalArgumentException.class, () -> fc.create(option));
    }

    @Test
    void fails_to_instantiate_plugin_that_wants_a_file_without_file_arg() {
        PluginOption option = parse(WantsFile.class.getName());
        Executable testMethod = () -> fc.create(option);
        CucumberException exception = assertThrows(CucumberException.class, testMethod);
        assertThat(exception.getMessage(), is(equalTo(
            "You must supply an output argument to io.cucumber.core.plugin.PluginFactoryTest$WantsFile. Like so: io.cucumber.core.plugin.PluginFactoryTest$WantsFile:DIR|FILE|URL")));
    }

    @Test
    void instantiates_pretty_plugin_with_file_arg() throws IOException {
        PluginOption option = parse("pretty:" + tmp.resolve("out.txt").toUri().toURL());
        plugin = fc.create(option);
        assertThat(plugin.getClass(), is(equalTo(PrettyFormatter.class)));
    }

    @Test
    void instantiates_pretty_plugin_without_file_arg() {
        PluginOption option = parse("pretty");
        plugin = fc.create(option);
        assertThat(plugin.getClass(), is(equalTo(PrettyFormatter.class)));
    }

    @Test
    void instantiates_usage_plugin_without_file_arg() {
        PluginOption option = parse("usage");
        plugin = fc.create(option);
        assertThat(plugin.getClass(), is(equalTo(UsageFormatter.class)));
    }

    @Test
    void instantiates_usage_plugin_with_file_arg() {
        PluginOption option = parse("usage:" + tmp.resolve("out.txt").toAbsolutePath());
        plugin = fc.create(option);
        assertThat(plugin.getClass(), is(equalTo(UsageFormatter.class)));
    }

    @Test
    void plugin_does_not_buffer_its_output() {
        PrintStream previousSystemOut = System.out;
        OutputStream mockSystemOut = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(mockSystemOut));

            // Need to create a new plugin factory here since we need it to pick
            // up the new value of System.out
            fc = new PluginFactory();

            PluginOption option = parse("progress");
            ProgressFormatter plugin = (ProgressFormatter) fc.create(option);
            EventBus bus = new TimeServiceEventBus(new ClockStub(ZERO), UUID::randomUUID);
            plugin.setEventPublisher(bus);
            Result result = new Result(Status.PASSED, ZERO, null);
            TestStepFinished event = new TestStepFinished(bus.getInstant(), mock(TestCase.class),
                mock(PickleStepTestStep.class), result);
            bus.send(event);

            assertThat(mockSystemOut.toString(), is(not(equalTo(""))));
        } finally {
            System.setOut(previousSystemOut);
        }
    }

    @Test
    void instantiates_single_custom_appendable_plugin_with_stdout() {
        PluginOption option = parse(WantsOutputStream.class.getName());
        WantsOutputStream plugin = (WantsOutputStream) fc.create(option);
        assertThat(plugin.printStream, is(not(nullValue())));

        CucumberException exception = assertThrows(CucumberException.class, () -> fc.create(option));
        assertThat(exception.getMessage(), is(equalTo(
            "Only one plugin can use STDOUT, now both io.cucumber.core.plugin.PluginFactoryTest$WantsOutputStream " +
                    "and io.cucumber.core.plugin.PluginFactoryTest$WantsOutputStream use it. " +
                    "If you use more than one plugin you must specify output path with io.cucumber.core.plugin.PluginFactoryTest$WantsOutputStream:DIR|FILE|URL")));
    }

    @Test
    void instantiates_custom_file_plugin() {
        PluginOption option = parse(WantsFile.class.getName() + ":halp.txt");
        WantsFile plugin = (WantsFile) fc.create(option);
        assertThat(plugin.out, is(equalTo(new File("halp.txt"))));
    }

    @Test
    void instantiates_custom_string_arg_plugin() {
        PluginOption option = parse(WantsString.class.getName() + ":hello");
        WantsString plugin = (WantsString) fc.create(option);
        assertThat(plugin.arg, is(equalTo("hello")));
    }

    @Test
    void instantiates_file_or_empty_arg_plugin_with_arg() {
        PluginOption option = parse(WantsFileOrEmpty.class.getName() + ":" + tmp.resolve("out.txt"));
        WantsFileOrEmpty plugin = (WantsFileOrEmpty) fc.create(option);
        assertThat(plugin.out, is(notNullValue()));
    }

    @Test
    void instantiates_file_or_empty_arg_plugin_without_arg() {
        PluginOption option = parse(WantsFileOrEmpty.class.getName());
        WantsFileOrEmpty plugin = (WantsFileOrEmpty) fc.create(option);
        assertThat(plugin.out, is(nullValue()));
    }

    @Test
    void instantiates_custom_deprecated_appendable_arg_plugin() throws IOException {
        Path tempDirPath = tmp.resolve("out.txt").toAbsolutePath();
        PluginOption option = parse(WantsAppendable.class.getName() + ":" + tempDirPath);
        WantsAppendable plugin = (WantsAppendable) fc.create(option);
        plugin.writeAndClose("hello");
        String written = String.join("", readAllLines(tempDirPath));
        assertThat(written, is(equalTo("hello")));
    }

    @Test
    void instantiates_timeline_plugin_with_dir_arg() {
        PluginOption option = parse("timeline:" + tmp.toAbsolutePath());
        plugin = fc.create(option);
        assertThat(plugin.getClass(), is(equalTo(TimelineFormatter.class)));
    }

    @Test
    void instantiates_wants_nothing_plugin() {
        PluginOption option = parse(WantsNothing.class.getName());
        WantsNothing plugin = (WantsNothing) fc.create(option);
        assertThat(plugin.getClass(), is(equalTo(WantsNothing.class)));
    }

    @Test
    void fails_to_instantiate_plugin_that_wants_too_much() {
        PluginOption option = parse(WantsTooMuch.class.getName());
        Executable testMethod = () -> fc.create(option);
        CucumberException exception = assertThrows(CucumberException.class, testMethod);
        assertThat(exception.getMessage(), is(equalTo(
            "class io.cucumber.core.plugin.PluginFactoryTest$WantsTooMuch must have at least one empty constructor or a constructor that declares a single parameter of one of: [class java.lang.String, class java.io.File, class java.net.URI, class java.net.URL, class java.io.OutputStream, interface java.lang.Appendable]")));
    }

    @Test
    void fails_to_instantiate_plugin_that_declares_two_single_arg_constructors_when_argument_specified() {
        PluginOption option = parse(WantsFileOrURL.class.getName() + ":some_arg");
        Executable testMethod = () -> fc.create(option);
        CucumberException exception = assertThrows(CucumberException.class, testMethod);
        assertThat(exception.getMessage(), is(equalTo(
            "class io.cucumber.core.plugin.PluginFactoryTest$WantsFileOrURL must have exactly one constructor that declares a single parameter of one of: [class java.lang.String, class java.io.File, class java.net.URI, class java.net.URL, class java.io.OutputStream, interface java.lang.Appendable]")));
    }

    @Test
    void fails_to_instantiate_plugin_that_declares_two_single_arg_constructors_when_no_argument_specified() {
        PluginOption option = parse(WantsFileOrURL.class.getName());
        Executable testMethod = () -> fc.create(option);
        CucumberException exception = assertThrows(CucumberException.class, testMethod);
        assertThat(exception.getMessage(), is(equalTo(
            "You must supply an output argument to io.cucumber.core.plugin.PluginFactoryTest$WantsFileOrURL. Like so: io.cucumber.core.plugin.PluginFactoryTest$WantsFileOrURL:DIR|FILE|URL")));
    }

    public static class WantsOutputStream extends StubFormatter {

        public OutputStream printStream;

        public WantsOutputStream(OutputStream outputStream) {
            this.printStream = Objects.requireNonNull(outputStream);
        }

    }

    public static class WantsFileOrEmpty extends StubFormatter {

        public File out = null;

        public WantsFileOrEmpty(File out) {
            this.out = Objects.requireNonNull(out);
        }

        public WantsFileOrEmpty() {
        }

    }

    public static class WantsFile extends StubFormatter {

        public final File out;

        public WantsFile(File out) {
            this.out = Objects.requireNonNull(out);
        }

    }

    public static class WantsFileOrURL extends StubFormatter {

        public WantsFileOrURL(File out) {
            Objects.requireNonNull(out);
        }

        public WantsFileOrURL(URL out) {
            Objects.requireNonNull(out);
        }

    }

    public static class WantsString extends StubFormatter {

        public final String arg;

        public WantsString(String arg) {
            this.arg = Objects.requireNonNull(arg);
        }

    }

    public static class WantsAppendable extends StubFormatter {

        public final NiceAppendable arg;

        public WantsAppendable(Appendable arg) {
            this.arg = new NiceAppendable(Objects.requireNonNull(arg));
        }

        public void writeAndClose(String s) {
            this.arg.println(s);
            this.arg.close();
        }

    }

    public static class WantsNothing extends StubFormatter {

    }

    public static class WantsTooMuch extends StubFormatter {

        public WantsTooMuch(String too, String much) {
        }

    }

    private static class FakeTestRunEventsPublisher implements EventPublisher {
        private EventHandler<TestRunStarted> startHandler;
        private EventHandler<TestRunFinished> finishedHandler;

        @Override
        public <T> void registerHandlerFor(Class<T> eventType, EventHandler<T> handler) {
            if (eventType == TestRunStarted.class) {
                startHandler = ((EventHandler<TestRunStarted>) handler);
            }
            if (eventType == TestRunFinished.class) {
                finishedHandler = ((EventHandler<TestRunFinished>) handler);
            }
        }

        @Override
        public <T> void removeHandlerFor(Class<T> eventType, EventHandler<T> handler) {
        }

        public void fakeTestRunEvents() {
            if (startHandler != null) {
                startHandler.receive(new TestRunStarted(Instant.now()));
            }
            if (finishedHandler != null) {
                finishedHandler.receive(new TestRunFinished(Instant.now(), new Result(Status.PASSED, ZERO, null)));
            }
        }

    }

    private void releaseResources(Object plugin) {
        FakeTestRunEventsPublisher fakeTestRun = new FakeTestRunEventsPublisher();
        if (plugin instanceof EventListener) {
            ((EventListener) plugin).setEventPublisher(fakeTestRun);
            fakeTestRun.fakeTestRunEvents();
        } else if (plugin instanceof ConcurrentEventListener) {
            ((ConcurrentEventListener) plugin).setEventPublisher(fakeTestRun);
            fakeTestRun.fakeTestRunEvents();
        }
    }

}
