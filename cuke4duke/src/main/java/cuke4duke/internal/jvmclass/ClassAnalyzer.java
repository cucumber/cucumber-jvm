package cuke4duke.internal.jvmclass;

import java.lang.reflect.Method;

public interface ClassAnalyzer {
    void populateStepDefinitionsAndHooksFor(Method method, ObjectFactory objectFactory, ClassLanguage classLanguage) throws Throwable;
    Class<?>[] alwaysLoad();
}
