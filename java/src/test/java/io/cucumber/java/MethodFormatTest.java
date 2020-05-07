package io.cucumber.java;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

class MethodFormatTest {

    private Method methodWithArgsAndException;
    private Method methodWithoutArgs;

    public void methodWithoutArgs() {
    }

    public List methodWithArgsAndException(String foo, Map bar) throws IllegalArgumentException {
        return null;
    }

    @BeforeEach
    void lookupMethod() throws NoSuchMethodException {
        this.methodWithoutArgs = this.getClass().getMethod("methodWithoutArgs");
        this.methodWithArgsAndException = this.getClass().getMethod("methodWithArgsAndException", String.class,
            Map.class);
    }

    @Test
    void shouldUseSimpleFormatWhenMethodHasException() {
        assertThat(MethodFormat.FULL.format(methodWithoutArgs),
            startsWith("io.cucumber.java.MethodFormatTest.methodWithoutArgs()"));
    }

    @Test
    void shouldUseSimpleFormatWhenMethodHasNoException() {
        assertThat(MethodFormat.FULL.format(methodWithArgsAndException),
            startsWith("io.cucumber.java.MethodFormatTest.methodWithArgsAndException(java.lang.String,java.util.Map)"));
    }

}
