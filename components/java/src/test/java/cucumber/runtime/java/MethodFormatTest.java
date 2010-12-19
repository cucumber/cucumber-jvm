package cucumber.runtime.java;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MethodFormatTest {
    private Method methodWithArgsAndException;
    private Method methodWithoutArgs;

    public void methodWithoutArgs() {
    }

    public List methodWithArgsAndException(String foo, Map bar) throws IllegalArgumentException, IOException {
        return null;
    }

    @Before
    public void lookupMethod() throws NoSuchMethodException {
        this.methodWithoutArgs = this.getClass().getMethod("methodWithoutArgs");
        this.methodWithArgsAndException = this.getClass().getMethod("methodWithArgsAndException", String.class, Map.class);
    }

    @Test
    public void shouldUseSimpleFormatWhenMethodHasException() {
        assertEquals("MethodFormatTest.methodWithArgsAndException(String,Map)", new MethodFormat("%c.%m(%a)").format(methodWithArgsAndException));
    }

    @Test
    public void shouldUseSimpleFormatWhenMethodHasNoException() {
        assertEquals("MethodFormatTest.methodWithoutArgs()", new MethodFormat("%c.%m(%a)").format(methodWithoutArgs));
    }
}
