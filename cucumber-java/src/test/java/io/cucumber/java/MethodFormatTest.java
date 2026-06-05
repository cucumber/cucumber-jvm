package io.cucumber.java;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MethodFormatTest {

    private Method methodWithArgsAndException;
    private Method methodWithoutArgs;
    private Method packagePrivateMethod;
    private Method protectedMethod;
    private Method privateMethod;

    public void methodWithoutArgs() {
    }

    public @Nullable List<?> methodWithArgsAndException(String foo, Map<?, ?> bar) throws IllegalArgumentException {
        return null;
    }

    void packagePrivateMethod() {

    }

    protected void protectedMethod() {

    }

    protected void privateMethod() {

    }

    @BeforeEach
    void lookupMethod() throws NoSuchMethodException {
        this.methodWithoutArgs = this.getClass().getMethod("methodWithoutArgs");
        this.methodWithArgsAndException = this.getClass().getMethod("methodWithArgsAndException", String.class,
            Map.class);
        this.packagePrivateMethod = this.getClass().getDeclaredMethod("packagePrivateMethod");
        this.protectedMethod = this.getClass().getDeclaredMethod("protectedMethod");
        this.privateMethod = this.getClass().getDeclaredMethod("privateMethod");
    }

    @Test
    void formatPublicMethod() {
        assertThat(MethodFormat.FULL.format(methodWithoutArgs))
                .isEqualTo("io.cucumber.java.MethodFormatTest.methodWithoutArgs()");
    }

    @Test
    void formatPackagePrivateMethod() {
        assertThat(MethodFormat.FULL.format(packagePrivateMethod))
                .isEqualTo("io.cucumber.java.MethodFormatTest.packagePrivateMethod()");
    }

    @Test
    void formatProtectedMethod() {
        assertThat(MethodFormat.FULL.format(protectedMethod))
                .isEqualTo("io.cucumber.java.MethodFormatTest.protectedMethod()");
    }

    @Test
    void formatPrivateMethod() {
        assertThat(MethodFormat.FULL.format(privateMethod))
                .isEqualTo("io.cucumber.java.MethodFormatTest.privateMethod()");
    }

    @Test
    void formatMethodWithExceptionsAndArguments() {
        assertThat(MethodFormat.FULL.format(methodWithArgsAndException))
                .isEqualTo(
                    "io.cucumber.java.MethodFormatTest.methodWithArgsAndException(java.lang.String,java.util.Map<?, ?>)");
    }

}
