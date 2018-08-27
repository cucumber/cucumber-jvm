package cucumber.runner;

import cucumber.api.Result;
import cucumber.api.event.EmbedEvent;
import cucumber.api.event.WriteEvent;
import gherkin.events.PickleEvent;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ScenarioResultTest {

    private EventBus bus = mock(EventBus.class);
    private Scenario s = new Scenario(
        bus,
        new TestCase(
            Collections.<PickleStepTestStep>emptyList(),
            Collections.<HookTestStep>emptyList(),
            Collections.<HookTestStep>emptyList(),
            mock(PickleEvent.class),
            false
        )
    );

    @Test
    public void no_steps_is_undefined() {
        assertEquals(Result.Type.UNDEFINED, s.getStatus());
    }

    @Test
    public void one_passed_step_is_passed() {
        s.add(new Result(Result.Type.PASSED, 0L, null));
        assertEquals(Result.Type.PASSED, s.getStatus());
    }

    @Test
    public void passed_failed_pending_undefined_skipped_is_failed() {
        s.add(new Result(Result.Type.PASSED, 0L, null));
        s.add(new Result(Result.Type.FAILED, 0L, null));
        s.add(new Result(Result.Type.PENDING, 0L, null));
        s.add(new Result(Result.Type.UNDEFINED, 0L, null));
        s.add(new Result(Result.Type.SKIPPED, 0L, null));
        assertEquals(Result.Type.FAILED, s.getStatus());
        assertTrue(s.isFailed());
    }

    @Test
    public void passed_and_skipped_is_skipped_although_we_cant_have_skipped_without_undefined_or_pending() {
        s.add(new Result(Result.Type.PASSED, 0L, null));
        s.add(new Result(Result.Type.SKIPPED, 0L, null));
        assertEquals(Result.Type.SKIPPED, s.getStatus());
        assertFalse(s.isFailed());
    }

    @Test
    public void passed_pending_undefined_skipped_is_pending() {
        s.add(new Result(Result.Type.PASSED, 0L, null));
        s.add(new Result(Result.Type.UNDEFINED, 0L, null));
        s.add(new Result(Result.Type.PENDING, 0L, null));
        s.add(new Result(Result.Type.SKIPPED, 0L, null));
        assertEquals(Result.Type.UNDEFINED, s.getStatus());
        assertFalse(s.isFailed());
    }

    @Test
    public void passed_undefined_skipped_is_undefined() {
        s.add(new Result(Result.Type.PASSED, 0L, null));
        s.add(new Result(Result.Type.UNDEFINED, 0L, null));
        s.add(new Result(Result.Type.SKIPPED, 0L, null));
        assertEquals(Result.Type.UNDEFINED, s.getStatus());
        assertFalse(s.isFailed());
    }

    @Test
    public void embeds_data() {
        byte[] data = new byte[]{1, 2, 3};
        s.embed(data, "bytes/foo");
        verify(bus).send(argThat(new EmbedEventMatcher(data, "bytes/foo")));
    }

    @Test
    public void prints_output() {
        s.write("Hi");
        verify(bus).send(argThat(new WriteEventMatcher("Hi")));
    }

    @Test
    public void failed_followed_by_pending_yields_failed_error() {
        Throwable failedError = mock(Throwable.class);
        Throwable pendingError = mock(Throwable.class);

        s.add(new Result(Result.Type.FAILED, 0L, failedError));
        s.add(new Result(Result.Type.PENDING, 0L, pendingError));

        assertThat(s.getError(), sameInstance(failedError));
    }

    @Test
    public void pending_followed_by_failed_yields_failed_error() {
        Throwable pendingError = mock(Throwable.class);
        Throwable failedError = mock(Throwable.class);

        s.add(new Result(Result.Type.PENDING, 0L, pendingError));
        s.add(new Result(Result.Type.FAILED, 0L, failedError));

        assertThat(s.getError(), sameInstance(failedError));
    }

    private final class EmbedEventMatcher implements ArgumentMatcher<EmbedEvent> {
        private byte[] data;
        private String mimeType;

        EmbedEventMatcher(byte[] data, String mimeType) {
            this.data = data;
            this.mimeType = mimeType;
        }

        @Override
        public boolean matches(EmbedEvent argument) {
            return (argument != null &&
                Arrays.equals(argument.data, data) && argument.mimeType.equals(mimeType));
        }
    }

    private final class WriteEventMatcher implements ArgumentMatcher<WriteEvent> {
        private String text;

        WriteEventMatcher(String text) {
            this.text = text;
        }

        @Override
        public boolean matches(WriteEvent argument) {
            return (argument != null && argument.text.equals(text));
        }
    }
}