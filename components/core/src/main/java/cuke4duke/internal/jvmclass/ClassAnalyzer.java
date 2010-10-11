package cuke4duke.internal.jvmclass;

import cucumber.runtime.java.ObjectFactory;

public interface ClassAnalyzer {
    void populateStepDefinitionsAndHooks(ObjectFactory objectFactory, ClassLanguage classLanguage) throws Throwable;

    Class<?>[] alwaysLoad();
}
