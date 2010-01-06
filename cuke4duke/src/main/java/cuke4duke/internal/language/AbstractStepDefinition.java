package cuke4duke.internal.language;

import org.jruby.RubyArray;


public abstract class AbstractStepDefinition implements StepDefinition {
    private final AbstractProgrammingLanguage programmingLanguage;

    public AbstractStepDefinition(AbstractProgrammingLanguage programmingLanguage) {
        this.programmingLanguage = programmingLanguage;
    }

    protected void register() throws Throwable {
        programmingLanguage.availableStepDefinition(regexp_source(), file_colon_line());
    }

    public final void invoke(RubyArray rubyArgs) throws Throwable {
        programmingLanguage.invoked(regexp_source(), file_colon_line());
        invokeWithArgs(rubyArgs.toArray());
    }

    public abstract Object invokeWithArgs(Object[] args) throws Throwable;
}
