package io.cucumber.core.runner;

import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.SourceReference;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.backend.TypeResolver;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StubStepDefinition implements StepDefinition {

    private final List<ParameterInfo> parameterInfos;
    private final String expression;
    private final boolean transposed;

    private List<Object> args;

    StubStepDefinition(String pattern, Type... types) {
        this(pattern, false, types);
    }

    StubStepDefinition(String pattern, boolean transposed, Type... types) {
        this.parameterInfos = Stream.of(types).map(StubParameterInfo::new).collect(Collectors.toList());
        this.expression = pattern;
        this.transposed = transposed;
    }

    @Override
    public void execute(Object[] args) {
        assertEquals(parameterInfos.size(), args.length);
        this.args = Arrays.asList(args);
    }

    @Override
    public List<ParameterInfo> parameterInfos() {
        return parameterInfos;
    }

    @Override
    public String getPattern() {
        return expression;
    }

    public List<Object> getArgs() {
        return args;
    }

    @Override
    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return false;
    }

    @Override
    public String getLocation() {
        return "{stubbed location with details}";
    }

    private final class StubParameterInfo implements ParameterInfo {

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
            return transposed;
        }

        @Override
        public TypeResolver getTypeResolver() {
            return () -> type;
        }

    }

    @Override
    public Optional<SourceReference> getSourceReference() {
        try {
            Method method = getClass().getMethod("getSourceReference");
            return Optional.of(SourceReference.fromMethod(method));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

}
