package cucumber.runtime;

import cucumber.api.Result;
import cucumber.api.event.EmbedEvent;
import cucumber.api.event.WriteEvent;
import cucumber.runner.EventBus;
import gherkin.events.PickleEvent;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.argThat;

public class ScenarioResultTest {

    private EventBus bus = mock(EventBus.class);
    private ScenarioImpl s = new ScenarioImpl(bus, pickleEvent());

    @Test
    public void no_steps_is_passed() throws Exception {
        assertEquals(Result.Type.PASSED, s.getStatus());
    }

    @Test
    public void passed_failed_pending_undefined_skipped_is_failed() throws Exception {
        s.add(new Result(Result.Type.PASSED, 0L, null));
        s.add(new Result(Result.Type.FAILED, 0L, null));
        s.add(new Result(Result.Type.PENDING, 0L, null));
        s.add(new Result(Result.Type.UNDEFINED, 0L, null));
        s.add(new Result(Result.Type.SKIPPED, 0L, null));
        assertEquals(Result.Type.FAILED, s.getStatus());
        assertTrue(s.isFailed());
    }

    @Test
    public void passed_and_skipped_is_skipped_although_we_cant_have_skipped_without_undefined_or_pending() throws Exception {
        s.add(new Result(Result.Type.PASSED, 0L, null));
        s.add(new Result(Result.Type.SKIPPED, 0L, null));
        assertEquals(Result.Type.SKIPPED, s.getStatus());
        assertFalse(s.isFailed());
    }

    @Test
    public void passed_pending_undefined_skipped_is_pending() throws Exception {
        s.add(new Result(Result.Type.PASSED, 0L, null));
        s.add(new Result(Result.Type.UNDEFINED, 0L, null));
        s.add(new Result(Result.Type.PENDING, 0L, null));
        s.add(new Result(Result.Type.SKIPPED, 0L, null));
        assertEquals(Result.Type.UNDEFINED, s.getStatus());
        assertFalse(s.isFailed());
    }

    @Test
    public void passed_undefined_skipped_is_undefined() throws Exception {
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

    private PickleEvent pickleEvent() {
        Pickle pickle = mock(Pickle.class);
        when(pickle.getLocations()).thenReturn(asList(new PickleLocation(1, 1)));
        PickleEvent pickleEvent = new PickleEvent("uri", pickle);
        return pickleEvent;
    }
}

class EmbedEventMatcher extends ArgumentMatcher<WriteEvent> {
    private byte[] data;
    private String mimeType;

    public EmbedEventMatcher(byte[] data, String mimeType) {
        this.data = data;
        this.mimeType = mimeType;
    }

    @Override
    public boolean matches(Object argument) {
        return (argument instanceof EmbedEvent &&
                ((EmbedEvent)argument).data.equals(data) && ((EmbedEvent)argument).mimeType.equals(mimeType));
    }
}

class WriteEventMatcher extends ArgumentMatcher<WriteEvent> {
    private String text;

    public WriteEventMatcher(String text) {
        this.text = text;
    }

    @Override
    public boolean matches(Object argument) {
        return (argument instanceof WriteEvent && ((WriteEvent)argument).text.equals(text));
    }
}
