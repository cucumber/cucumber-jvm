package cucumber.cli;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.*;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class RuntimeOptionsTest {

    private class TestReceiver implements Messagable {
        public final List<String> messages = new ArrayList<String>();
        public void message(String $message) {
            messages.add($message);
        }
    }

    private RuntimeOptions _options;
    private TestReceiver _receiver;

    @Before
    public void setup() {
        _options = new RuntimeOptions();
        _receiver = new TestReceiver();
    }

    @After
    public void tearDown() {
        _options = null;
        _receiver = null;
    }

    private void assertThatMessageReceived(String $reason, String $message) {
        assertThat($reason, _receiver.messages, hasItem($message));
    }

    private void assertThatMessageNotReceived(String $reason, String $message) {
        assertThat($reason, _receiver.messages, not(hasItem($message)));
    }
    
    private void assertNumberOfMessagesReceived(String $reason, int $count) {
        assertThat($reason, _receiver.messages.size(), is($count));
    }

    private void parseAndReceiveErrors(String[] options) {
        _options.parse(options);
        _receiver.messages.clear();
        _options.applyErrorsTo(_receiver);
    }

    @Test
    public void has_version_from_properties_file() {
        assertThat(RuntimeOptions.VERSION, containsString("1.0"));
    }

    @Test
    public void should_be_valid_when_glue_path_is_specified() {
        String [] options = {"--glue", "abc"};
        parseAndReceiveErrors(options);
        assertNumberOfMessagesReceived("empty options should be valid", 0);
    }

    @Test
    public void should_be_invalid_when_glue_path_is_absent() {
        String [] options = {};
        parseAndReceiveErrors(options);
        assertThatMessageReceived("absent glue path should provide an error", RuntimeOptions.GLUE_REQUIRED);
    }

    @Test
    public void should_be_invalid_when_html_formatter_is_used_without_output_directory() {
        String [] options = {"--format", "html"};
        parseAndReceiveErrors(options);
        assertThatMessageReceived("html without output should have an error", RuntimeOptions.OUTPUT_REQUIRED);
    }
    
    @Test
    public void should_not_contain_output_error_when_html_formatter_is_used_with_output_directory() {
        String [] options = {"-f", "html", "-o", "reports"};
        parseAndReceiveErrors(options);
        assertThatMessageNotReceived("html with output should not have an error", RuntimeOptions.OUTPUT_REQUIRED);
    }
    
    @Test
    public void flag_matches_should_be_true_on_match() {
        assertThat("flag matches should be true on match of first parameter", _options.flagMatches("-o", "--output", "-o"), is(true));
        assertThat("flag matches should be true on match of second parameter", _options.flagMatches("--output", "--output", "-o"), is(true));
    }
    
    @Test
    public void flag_matches_should_be_false_on_mismatch() {
        assertThat("flag matches should be false on mismatch", _options.flagMatches("-f", "--output", "-o"), is(false));
    }

    @Test
    public void flag_matches_should_be_false_when_a_hyphen_is_not_found_for_the_first_character() {
        assertThat("flag matches should be false with invalid flag", _options.flagMatches("f", "format", "f"), is(false));
    }

    @Test
    public void ensure_multiple_calls_to_validate_does_not_accumulate_errors() {
        String [] options = {};
        parseAndReceiveErrors(options);
        int initialCount = _receiver.messages.size();
        parseAndReceiveErrors(options);
        int finalCount = _receiver.messages.size();
        assertThat("error count should be the same", finalCount, is(equalTo(initialCount)));
    }

    @Test
    public void reset_should_set_values_to_default() throws InterruptedException {
        _options.validate(); // force errors

        _options.addFeaturePath(".");
        _options.addFilterTag("abc");
        _options.addFormat("abc");
        _options.addGluePath("abc");
        _options.addOutputPath("abc");

        _options.setDotCucumber("abc");

        _options.setHelpRequested(true);
        _options.setVersionRequested(true);
        _options.setDryRun(true);

        _options.reset();


        assertThatMessageNotReceived("errors should not be present", RuntimeOptions.GLUE_REQUIRED);

        assertThat("feature paths should be empty", _options.getFeaturePaths().isEmpty(), is(true));
        assertThat("filter tag should be empty", _options.getFilterTags().size(), is(equalTo(0)));
        assertThat("formats should be empty", _options.getFormats().size(), is(equalTo(0)));
        assertThat("glue paths should be empty", _options.getGluePaths().size(), is(equalTo(0)));
        assertThat("output path should be empty",  _options.getOutputPath("abc"), is(nullValue()));

        assertThat("dry run should be disabled", _options.isDryRun(), is(false));
        _options.applyIfHelpRequestedTo(_receiver);
        assertThat("help should not be requested", _receiver.messages, not(hasItem(RuntimeOptions.USAGE)));
        _options.applyIfVersionRequestedTo(_receiver);
        assertThat("version should not be requested", _receiver.messages, not(hasItem(RuntimeOptions.USAGE)));

        assertThat("dot cucumber should be empty", _options.getDotCucumber(), is(equalTo("")));
    }

    @Test
    public void output_should_be_linked_with_the_format() {
        String [] options = {"--format", "html", "--out", "target/html", "--format", "progress", "--format", "json", "--out", "target/cucumber.json"};
        _options.parse(options);
        assertThat("should have 3 formats", _options.getFormats().size(), is(equalTo(3)));
        assertThat("json output should match argument", _options.getOutputPath("json"), is(equalTo("target/cucumber.json")));
        assertThat("html output should match argument", _options.getOutputPath("html"), is(equalTo("target/html")));
        assertThat("progress output should be null", _options.getOutputPath("progress"), is(nullValue()));
    }

    @Test
    public void should_have_errors_if_two_formats_output_to_the_console() {
        String [] options = {"--format", "html", "--format", "progress", "--format", "json", "--out", "target/cucumber.json"};
        parseAndReceiveErrors(options);
        assertThatMessageReceived("should have output path missing error", RuntimeOptions.THERE_CAN_ONLY_BE_ONE);
        assertThat("should have 3 formats", _options.getFormats().size(), is(equalTo(3)));
        assertThat("should have 1 output", _options.getOutputPaths().size(), is(equalTo(1)));
        assertThat("json output should match argument", _options.getOutputPath("json"), is(equalTo("target/cucumber.json")));
    }
}
