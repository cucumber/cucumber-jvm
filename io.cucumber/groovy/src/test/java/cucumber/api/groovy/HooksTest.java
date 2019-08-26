package cucumber.api.groovy;

import cucumber.runtime.CucumberException;
import org.codehaus.groovy.runtime.MethodClosure;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class HooksTest {

    @Test
    public void only_allows_arguments_string_long_integer_closure() {
        try {
            Hooks.Before("TAG", 10L, 100, new MethodClosure(this, "dummyClosureCall"), 0.0);
            fail("CucumberException was not thrown");
        } catch (CucumberException e) {
            assertEquals("An argument of the type java.lang.Double found, Before only allows the argument types " +
                         "String - Tag, Long - timeout, Integer - order, and Closure",
                         e.getMessage());
        }
    }

    @Test
    public void only_allows_one_timeout_argument() {
        try {
            Hooks.Before(1L, 2L);
            fail("CucumberException was not thrown");
        } catch (CucumberException e) {
            assertEquals("Two timeout (Long) arguments found; 1, and; 2", e.getMessage());
        }
    }

    @Test
    public void only_allows_one_order_argument() {
        try {
            Hooks.Before(1, 2);
            fail("CucumberException was not thrown");
        } catch (CucumberException e) {
            assertEquals("Two order (Integer) arguments found; 1, and; 2", e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    private void dummyClosureCall() {
    }
}
