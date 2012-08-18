package cucumber.runtime;

import gherkin.formatter.Reporter;
import gherkin.formatter.model.Result;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ScenarioResultTest {

    private Reporter reporter = mock(Reporter.class);
    private ScenarioResultImpl r = new ScenarioResultImpl(reporter);

    @Test
    public void no_steps_is_passed() throws Exception {
        assertEquals("passed", r.getStatus());
    }

    @Test
    public void passed_and_failed_is_passed() throws Exception {
        r.add(new Result("passed", 0L, null, null));
        r.add(new Result("failed", 0L, null, null));
        assertEquals("failed", r.getStatus());
    }

    @Test
    public void passed_and_skipped_is_skipped_although_we_cant_have_skipped_without_undefined_or_pending() throws Exception {
        r.add(new Result("passed", 0L, null, null));
        r.add(new Result("skipped", 0L, null, null));
        assertEquals("skipped", r.getStatus());
    }

    @Test
    public void undefined_and_pending_is_pending() throws Exception {
        r.add(new Result("undefined", 0L, null, null));
        r.add(new Result("pending", 0L, null, null));
        assertEquals("pending", r.getStatus());
    }

    @Test
    public void embeds_data() {
        byte[] data = new byte[]{1, 2, 3};
        r.embed(data, "bytes/foo");
        verify(reporter).embedding("bytes/foo", data);
    }

    @Test
    public void prints_output() {
        r.write("Hi");
        verify(reporter).write("Hi");
    }
}
