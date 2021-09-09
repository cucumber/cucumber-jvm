package io.cucumber.core.runner;

import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.StepDefinition;

import java.util.List;

final class NoStepDefinition implements StepDefinition {

    @Override
    public void execute(Object[] args) {
    }

    @Override
    public List<ParameterInfo> parameterInfos() {
        return null;
    }

    @Override
    public String getPattern() {
        return null;
    }

    @Override
    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return false;
    }

    @Override
    public String getLocation() {
        return null;
    }

}
