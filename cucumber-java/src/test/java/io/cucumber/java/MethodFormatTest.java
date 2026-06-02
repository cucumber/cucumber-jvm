package io.cucumber.java;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class MethodFormatTest {

    private Method methodWithArgsAndException;
    private Method methodWithoutArgs;
    private Method packagePrivateMethod;
    private Method protectedMethod;

    public void methodWithoutArgs() {
    }

    public @Nullable List<?> methodWithArgsAndException(String foo, Map<?, ?> bar) throws IllegalArgumentException {
        return null;
    }

    void packagePrivateMethod() {

    }

    protected void protectedMethod() {

    }

    @BeforeEach
    void lookupMethod() throws NoSuchMethodException {
        this.methodWithoutArgs = this.getClass().getMethod("methodWithoutArgs");
        this.methodWithArgsAndException = this.getClass().getMethod("methodWithArgsAndException", String.class,
            Map.class);
        this.packagePrivateMethod = this.getClass().getDeclaredMethod("packagePrivateMethod");
        this.protectedMethod = this.getClass().getDeclaredMethod("protectedMethod");
    }

    @Test
    void formatPublicMethod() {
        assertThat(MethodFormat.FULL.format(methodWithoutArgs),
            equalTo("io.cucumber.java.MethodFormatTest.methodWithoutArgs()"));
    }

    @Test
    void formatPackagePrivateMethod() {
        assertThat(MethodFormat.FULL.format(packagePrivateMethod),
            equalTo("io.cucumber.java.MethodFormatTest.packagePrivateMethod()"));
    }

    @Test
    void formatProtectedMethod() {
        assertThat(MethodFormat.FULL.format(protectedMethod),
            equalTo("io.cucumber.java.MethodFormatTest.protectedMethod()"));
    }

    @Test
    void formatMethodWithExceptionsAndArguments() {
        assertThat(MethodFormat.FULL.format(methodWithArgsAndException),
            equalTo(
                "io.cucumber.java.MethodFormatTest.methodWithArgsAndException(java.lang.String,java.util.Map<?, ?>)"));
    }

}
