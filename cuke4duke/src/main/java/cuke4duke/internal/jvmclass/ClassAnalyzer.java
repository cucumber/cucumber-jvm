package cuke4duke.internal.jvmclass;

import cuke4duke.internal.language.Hook;
import cuke4duke.internal.language.StepDefinition;

import java.util.List;

public interface ClassAnalyzer {
    void populateStepDefinitionsAndHooksFor(Class<?> clazz, ObjectFactory objectFactory, ClassLanguage classLanguage);
}
