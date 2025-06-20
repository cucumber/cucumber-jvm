package io.cucumber.core.backend;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StubStepDefinition implements StepDefinition {

    private static final String STUBBED_LOCATION_WITH_DETAILS = "{stubbed location with details}";
    private final List<ParameterInfo> parameterInfos;
    private final String expression;
    private final Throwable exception;
    private final Located location;
    private SourceReference sourceReference;

    public StubStepDefinition(String pattern, String location, Type... types) {
        this(pattern, location, null, types);
    }

    public StubStepDefinition(String pattern, Type... types) {
        this(pattern, STUBBED_LOCATION_WITH_DETAILS, null, types);
    }

    public StubStepDefinition(String pattern, Throwable exception, Type... types) {
        this(pattern, STUBBED_LOCATION_WITH_DETAILS, exception, types);
    }

    public StubStepDefinition(String pattern, String location, Throwable exception, Type... types) {
        this.parameterInfos = Stream.of(types).map(StubParameterInfo::new).collect(Collectors.toList());
        this.expression = pattern;
        this.location = new StubLocation(location);
        this.exception = exception;
    }

    public StubStepDefinition(String pattern, SourceReference sourceReference, Type... types) {
        this(pattern, sourceReference, null, types);
    }

    public StubStepDefinition(String pattern, SourceReference sourceReference, Throwable exception, Type... types) {
        this.parameterInfos = Stream.of(types).map(StubParameterInfo::new).collect(Collectors.toList());
        this.expression = pattern;
        this.location = new StubLocation("");
        this.sourceReference = sourceReference;
        this.exception = exception;
    }

    @Override
    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return false;
    }

    @Override
    public String getLocation() {
        return location.getLocation();
    }

    @Override
    public void execute(Object[] args) {
        if (exception != null) {
            if (exception instanceof CucumberBackendException) {
                throw (CucumberBackendException) exception;
            }
            throw new CucumberInvocationTargetException(location, new InvocationTargetException(exception));
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

    @Override
    public Optional<SourceReference> getSourceReference() {
        return Optional.ofNullable(sourceReference);
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
