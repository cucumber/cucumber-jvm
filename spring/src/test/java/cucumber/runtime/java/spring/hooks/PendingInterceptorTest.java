package cucumber.runtime.java.spring.hooks;

import org.junit.Test;

import cucumber.runtime.PendingException;
import cucumber.runtime.java.ObjectFactory;
import cucumber.runtime.java.spring.SpringFactory;

import static org.junit.Assert.assertTrue;

public class PendingInterceptorTest {

    @Test
    public void pendingBellyShouldThrowPendingException() {
        final ObjectFactory factory = new SpringFactory();
        Belly belly = factory.getInstance(Belly.class);
        boolean pendingExceptionThrown = false;
        try {
          belly.getCukes();
        } catch (PendingException pe){
          assertTrue(pe.getMessage().contains(PendingBelly.class.getName()));
          assertTrue(pe.getMessage().contains("getCukes"));
          assertTrue(pe.getMessage().contains(PendingBelly.NO_BELLY));
          pendingExceptionThrown = true;
        }
        assertTrue(pendingExceptionThrown);
    }
}
