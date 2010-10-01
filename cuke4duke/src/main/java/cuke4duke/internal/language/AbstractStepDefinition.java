package cuke4duke.internal.language;

import java.util.List;

public abstract class AbstractStepDefinition implements StepDefinition {
    private final AbstractProgrammingLanguage programmingLanguage;

    public AbstractStepDefinition(AbstractProgrammingLanguage programmingLanguage) {
        this.programmingLanguage = programmingLanguage;
    }

    protected void register() throws Throwable {
        programmingLanguage.availableStepDefinition(regexp_source(), file_colon_line());
    }

    public final void invoke(List<Object> arguments) throws Throwable {
        programmingLanguage.invoked(regexp_source(), file_colon_line());
        invokeWithArgs(arguments.toArray());
    }

    public abstract Object invokeWithArgs(Object[] args) throws Throwable;
}
