package cuke4duke.internal.language;

import org.jruby.RubyArray;
import cuke4duke.internal.ArgumentsConverter;

public abstract class AbstractStepDefinition implements StepDefinition {
    private final ArgumentsConverter argumentsConverter = new ArgumentsConverter();

    public final void invoke(RubyArray rubyArgs) throws Throwable {
        Object[] args = rubyArgs.toArray();
        Object[] javaArgs = argumentsConverter.convert(getParameterTypes(args), args);
        invokeWithJavaArgs(javaArgs);
    }

    protected abstract Class<?>[] getParameterTypes(Object[] args);

    public abstract void invokeWithJavaArgs(Object[] args) throws Throwable;
}
