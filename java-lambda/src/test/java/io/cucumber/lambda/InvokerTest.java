package io.cucumber.lambda;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.CucumberInvocationTargetException;
import io.cucumber.core.backend.Located;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.lang.reflect.Method;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class InvokerTest {

    final Located located = new Located() {
        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getLocation() {
            return "io.cucumber.lambda.InvokerTest.example(InvokerTest.java:1)";
        }
    };

    final Function<Object, Object> function = o -> o;

    final Method functionApply = getApplyMethod(function);

    @Test
    void invokes() {
        Object result = assertDoesNotThrow(() -> Invoker.invoke(located, function, functionApply, "Hello"));
        assertThat(result).isEqualTo("Hello");
    }

    @Test
    void complainsAboutArguments() {
        Executable executable = () -> Invoker.invoke(located, function, functionApply);
        CucumberBackendException exception = assertThrows(CucumberBackendException.class, executable);
        assertThat(exception).hasMessage("Failed to invoke io.cucumber.lambda.InvokerTest.example(InvokerTest.java:1)");
    }

    @Test
    void complainsAboutFailureInMethod() {
        RuntimeException runtimeException = new RuntimeException("Oops");
        Function<Object, Object> failingFunction = (Object ignored) -> {
            throw runtimeException;
        };
        Executable executable = () -> Invoker.invoke(located, failingFunction, getApplyMethod(failingFunction), "Hello");
        CucumberInvocationTargetException exception = assertThrows(CucumberInvocationTargetException.class, executable);
        assertThat(exception.getInvocationTargetExceptionCause()).isSameAs(runtimeException);
    }

    private static Method getApplyMethod(Function<?, ?> function) {
        try {
            return function.getClass().getMethod("apply", Object.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
