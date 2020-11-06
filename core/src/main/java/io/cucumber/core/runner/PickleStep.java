package io.cucumber.core.runner;

import io.cucumber.plugin.event.Argument;

class PickleStep implements io.cucumber.core.backend.PickleStep {

    private final Keyword keyword;

    private final Object[] arguments;

    public PickleStep(PickleStepTestStep pickleStepTestStep) {
        this.keyword = Keyword.valueOf(pickleStepTestStep.getStep().getGwtType().name());
        this.arguments = pickleStepTestStep.getDefinitionArgument().stream().map(Argument::getValue).toArray();
    }

    @Override
    public Keyword getKeyword() {
        return keyword;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }
}
