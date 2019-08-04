package io.cucumber.core.plugin;

import io.cucumber.core.event.PickleStepTestStep;
import io.cucumber.core.event.Result;
import io.cucumber.core.event.Status;
import io.cucumber.core.event.TestCase;
import io.cucumber.core.event.TestStepFinished;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.runner.ClockStub;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static io.cucumber.core.options.TestPluginOption.parse;
import static java.time.Duration.ZERO;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class PluginFactoryTest {

    private PluginFactory fc = new PluginFactory();

    @Test
    public void instantiates_junit_plugin_with_file_arg() throws IOException {
        Object plugin = fc.create(parse("junit:" + File.createTempFile("cucumber", "xml")));
        assertThat(plugin.getClass(), is(equalTo(JUnitFormatter.class)));
    }

    @Test
    public void instantiates_html_plugin_with_dir_arg() throws IOException {
        Object plugin = fc.create(parse("html:" + TempDir.createTempDirectory().getAbsolutePath()));
        assertThat(plugin.getClass(), is(equalTo(HTMLFormatter.class)));
    }

    @Test
    public void fails_to_instantiate_html_plugin_without_dir_arg() {
        final Executable testMethod = () -> fc.create(parse("html"));
        final CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "You must supply an output argument to html. Like so: html:output"
        )));
    }

    @Test
    public void instantiates_pretty_plugin_with_file_arg() throws IOException {
        Object plugin = fc.create(parse("pretty:" + TempDir.createTempFile().toURI().toURL()));
        assertThat(plugin.getClass(), is(equalTo(PrettyFormatter.class)));
    }

    @Test
    public void instantiates_pretty_plugin_without_file_arg() {
        Object plugin = fc.create(parse("pretty"));
        assertThat(plugin.getClass(), is(equalTo(PrettyFormatter.class)));
    }

    @Test
    public void instantiates_usage_plugin_without_file_arg() {
        Object plugin = fc.create(parse("usage"));
        assertThat(plugin.getClass(), is(equalTo(UsageFormatter.class)));
    }

    @Test
    public void instantiates_usage_plugin_with_file_arg() throws IOException {
        Object plugin = fc.create(parse("usage:" + TempDir.createTempFile().getAbsolutePath()));
        assertThat(plugin.getClass(), is(equalTo(UsageFormatter.class)));
    }

    @Test
    public void plugin_does_not_buffer_its_output() {
        PrintStream previousSystemOut = System.out;
        OutputStream mockSystemOut = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(mockSystemOut));

            // Need to create a new plugin factory here since we need it to pick up the new value of System.out
            fc = new PluginFactory();

            ProgressFormatter plugin = (ProgressFormatter) fc.create(parse("progress"));
            EventBus bus = new TimeServiceEventBus(new ClockStub(ZERO));
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
    public void instantiates_single_custom_appendable_plugin_with_stdout() {
        WantsAppendable plugin = (WantsAppendable) fc.create(parse("io.cucumber.core.plugin.PluginFactoryTest$WantsAppendable"));
        assertThat(plugin.out, is(instanceOf(PrintStream.class)));

        final Executable testMethod = () -> fc.create(parse("io.cucumber.core.plugin.PluginFactoryTest$WantsAppendable"));
        final CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Only one plugin can use STDOUT, now both io.cucumber.core.plugin.PluginFactoryTest$WantsAppendable " +
                "and io.cucumber.core.plugin.PluginFactoryTest$WantsAppendable use it. " +
                "If you use more than one plugin you must specify output path with PLUGIN:PATH_OR_URL"
        )));
    }

    @Test
    public void instantiates_custom_appendable_plugin_with_stdout_and_file() throws IOException {
        WantsAppendable plugin = (WantsAppendable) fc.create(parse("io.cucumber.core.plugin.PluginFactoryTest$WantsAppendable"));
        assertThat(plugin.out, is(instanceOf(PrintStream.class)));

        WantsAppendable plugin2 = (WantsAppendable) fc.create(parse("io.cucumber.core.plugin.PluginFactoryTest$WantsAppendable:" + TempDir.createTempFile().getAbsolutePath()));
        assertThat(plugin2.out.getClass(), is(equalTo(UTF8OutputStreamWriter.class)));
    }

    @Test
    public void instantiates_custom_url_plugin() throws IOException {
        WantsUrl plugin = (WantsUrl) fc.create(parse("io.cucumber.core.plugin.PluginFactoryTest$WantsUrl:halp"));
        assertThat(plugin.out, is(equalTo(new URL("file:halp/"))));
    }

    @Test
    public void instantiates_custom_url_plugin_with_http() throws IOException {
        WantsUrl plugin = (WantsUrl) fc.create(parse("io.cucumber.core.plugin.PluginFactoryTest$WantsUrl:http://halp/"));
        assertThat(plugin.out, is(equalTo(new URL("http://halp/"))));
    }

    @Test
    public void instantiates_custom_uri_plugin_with_ws() throws URISyntaxException {
        WantsUri plugin = (WantsUri) fc.create(parse("io.cucumber.core.plugin.PluginFactoryTest$WantsUri:ws://halp/"));
        assertThat(plugin.out, is(equalTo(new URI("ws://halp/"))));
    }

    @Test
    public void instantiates_custom_file_plugin() {
        WantsFile plugin = (WantsFile) fc.create(parse("io.cucumber.core.plugin.PluginFactoryTest$WantsFile:halp.txt"));
        assertThat(plugin.out, is(equalTo(new File("halp.txt"))));
    }

    @Test
    public void instantiates_custom_string_arg_plugin() {
        WantsString plugin = (WantsString) fc.create(parse("io.cucumber.core.plugin.PluginFactoryTest$WantsString:hello"));
        assertThat(plugin.arg, is(equalTo("hello")));
    }

    @Test
    public void instantiates_plugin_using_empty_constructor_when_unspecified() {
        WantsStringOrDefault plugin = (WantsStringOrDefault) fc.create(parse("io.cucumber.core.plugin.PluginFactoryTest$WantsStringOrDefault"));
        assertThat(plugin.arg, is(equalTo("defaultValue")));
    }

    @Test
    public void instantiates_plugin_using_arg_constructor_when_specified() {
        WantsStringOrDefault plugin = (WantsStringOrDefault) fc.create(parse("io.cucumber.core.plugin.PluginFactoryTest$WantsStringOrDefault:hello"));
        assertThat(plugin.arg, is(equalTo("hello")));
    }

    @Test
    public void instantiates_timeline_plugin_with_dir_arg() throws IOException {
        Object plugin = fc.create(parse("timeline:" + TempDir.createTempDirectory().getAbsolutePath()));
        assertThat(plugin.getClass(), is(equalTo(TimelineFormatter.class)));
    }


    @Test
    public void test_url() throws MalformedURLException {
        URL dotCucumber = PluginFactory.toURL("foo/bar/.cucumber");
        URL url = new URL(dotCucumber, "stepdefs.json");
        assertThat(url, is(equalTo(new URL("file:foo/bar/.cucumber/stepdefs.json"))));
    }

    public static class WantsAppendable extends StubFormatter {
        public final Appendable out;

        public WantsAppendable(Appendable out) {
            this.out = out;
        }

        public WantsAppendable() {
            this.out = null;
        }
    }

    public static class WantsUrl extends StubFormatter {
        public final URL out;

        public WantsUrl(URL out) {
            this.out = out;
        }
    }

    public static class WantsUri extends StubFormatter {
        public final URI out;

        public WantsUri(URI out) {
            this.out = out;
        }
    }

    public static class WantsFile extends StubFormatter {
        public final File out;

        public WantsFile(File out) {
            this.out = out;
        }
    }

    public static class WantsString extends StubFormatter {
        public final String arg;

        public WantsString(String arg) {
            this.arg = arg;
        }
    }

    public static class WantsStringOrDefault extends StubFormatter {
        public final String arg;

        public WantsStringOrDefault(String arg) {
            this.arg = arg;
        }

        public WantsStringOrDefault() {
            this("defaultValue");
        }
    }

}
