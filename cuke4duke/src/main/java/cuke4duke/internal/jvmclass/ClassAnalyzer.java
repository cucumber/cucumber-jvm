package cuke4duke.internal.jvmclass;

public interface ClassAnalyzer {
    void populateStepDefinitionsAndHooksFor(Class<?> clazz, ObjectFactory objectFactory, ClassLanguage classLanguage) throws Throwable;
    void addDefaultTransforms(ClassLanguage classLanguage, ObjectFactory objectFactory);
    Class<?>[] alwaysLoad();
}
