package cuke4duke.internal.language;

import cuke4duke.internal.ArgumentsConverter;
import org.jruby.RubyArray;

public abstract class AbstractStepDefinition implements StepDefinition {
    private final ArgumentsConverter argumentsConverter = new ArgumentsConverter();
    private final ProgrammingLanguage programmingLanguage;

    public AbstractStepDefinition(ProgrammingLanguage programmingLanguage) {
        this.programmingLanguage = programmingLanguage;
    }

    protected void register() throws Throwable {
        programmingLanguage.availableStepDefinition(regexp_source(), file_colon_line());
    }

    public final void invoke(RubyArray rubyArgs) throws Throwable {
        programmingLanguage.invokedStepDefinition(regexp_source(), file_colon_line());
        Object[] args = rubyArgs.toArray();
        Object[] javaArgs = argumentsConverter.convert(getParameterTypes(args), args);
        invokeWithJavaArgs(javaArgs);
    }

    protected abstract Class<?>[] getParameterTypes(Object[] args);

    public abstract void invokeWithJavaArgs(Object[] args) throws Throwable;
}
