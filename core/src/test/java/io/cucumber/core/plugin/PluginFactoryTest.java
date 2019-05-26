package io.cucumber.core.plugin;

import io.cucumber.core.api.event.PickleStepTestStep;
import io.cucumber.core.api.event.Result;
import io.cucumber.core.api.event.TestCase;
import io.cucumber.core.api.event.TestStepFinished;
import io.cucumber.core.event.EventBus;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.runner.TimeServiceEventBus;
import io.cucumber.core.runner.ClockStub;
import org.junit.Test;

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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class PluginFactoryTest {
    private PluginFactory fc = new PluginFactory();

    @Test
    public void instantiates_junit_plugin_with_file_arg() throws IOException {
        Object plugin = fc.create(parse("junit:" + File.createTempFile("cucumber", "xml")));
        assertEquals(JUnitFormatter.class, plugin.getClass());
    }

    @Test
    public void instantiates_html_plugin_with_dir_arg() throws IOException {
        Object plugin = fc.create(parse("html:" + TempDir.createTempDirectory().getAbsolutePath()));
        assertEquals(HTMLFormatter.class, plugin.getClass());
    }

    @Test
    public void fails_to_instantiate_html_plugin_without_dir_arg() throws IOException {
        try {
            fc.create(parse("html"));
            fail();
        } catch (CucumberException e) {
            assertEquals("You must supply an output argument to html. Like so: html:output", e.getMessage());
        }
    }

    @Test
    public void instantiates_pretty_plugin_with_file_arg() throws IOException {
        Object plugin = fc.create(parse("pretty:" + TempDir.createTempFile().toURI().toURL()));
        assertEquals(PrettyFormatter.class, plugin.getClass());
    }

    @Test
    public void instantiates_pretty_plugin_without_file_arg() {
        Object plugin = fc.create(parse("pretty"));
        assertEquals(PrettyFormatter.class, plugin.getClass());
    }

    @Test
    public void instantiates_usage_plugin_without_file_arg() {
        Object plugin = fc.create(parse("usage"));
        assertEquals(UsageFormatter.class, plugin.getClass());
    }

    @Test
    public void instantiates_usage_plugin_with_file_arg() throws IOException {
        Object plugin = fc.create(parse("usage:" + TempDir.createTempFile().getAbsolutePath()));
        assertEquals(UsageFormatter.class, plugin.getClass());
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
            Result result = new Result(Result.Type.PASSED, ZERO, null);
            TestStepFinished event = new TestStepFinished(bus.getInstant(), mock(TestCase.class), mock(PickleStepTestStep.class), result);
            bus.send(event);

            assertThat(mockSystemOut.toString(), is(not("")));
        } finally {
            System.setOut(previousSystemOut);
        }
    }

    @Test
    public void instantiates_single_custom_appendable_plugin_with_stdout() {
        WantsAppendable plugin = (WantsAppendable) fc.create(parse("io.cucumber.core.plugin.PluginFactoryTest$WantsAppendable"));
        assertThat(plugin.out, is(instanceOf(PrintStream.class)));
        try {
            fc.create(parse("io.cucumber.core.plugin.PluginFactoryTest$WantsAppendable"));
            fail();
        } catch (CucumberException expected) {
            assertEquals("Only one plugin can use STDOUT, now both io.cucumber.core.plugin.PluginFactoryTest$WantsAppendable " +
                    "and io.cucumber.core.plugin.PluginFactoryTest$WantsAppendable use it. " +
                    "If you use more than one plugin you must specify output path with PLUGIN:PATH_OR_URL", expected.getMessage());
        }
    }

    @Test
    public void instantiates_custom_appendable_plugin_with_stdout_and_file() throws IOException {
        WantsAppendable plugin = (WantsAppendable) fc.create(parse("io.cucumber.core.plugin.PluginFactoryTest$WantsAppendable"));
        assertThat(plugin.out, is(instanceOf(PrintStream.class)));

        WantsAppendable plugin2 = (WantsAppendable) fc.create(parse("io.cucumber.core.plugin.PluginFactoryTest$WantsAppendable:" + TempDir.createTempFile().getAbsolutePath()));
        assertEquals(UTF8OutputStreamWriter.class, plugin2.out.getClass());
    }

    @Test
    public void instantiates_custom_url_plugin() throws IOException {
        WantsUrl plugin = (WantsUrl) fc.create(parse("io.cucumber.core.plugin.PluginFactoryTest$WantsUrl:halp"));
        assertEquals(new URL("file:halp/"), plugin.out);
    }

    @Test
    public void instantiates_custom_url_plugin_with_http() throws IOException {
        WantsUrl plugin = (WantsUrl) fc.create(parse("io.cucumber.core.plugin.PluginFactoryTest$WantsUrl:http://halp/"));
        assertEquals(new URL("http://halp/"), plugin.out);
    }

    @Test
    public void instantiates_custom_uri_plugin_with_ws() throws IOException, URISyntaxException {
        WantsUri plugin = (WantsUri) fc.create(parse("io.cucumber.core.plugin.PluginFactoryTest$WantsUri:ws://halp/"));
        assertEquals(new URI("ws://halp/"), plugin.out);
    }

    @Test
    public void instantiates_custom_file_plugin() throws IOException {
        WantsFile plugin = (WantsFile) fc.create(parse("io.cucumber.core.plugin.PluginFactoryTest$WantsFile:halp.txt"));
        assertEquals(new File("halp.txt"), plugin.out);
    }

    @Test
    public void instantiates_custom_string_arg_plugin() throws IOException {
        WantsString plugin = (WantsString) fc.create(parse("io.cucumber.core.plugin.PluginFactoryTest$WantsString:hello"));
        assertEquals("hello", plugin.arg);
    }

    @Test
    public void instantiates_plugin_using_empty_constructor_when_unspecified() throws IOException {
        WantsStringOrDefault plugin = (WantsStringOrDefault) fc.create(parse("io.cucumber.core.plugin.PluginFactoryTest$WantsStringOrDefault"));
        assertEquals("defaultValue", plugin.arg);
    }

    @Test
    public void instantiates_plugin_using_arg_constructor_when_specified() throws IOException {
        WantsStringOrDefault plugin = (WantsStringOrDefault) fc.create(parse("io.cucumber.core.plugin.PluginFactoryTest$WantsStringOrDefault:hello"));
        assertEquals("hello", plugin.arg);
    }

    @Test
    public void instantiates_timeline_plugin_with_dir_arg() throws IOException {
        Object plugin = fc.create(parse("timeline:" + TempDir.createTempDirectory().getAbsolutePath()));
        assertEquals(TimelineFormatter.class, plugin.getClass());
    }


    @Test
    public void test_url() throws MalformedURLException {
        URL dotCucumber = PluginFactory.toURL("foo/bar/.cucumber");
        URL url = new URL(dotCucumber, "stepdefs.json");
        assertEquals(new URL("file:foo/bar/.cucumber/stepdefs.json"), url);
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
