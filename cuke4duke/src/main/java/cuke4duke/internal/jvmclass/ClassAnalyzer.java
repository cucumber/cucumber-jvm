package cuke4duke.internal.jvmclass;

public interface ClassAnalyzer {
    void registerHooksAndStepDefinitionsFor(Class<?> clazz, ClassLanguage classLanguage);
}
