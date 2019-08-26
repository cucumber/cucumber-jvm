package cucumber.runtime.groovy;

import groovy.lang.Closure;
import org.codehaus.groovy.runtime.MethodClosure;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class GroovyStackTraceTest {
    GroovyStepDefinition groovyStepDefinition;

    @Before
    public void setUp() throws Throwable {
        Closure body = new MethodClosure("the owner", "length");
        groovyStepDefinition = new GroovyStepDefinition(null, 0, body, null, new ExceptionThrowingBackend());
    }

    @Test
    public void should_sanitize_stacktrace() throws Throwable {
        try {
            groovyStepDefinition.execute(null, new Object[0]);
            fail("step definition didn't throw an exception");
        } catch(Throwable thrown) {
            for (StackTraceElement stackTraceElement : thrown.getStackTrace()) {
                // if there are none of these, pretty good chance it's cleaned up the stack trace
                assertFalse("Stack trace has internal groovy callsite elements", stackTraceElement.getClassName().startsWith("org.codehaus.groovy.runtime.callsite"));
            }
        }

    }

    private static class ExceptionThrowingBackend extends GroovyBackend {
        public ExceptionThrowingBackend() {
            super(null);
        }

        @Override
        public void invoke(Closure body, Object[] args) throws Throwable {
            throw new ExceptionThrowingThing().returnGroovyException();
        }
    }
}
