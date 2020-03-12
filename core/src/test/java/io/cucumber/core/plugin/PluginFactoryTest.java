package io.cucumber.core.plugin;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.runner.ClockStub;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestStepFinished;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.cucumber.core.options.TestPluginOption.parse;
import static java.time.Duration.ZERO;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class PluginFactoryTest {

    private PluginFactory fc = new PluginFactory();

    @Test
    void instantiates_junit_plugin_with_file_arg() throws IOException {
        Object plugin = fc.create(parse("junit:" + File.createTempFile("cucumber", "xml")));
        assertThat(plugin.getClass(), is(equalTo(JUnitFormatter.class)));
    }

    @Test
    void instantiates_rerun_plugin_with_file_arg() throws IOException {
        Object plugin = fc.create(parse("rerun:" + File.createTempFile("rerun", "txt")));
        assertThat(plugin.getClass(), is(equalTo(RerunFormatter.class)));
    }

    @Test
    void fails_to_instantiates_html_plugin_with_dir_arg() {
        assertThrows(
            IllegalArgumentException.class,
            () -> fc.create(parse("html:" + TempDir.createTempDirectory().getAbsolutePath()))
        );
    }

    @Test
    void fails_to_instantiate_plugin_that_wants_a_file_without_file_arg() {
        Executable testMethod = () -> fc.create(parse(WantsFile.class.getName()));
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        actualThrown.printStackTrace();
        assertThat(actualThrown.getMessage(), is(equalTo(
            "You must supply an output argument to io.cucumber.core.plugin.PluginFactoryTest$WantsFile. Like so: io.cucumber.core.plugin.PluginFactoryTest$WantsFile:DIR|FILE|URL"
        )));
    }

    @Test
    void instantiates_pretty_plugin_with_file_arg() throws IOException {
        Object plugin = fc.create(parse("pretty:" + TempDir.createTempFile().toURI().toURL()));
        assertThat(plugin.getClass(), is(equalTo(PrettyFormatter.class)));
    }

    @Test
    void instantiates_pretty_plugin_without_file_arg() {
        Object plugin = fc.create(parse("pretty"));
        assertThat(plugin.getClass(), is(equalTo(PrettyFormatter.class)));
    }

    @Test
    void instantiates_usage_plugin_without_file_arg() {
        Object plugin = fc.create(parse("usage"));
        assertThat(plugin.getClass(), is(equalTo(UsageFormatter.class)));
    }

    @Test
    void instantiates_usage_plugin_with_file_arg() throws IOException {
        Object plugin = fc.create(parse("usage:" + TempDir.createTempFile().getAbsolutePath()));
        assertThat(plugin.getClass(), is(equalTo(UsageFormatter.class)));
    }

    @Test
    void plugin_does_not_buffer_its_output() {
        PrintStream previousSystemOut = System.out;
        OutputStream mockSystemOut = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(mockSystemOut));

            // Need to create a new plugin factory here since we need it to pick up the new value of System.out
            fc = new PluginFactory();

            ProgressFormatter plugin = (ProgressFormatter) fc.create(parse("progress"));
            EventBus bus = new TimeServiceEventBus(new ClockStub(ZERO), UUID::randomUUID);
            plugin.setEventPublisher(bus);
            Result result = new Result(Status.PASSED, ZERO, null);
            TestStepFinished event = new TestStepFinished(bus.getInstant(), mock(TestCase.class), mock(PickleStepTestStep.class), result);
            bus.send(event);

            assertThat(mockSystemOut.toString(), is(not(equalTo(""))));
        } finally {
            System.setOut(previousSystemOut);
        }
    }

    @Test
    void instantiates_single_custom_appendable_plugin_with_stdout() {
        WantsOutputStream plugin1 = (WantsOutputStream) fc.create(parse(WantsOutputStream.class.getName()));
        assertThat(plugin1.printStream, is(not(nullValue())));

        Executable testMethod = () -> fc.create(parse(WantsOutputStream.class.getName()));
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Only one plugin can use STDOUT, now both io.cucumber.core.plugin.PluginFactoryTest$WantsOutputStream " +
                "and io.cucumber.core.plugin.PluginFactoryTest$WantsOutputStream use it. " +
                "If you use more than one plugin you must specify output path with io.cucumber.core.plugin.PluginFactoryTest$WantsOutputStream:DIR|FILE|URL"
        )));
    }

    @Test
    void instantiates_custom_file_plugin() {
        WantsFile plugin = (WantsFile) fc.create(parse(WantsFile.class.getName() + ":halp.txt"));
        assertThat(plugin.out, is(equalTo(new File("halp.txt"))));
    }

    @Test
    void instantiates_custom_string_arg_plugin() {
        WantsString plugin = (WantsString) fc.create(parse(WantsString.class.getName() + ":hello"));
        assertThat(plugin.arg, is(equalTo("hello")));
    }

    @Test
    void instantiates_file_or_empty_arg_plugin_with_arg() throws IOException {
        WantsFileOrEmpty plugin = (WantsFileOrEmpty) fc.create(parse(WantsFileOrEmpty.class.getName() + ":" + File.createTempFile("blah", "txt")));
        assertThat(plugin.out, is(notNullValue()));
    }

    @Test
    void instantiates_file_or_empty_arg_plugin_without_arg() {
        WantsFileOrEmpty plugin = (WantsFileOrEmpty) fc.create(parse(WantsFileOrEmpty.class.getName()));
        assertThat(plugin.out, is(nullValue()));
    }

    @Test
    void instantiates_custom_deprecated_appendable_arg_plugin() throws IOException {
        String tempDirPath = TempDir.createTempFile().getAbsolutePath();
        WantsAppendable plugin = (WantsAppendable) fc.create(parse(WantsAppendable.class.getName() + ":" + tempDirPath));
        plugin.writeAndClose("hello");
        String written = new BufferedReader(new FileReader(tempDirPath)).lines().collect(Collectors.joining());
        assertThat(written, is(equalTo("hello")));
    }

    @Test
    void instantiates_timeline_plugin_with_dir_arg() throws IOException {
        Object plugin = fc.create(parse("timeline:" + TempDir.createTempDirectory().getAbsolutePath()));
        assertThat(plugin.getClass(), is(equalTo(TimelineFormatter.class)));
    }

    @Test
    void instantiates_wants_nothing_plugin() {
        WantsNothing plugin = (WantsNothing) fc.create(parse(WantsNothing.class.getName()));
        assertThat(plugin.getClass(), is(equalTo(WantsNothing.class)));
    }

    @Test
    void fails_to_instantiate_plugin_that_wants_too_much() {
        Executable testMethod = () -> fc.create(parse(WantsTooMuch.class.getName()));
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        actualThrown.printStackTrace();
        assertThat(actualThrown.getMessage(), is(equalTo(
            "class io.cucumber.core.plugin.PluginFactoryTest$WantsTooMuch must have at least one empty constructor or a constructor that declares a single parameter of one of: [class java.lang.String, class java.io.File, class java.net.URI, class java.net.URL, class java.io.OutputStream, interface java.lang.Appendable]"
        )));
    }

    @Test
    void fails_to_instantiate_plugin_that_declares_two_single_arg_constructors_when_argument_specified() {
        Executable testMethod = () -> fc.create(parse(WantsFileOrURL.class.getName() + ":some_arg"));
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        actualThrown.printStackTrace();
        assertThat(actualThrown.getMessage(), is(equalTo(
            "class io.cucumber.core.plugin.PluginFactoryTest$WantsFileOrURL must have exactly one constructor that declares a single parameter of one of: [class java.lang.String, class java.io.File, class java.net.URI, class java.net.URL, class java.io.OutputStream, interface java.lang.Appendable]"
        )));
    }

    @Test
    void fails_to_instantiate_plugin_that_declares_two_single_arg_constructors_when_no_argument_specified() {
        Executable testMethod = () -> fc.create(parse(WantsFileOrURL.class.getName()));
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        actualThrown.printStackTrace();
        assertThat(actualThrown.getMessage(), is(equalTo(
            "You must supply an output argument to io.cucumber.core.plugin.PluginFactoryTest$WantsFileOrURL. Like so: io.cucumber.core.plugin.PluginFactoryTest$WantsFileOrURL:DIR|FILE|URL"
        )));
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
}
