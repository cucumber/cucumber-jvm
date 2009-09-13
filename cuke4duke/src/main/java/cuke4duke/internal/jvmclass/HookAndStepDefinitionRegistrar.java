package cuke4duke.internal.jvmclass;

public interface HookAndStepDefinitionRegistrar {
    void registerHooksAndStepDefinitionsFor(Class<?> clazz, ClassLanguage classLanguage);
}
