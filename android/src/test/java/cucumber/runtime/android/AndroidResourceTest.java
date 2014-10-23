package cucumber.runtime.android;

import android.content.Context;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 16, manifest = Config.NONE)
public class AndroidResourceTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();
    
    private final Context context = Robolectric.application;

    @Test
    public void getPath_returns_given_path() {

        // given
        final String path = "some/path.feature";
        final AndroidResource androidResource = new AndroidResource(context, path);

        // when
        final String result = androidResource.getPath();

        // then
        assertThat(result, is(path));
    }

    @Test
    public void getAbsolutePath_returns_given_path() {

        // given
        final String path = "some/path.feature";
        final AndroidResource androidResource = new AndroidResource(context, path);

        // when
        final String result = androidResource.getAbsolutePath();

        // then
        assertThat(result, is(path));
    }

    @Test
    public void toString_outputs_the_path() {

        // given
        final String path = "some/path.feature";
        final AndroidResource androidResource = new AndroidResource(context, path);

        // when
        final String result = androidResource.toString();

        // then
        assertThat(result, containsString(path));
    }
}