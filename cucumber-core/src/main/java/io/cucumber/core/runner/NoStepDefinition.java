package io.cucumber.core.runner;

import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.StepDefinition;

import java.util.Collections;
import java.util.List;

final class NoStepDefinition implements StepDefinition {

    @Override
    public void execute(Object[] args) {
    }

    @Override
    public List<ParameterInfo> parameterInfos() {
        return Collections.emptyList();
    }

    @Override
    public String getPattern() {
        return "";
    }

    @Override
    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return false;
    }

    @Override
    public String getLocation() {
        return "no location";
    }

}
