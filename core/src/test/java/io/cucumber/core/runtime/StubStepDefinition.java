package io.cucumber.core.runtime;

import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.backend.TypeResolver;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StubStepDefinition implements StepDefinition {
    private final List<ParameterInfo> parameterInfos;
    private final String expression;
    private final RuntimeException exception;

    public StubStepDefinition(String pattern, Type... types) {
        this(pattern, null, types);
    }

    public StubStepDefinition(String pattern, RuntimeException exception, Type... types) {
        this.parameterInfos = Stream.of(types).map(StubParameterInfo::new).collect(Collectors.toList());
        this.expression = pattern;
        this.exception = exception;
    }

    @Override
    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return false;
    }

    @Override
    public String getLocation() {
        return "{stubbed location with details}";
    }

    @Override
    public void execute(Object[] args) {
        if (exception != null) {
            throw exception;
        }

        assertEquals(parameterInfos.size(), args.length);
        for (int i = 0; i < args.length; i++) {
            assertEquals(parameterInfos.get(i).getType(), args[i].getClass());
        }
    }

    @Override
    public List<ParameterInfo> parameterInfos() {
        return parameterInfos;
    }

    @Override
    public String getPattern() {
        return expression;
    }

    private static final class StubParameterInfo implements ParameterInfo {

        private final Type type;

        private StubParameterInfo(Type type) {
            this.type = type;
        }

        @Override
        public Type getType() {
            return type;
        }

        @Override
        public boolean isTransposed() {
            return false;
        }

        @Override
        public TypeResolver getTypeResolver() {
            return () -> type;
        }
    }

}
