package cucumber.runtime;

import gherkin.formatter.Reporter;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Tag;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ScenarioResultTest {

    private Reporter reporter = mock(Reporter.class);
    private ScenarioImpl s = new ScenarioImpl(reporter, Collections.<Tag>emptySet(), mock(Scenario.class));

    @Test
    public void no_steps_is_passed() throws Exception {
        assertEquals("passed", s.getStatus());
    }

    @Test
    public void passed_failed_pending_undefined_skipped_is_failed() throws Exception {
        s.add(new Result("passed", 0L, null, null));
        s.add(new Result("failed", 0L, null, null));
        s.add(new Result("pending", 0L, null, null));
        s.add(new Result("undefined", 0L, null, null));
        s.add(new Result("skipped", 0L, null, null));
        assertEquals("failed", s.getStatus());
    }

    @Test
    public void passed_and_skipped_is_skipped_although_we_cant_have_skipped_without_undefined_or_pending() throws Exception {
        s.add(new Result("passed", 0L, null, null));
        s.add(new Result("skipped", 0L, null, null));
        assertEquals("skipped", s.getStatus());
    }

    @Test
    public void passed_pending_undefined_skipped_is_pending() throws Exception {
        s.add(new Result("passed", 0L, null, null));
        s.add(new Result("undefined", 0L, null, null));
        s.add(new Result("pending", 0L, null, null));
        s.add(new Result("skipped", 0L, null, null));
        assertEquals("undefined", s.getStatus());
    }

    @Test
    public void passed_undefined_skipped_is_undefined() throws Exception {
        s.add(new Result("passed", 0L, null, null));
        s.add(new Result("undefined", 0L, null, null));
        s.add(new Result("skipped", 0L, null, null));
        assertEquals("undefined", s.getStatus());
    }

    @Test
    public void embeds_data() {
        byte[] data = new byte[]{1, 2, 3};
        s.embed(data, "bytes/foo");
        verify(reporter).embedding("bytes/foo", data);
    }

    @Test
    public void prints_output() {
        s.write("Hi");
        verify(reporter).write("Hi");
    }
}
