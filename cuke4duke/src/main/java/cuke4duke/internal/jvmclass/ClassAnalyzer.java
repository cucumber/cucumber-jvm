package cuke4duke.internal.jvmclass;

public interface ClassAnalyzer {
    void populateStepDefinitionsAndHooks(ObjectFactory objectFactory, ClassLanguage classLanguage) throws Throwable;

    Class<?>[] alwaysLoad();
}
