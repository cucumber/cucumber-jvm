package io.cucumber.core.plugin;

import io.cucumber.core.backend.SourceReference;

import java.lang.reflect.Method;

class TeamCityPluginTestStepDefinition {
    SourceReference source;

    TeamCityPluginTestStepDefinition() {
        source = SourceReference.fromStackTraceElement(new Exception().getStackTrace()[0]);
    }

    public void beforeHook() {

    }

    static SourceReference getAnnotationSourceReference() {
        try {
            Method method = TeamCityPluginTestStepDefinition.class.getMethod("beforeHook");
            return SourceReference.fromMethod(method);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    static SourceReference getStackSourceReference() {
        return new TeamCityPluginTestStepDefinition().source;
    }
}
