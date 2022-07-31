package io.cucumber.core.backend;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StubStepDefinition implements StepDefinition {

    private static final String STUBBED_LOCATION_WITH_DETAILS = "{stubbed location with details}";
    private final List<ParameterInfo> parameterInfos;
    private final String expression;
    private final RuntimeException exception;
    private final String location;

    public StubStepDefinition(String pattern, String location, Type... types) {
        this(pattern, location, null, types);
    }

    public StubStepDefinition(String pattern, Type... types) {
        this(pattern, STUBBED_LOCATION_WITH_DETAILS, null, types);
    }

    public StubStepDefinition(String pattern, RuntimeException exception, Type... types) {
        this(pattern, STUBBED_LOCATION_WITH_DETAILS, exception, types);
    }

    public StubStepDefinition(String pattern, String location, RuntimeException exception, Type... types) {
        this.parameterInfos = Stream.of(types).map(StubParameterInfo::new).collect(Collectors.toList());
        this.expression = pattern;
        this.location = location;
        this.exception = exception;
    }

    @Override
    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return false;
    }

    @Override
    public String getLocation() {
        return location;
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
