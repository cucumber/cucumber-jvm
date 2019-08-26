package cucumber.runtime.android;

import android.app.Instrumentation;
import android.os.Bundle;
import com.vladium.emma.rt.RT;
import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 16, manifest = Config.NONE)
public class CoverageDumperTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final Bundle bundle = mock(Bundle.class);
    private final Arguments arguments = mock(Arguments.class);
    private final CoverageDumper coverageDumper = new CoverageDumper(arguments);

    @Before
    public void beforeEach() {
        RT.resetMock();
    }

    @Test
    public void does_not_dump_when_flag_is_disabled() {

        // given
        when(arguments.isCoverageEnabled()).thenReturn(false);

        // when
        coverageDumper.requestDump(bundle);

        // then
        verifyZeroInteractions(bundle);
    }

    @Test
    public void dumps_file_when_flag_is_enabled() throws IOException {

        // given
        final String fileName = temporaryFolder.getRoot().getAbsolutePath() + File.separator + "foo.bar";
        when(arguments.isCoverageEnabled()).thenReturn(true);
        when(arguments.coverageDataFilePath()).thenReturn(fileName);

        // when
        coverageDumper.requestDump(bundle);

        // then
        assertThat(new File(fileName).exists(), is(true));
    }

    @Test
    public void puts_path_to_coverage_file_into_bundle() throws IOException {

        // given
        final String fileName = temporaryFolder.getRoot().getAbsolutePath() + File.separator + "foo.bar";
        when(arguments.isCoverageEnabled()).thenReturn(true);
        when(arguments.coverageDataFilePath()).thenReturn(fileName);

        // when
        coverageDumper.requestDump(bundle);

        // then
        verify(bundle).putString("coverageFilePath", fileName);
    }

    @Test
    public void appends_message_about_dumped_coverage_data_to_result_stream() {

        // given
        final String fileName = temporaryFolder.getRoot().getAbsolutePath() + File.separator + "foo.bar";
        final String previousStream = "previous stream data";
        when(arguments.isCoverageEnabled()).thenReturn(true);
        when(arguments.coverageDataFilePath()).thenReturn(fileName);
        when(bundle.getString(Instrumentation.REPORT_KEY_STREAMRESULT)).thenReturn(previousStream);

        // when
        coverageDumper.requestDump(bundle);

        // then
        verify(bundle).putString(eq(Instrumentation.REPORT_KEY_STREAMRESULT), and(contains(previousStream), contains(fileName)));
    }

    @Test
    public void passes_file_for_specified_name_to_code_coverage_dumper_implementation() {

        // given
        final String fileName = temporaryFolder.getRoot().getAbsolutePath() + File.separator + "foo.bar";
        when(arguments.isCoverageEnabled()).thenReturn(true);
        when(arguments.coverageDataFilePath()).thenReturn(fileName);

        // when
        coverageDumper.requestDump(bundle);

        // then
        assertThat(RT.getLastFile().getAbsolutePath(), is(fileName));
    }


    @Test
    public void adds_error_message_to_result_stream_when_coverage_class_can_not_be_found() {

        // given
        final String fileName = temporaryFolder.getRoot().getAbsolutePath() + File.separator + "foo.bar";
        final String previousStream = "previous stream data";
        when(arguments.isCoverageEnabled()).thenReturn(true);
        when(arguments.coverageDataFilePath()).thenReturn(fileName);
        when(bundle.getString(Instrumentation.REPORT_KEY_STREAMRESULT)).thenReturn(previousStream);

        // when
        coverageDumper.requestDump(bundle);

        // then
        verify(bundle).putString(eq(Instrumentation.REPORT_KEY_STREAMRESULT), and(contains(previousStream), contains(fileName)));
    }

    @Test
    public void adds_error_message_to_result_stream_when_file_cannot_be_dumped() {

        // given
        final String fileName = temporaryFolder.getRoot().getAbsolutePath() + File.separator + "foo.bar";
        final String previousStream = "previous stream data";
        when(arguments.isCoverageEnabled()).thenReturn(true);
        when(arguments.coverageDataFilePath()).thenReturn(fileName);
        when(bundle.getString(Instrumentation.REPORT_KEY_STREAMRESULT)).thenReturn(previousStream);
        RT.throwOnNextInvocation(new RuntimeException("something terrible happened"));

        // when
        coverageDumper.requestDump(bundle);

        // then
        verify(bundle).putString(eq(Instrumentation.REPORT_KEY_STREAMRESULT), and(contains(previousStream), contains("Error: Failed to generate coverage. Check logcat for details.")));

    }
}
