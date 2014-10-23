package cucumber.runtime.android;

import android.os.Bundle;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ArgumentsTest {

    @Test
    public void handles_null_bundle_gracefully() {

        // given
        final Arguments arguments = new Arguments(null);

        // when
        final String cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, is(""));
    }

    @Test
    public void handles_empty_bundle_gracefully() {

        // given
        final Arguments arguments = new Arguments(new Bundle());

        // when
        final String cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, is(""));
    }

    @Test
    public void supports_glue_as_direct_bundle_argument() {

        // given
        final Bundle bundle = new Bundle();
        bundle.putString("glue", "glue/code/path");
        final Arguments arguments = new Arguments(bundle);

        // when
        final String cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, is("--glue glue/code/path"));
    }

    @Test
    public void supports_format_as_direct_bundle_argument() {

        // given
        final Bundle bundle = new Bundle();
        bundle.putString("format", "someFormat");
        final Arguments arguments = new Arguments(bundle);

        // when
        final String cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, is("--format someFormat"));
    }

    @Test
    public void supports_plugin_as_direct_bundle_argument() {

        // given
        final Bundle bundle = new Bundle();
        bundle.putString("plugin", "someFormat");
        final Arguments arguments = new Arguments(bundle);

        // when
        final String cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, is("--plugin someFormat"));
    }

    @Test
    public void supports_tags_as_direct_bundle_argument() {

        // given
        final Bundle bundle = new Bundle();
        bundle.putString("tags", "@someTag");
        final Arguments arguments = new Arguments(bundle);

        // when
        final String cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, is("--tags @someTag"));
    }

    @Test
    public void supports_name_as_direct_bundle_argument() {

        // given
        final Bundle bundle = new Bundle();
        bundle.putString("name", "someName");
        final Arguments arguments = new Arguments(bundle);

        // when
        final String cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, is("--name someName"));
    }

    @Test
    public void supports_dryRun_as_direct_bundle_argument() {

        // given
        final Bundle bundle = new Bundle();
        bundle.putString("dryRun", "true");
        final Arguments arguments = new Arguments(bundle);

        // when
        final String cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, is("--dry-run"));
    }

    @Test
    public void supports_log_as_alias_for_dryRun_as_direct_bundle_argument() {

        // given
        final Bundle bundle = new Bundle();
        bundle.putString("log", "true");
        final Arguments arguments = new Arguments(bundle);

        // when
        final String cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, is("--dry-run"));
    }

    @Test
    public void supports_noDryRun_as_direct_bundle_argument() {

        // given
        final Bundle bundle = new Bundle();
        bundle.putString("noDryRun", "true");
        final Arguments arguments = new Arguments(bundle);

        // when
        final String cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, is("--no-dry-run"));
    }

    @Test
    public void supports_monochrome_as_direct_bundle_argument() {

        // given
        final Bundle bundle = new Bundle();
        bundle.putString("monochrome", "true");
        final Arguments arguments = new Arguments(bundle);

        // when
        final String cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, is("--monochrome"));
    }

    @Test
    public void supports_noMonochrome_as_direct_bundle_argument() {

        // given
        final Bundle bundle = new Bundle();
        bundle.putString("noMonochrome", "true");
        final Arguments arguments = new Arguments(bundle);

        // when
        final String cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, is("--no-monochrome"));
    }

    @Test
    public void supports_strict_as_direct_bundle_argument() {

        // given
        final Bundle bundle = new Bundle();
        bundle.putString("strict", "true");
        final Arguments arguments = new Arguments(bundle);

        // when
        final String cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, is("--strict"));
    }

    @Test
    public void supports_noStrict_as_direct_bundle_argument() {

        // given
        final Bundle bundle = new Bundle();
        bundle.putString("noStrict", "true");
        final Arguments arguments = new Arguments(bundle);

        // when
        final String cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, is("--no-strict"));
    }

    @Test
    public void supports_snippets_as_direct_bundle_argument() {

        // given
        final Bundle bundle = new Bundle();
        bundle.putString("snippets", "someSnippet");
        final Arguments arguments = new Arguments(bundle);

        // when
        final String cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, is("--snippets someSnippet"));
    }

    @Test
    public void supports_features_as_direct_bundle_argument() {

        // given
        final Bundle bundle = new Bundle();
        bundle.putString("features", "someFeature");
        final Arguments arguments = new Arguments(bundle);

        // when
        final String cucumberOptions = arguments.getCucumberOptions();

        // then
        // TODO does this space makes sense?
        assertThat(cucumberOptions, is(" someFeature"));
    }

    @Test
    public void supports_multiple_values() {

        // given
        final Bundle bundle = new Bundle();
        bundle.putString("name", "Feature1--Feature2");
        final Arguments arguments = new Arguments(bundle);

        // when
        final String cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, is("--name Feature1 --name Feature2"));
    }

    @Test
    public void supports_single_cucumber_options_string() {

        // given
        final List<String> cucumberOptions = Lists.newArrayList("--tags @mytag",
                                                                "--monochrome",
                                                                "--name MyFeature",
                                                                "--dry-run",
                                                                "--glue com.someglue.Glue",
                                                                "--format pretty",
                                                                "--snippets underscore",
                                                                "--strict",
                                                                "--dotcucumber",
                                                                "test features");
        final Bundle bundle = new Bundle();
        bundle.putString("cucumberOptions", Joiner.on(" ").join(cucumberOptions));

        // when
        final Arguments arguments = new Arguments(bundle);

        // then
        for (final String cucumberOption : cucumberOptions) {
            assertThat(arguments.getCucumberOptions(), containsString(cucumberOption));
        }
    }

    @Test
    public void single_cucumber_options_string_takes_precedence_over_direct_bundle_argument() {

        // given
        final String cucumberOptions = "--tags @mytag1";
        final Bundle bundle = new Bundle();
        bundle.putString("cucumberOptions", cucumberOptions);
        bundle.putString("tags", "@mytag2");

        // when
        final Arguments arguments = new Arguments(bundle);

        // then
        assertThat(arguments.getCucumberOptions(), is(cucumberOptions));
    }

    @Test
    public void supports_spaces_in_values() {

        // given
        final Bundle bundle = new Bundle();
        bundle.putString("name", "'Name with spaces'");
        final Arguments arguments = new Arguments(bundle);

        // when
        final String cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, is("--name 'Name with spaces'"));
    }

    @Test
    public void isCountEnabled_returns_true_when_bundle_contains_true() {
        // given
        final Bundle bundle = spy(new Bundle());
        bundle.putString(Arguments.KEY.COUNT_ENABLED, "true");

        // when
        final Arguments arguments = new Arguments(bundle);

        // then
        assertThat(arguments.isCountEnabled(), is(true));
    }

    @Test
    public void isCountEnabled_returns_false_when_bundle_contains_false() {
        // given
        final Bundle bundle = spy(new Bundle());
        bundle.putString(Arguments.KEY.COUNT_ENABLED, "false");

        // when
        final Arguments arguments = new Arguments(bundle);

        // then
        assertThat(arguments.isCountEnabled(), is(false));
    }

    @Test
    public void isCountEnabled_returns_false_when_bundle_contains_no_value() {
        // given
        final Bundle bundle = spy(new Bundle());

        // when
        final Arguments arguments = new Arguments(bundle);

        // then
        assertThat(arguments.isCountEnabled(), is(false));
    }

    @Test
    public void isDebugEnabled_returns_true_when_bundle_contains_true() {
        // given
        final Bundle bundle = spy(new Bundle());
        bundle.putString(Arguments.KEY.DEBUG_ENABLED, "true");

        // when
        final Arguments arguments = new Arguments(bundle);

        // then
        assertThat(arguments.isDebugEnabled(), is(true));
    }

    @Test
    public void isDebugEnabled_returns_false_when_bundle_contains_false() {
        // given
        final Bundle bundle = spy(new Bundle());
        bundle.putString(Arguments.KEY.DEBUG_ENABLED, "false");

        // when
        final Arguments arguments = new Arguments(bundle);

        // then
        assertThat(arguments.isDebugEnabled(), is(false));
    }

    @Test
    public void isDebugEnabled_returns_false_when_bundle_contains_no_value() {
        // given
        final Bundle bundle = spy(new Bundle());

        // when
        final Arguments arguments = new Arguments(bundle);

        // then
        assertThat(arguments.isDebugEnabled(), is(false));
    }

    @Test
    public void coverageDataFilePath_returns_value_when_bundle_contains_value() {
        // given
        final String fileName = "some_custome_file.name";
        final Bundle bundle = spy(new Bundle());
        bundle.putString(Arguments.KEY.COVERAGE_DATA_FILE_PATH, fileName);

        // when
        final Arguments arguments = new Arguments(bundle);

        // then
        assertThat(arguments.coverageDataFilePath(), is(fileName));
    }

    @Test
    public void coverageDataFilePath_returns_default_value_when_bundle_contains_no_value() {
        // given
        final Bundle bundle = spy(new Bundle());

        // when
        final Arguments arguments = new Arguments(bundle);

        // then
        assertThat(arguments.coverageDataFilePath(), is(Arguments.DEFAULT.COVERAGE_DATA_FILE_PATH));
    }

    @Test
    public void isCoverageEnabled_returns_true_when_bundle_contains_true() {
        // given
        final Bundle bundle = spy(new Bundle());
        bundle.putString(Arguments.KEY.COVERAGE_ENABLED, "true");

        // when
        final Arguments arguments = new Arguments(bundle);

        // then
        assertThat(arguments.isCoverageEnabled(), is(true));
    }

    @Test
    public void isCoverageEnabled_returns_false_when_bundle_contains_false() {
        // given
        final Bundle bundle = spy(new Bundle());
        bundle.putString(Arguments.KEY.COVERAGE_ENABLED, "false");

        // when
        final Arguments arguments = new Arguments(bundle);

        // then
        assertThat(arguments.isCoverageEnabled(), is(false));
    }

    @Test
    public void isCoverageEnabled_returns_false_when_bundle_contains_no_value() {
        // given
        final Bundle bundle = spy(new Bundle());

        // when
        final Arguments arguments = new Arguments(bundle);

        // then
        assertThat(arguments.isCoverageEnabled(), is(false));
    }
}
