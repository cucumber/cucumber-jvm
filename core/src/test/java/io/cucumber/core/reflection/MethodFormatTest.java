package io.cucumber.core.reflection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertTrue;

public class MethodFormatTest {

    private Method methodWithArgsAndException;
    private Method methodWithoutArgs;

    public void methodWithoutArgs() {
    }

    public List methodWithArgsAndException(String foo, Map bar) throws IllegalArgumentException {
        return null;
    }

    @BeforeEach
    public void lookupMethod() throws NoSuchMethodException {
        this.methodWithoutArgs = this.getClass().getMethod("methodWithoutArgs");
        this.methodWithArgsAndException = this.getClass().getMethod("methodWithArgsAndException", String.class, Map.class);
    }

    @Test
    public void shouldUseSimpleFormatWhenMethodHasException() {
        assertThat(MethodFormat.SHORT.format(methodWithArgsAndException), is(equalTo("MethodFormatTest.methodWithArgsAndException(String,Map)")));
    }

    @Test
    public void shouldUseSimpleFormatWhenMethodHasNoException() {
        assertThat(MethodFormat.SHORT.format(methodWithoutArgs), is(equalTo("MethodFormatTest.methodWithoutArgs()")));
    }

    @Test
    public void prints_code_source() {
        String format = MethodFormat.FULL.format(methodWithoutArgs);
        assertTrue(format.startsWith("io.cucumber.core.reflection.MethodFormatTest.methodWithoutArgs() in file:"));
    }

}
