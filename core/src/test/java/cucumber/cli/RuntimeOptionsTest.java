package cucumber.cli;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.*;

public class RuntimeOptionsTest {

    private RuntimeOptions _registry;

    @Before
    public void setup() {
        _registry = new RuntimeOptions();
    }

    @After
    public void tearDown() {
        _registry = null;
    }

    @Test
    public void has_version_from_properties_file() {
        assertThat(RuntimeOptions.VERSION, containsString("1.0"));
    }

    @Test
    public void should_be_valid_when_glue_path_is_specified() {
        String [] options = {"--glue", "abc"};
        _registry.parse(options);
        assertThat("empty options should be valid", _registry.hasErrors(), is(false));
    }

    @Test
    public void should_be_invalid_when_glue_path_is_absent() {
        String [] options = {};
        _registry.parse(options);
        assertThat("registry requires glue path", _registry.hasErrors(), is(true));
        assertThat("absent registry should have an error", _registry.getErrors(), hasItem(RuntimeOptions.GLUE_REQUIRED));
    }

    @Test
    public void should_be_invalid_when_html_formatter_is_used_without_output_directory() {
        String [] options = {"--format", "html"};
        _registry.parse(options);
        assertThat("html formatter requires output directory", _registry.hasErrors(), is(true));
        assertThat("html without output should have an error", _registry.getErrors(), hasItem(RuntimeOptions.OUTPUT_REQUIRED));
    }
    
    @Test
    public void should_not_contain_output_error_when_html_formatter_is_used_with_output_directory() {
        String [] options = {"-f", "html", "-o", "reports"};
        // don't bother checking hasErrors as it is likely to contain errors about glue, etc
        assertThat("html with output should not have an error", _registry.getErrors(), not(hasItem(RuntimeOptions.OUTPUT_REQUIRED)));
    }
    
    @Test
    public void flag_matches_should_be_true_on_match() {
        assertThat("flag matches should be true on match of first parameter", _registry.flagMatches("-o", "--output", "-o"), is(true));
        assertThat("flag matches should be true on match of second parameter", _registry.flagMatches("--output", "--output", "-o"), is(true));
    }
    
    @Test
    public void flag_matches_should_be_false_on_mismatch() {
        assertThat("flag matches should be false on mismatch", _registry.flagMatches("-f", "--output", "-o"), is(false));
    }

    @Test
    public void flag_matches_should_be_false_when_a_hyphen_is_not_found_for_the_first_character() {
        assertThat("flag matches should be false with invalid flag", _registry.flagMatches("f", "format", "f"), is(false));
    }

    @Test
    public void ensure_multiple_calls_to_has_errors_does_not_accumulate_errors() {
        _registry.hasErrors();
        int initialCount = _registry.getErrors().size();
        _registry.hasErrors();
        int finalCount = _registry.getErrors().size();
        assertThat("error count should be the same", finalCount, is(equalTo(initialCount)));
    }

    @Test
    public void reset_should_set_values_to_default() throws InterruptedException {
        _registry.hasErrors(); // force errors

        _registry.addFeaturePath(".");
        _registry.addFilterTag("abc");
        _registry.addFormat("abc");
        _registry.addGluePath("abc");

        _registry.setOutputPath("abc");
        _registry.setDotCucumber("abc");

        _registry.setHelpRequired(true);
        _registry.setVersionRequested(true);
        _registry.setDryRun(true);

        _registry.reset();

        assertThat("no errors should be present", _registry.getErrors().size(), is(equalTo(0)));

        assertThat("feature paths should be empty", _registry.getFeaturePaths().isEmpty(), is(true));
        assertThat("filter tag should be empty", _registry.getFilterTags().size(), is(equalTo(0)));
        assertThat("formats should be empty", _registry.getFormats().size(), is(equalTo(0)));
        assertThat("glue paths should be empty", _registry.getGluePaths().size(), is(equalTo(0)));

        assertThat("dry run should be disabled", _registry.isDryRun(), is(false));
        assertThat("help should be reset", _registry.isHelpRequested(), is(false));
        assertThat("version should be reset", _registry.isVersionRequested(), is(false));

        assertThat("output path should be empty",  _registry.getOutputPath(), is(equalTo("")));
        assertThat("dot cucumber should be empty", _registry.getDotCucumber(), is(equalTo("")));
    }
}
