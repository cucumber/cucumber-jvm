package cuke4duke.internal.java;

import cuke4duke.Scenario;
import cuke4duke.internal.jvmclass.ClassLanguage;
import cuke4duke.internal.language.AbstractHook;

import java.lang.reflect.Method;

public class JavaHook extends AbstractHook {
    private final ClassLanguage classLanguage;
    private final Method method;

    public JavaHook(ClassLanguage classLanguage, Method method, String[] tagExpressions) {
        super(tagExpressions);
        this.classLanguage = classLanguage;

        this.method = method;
    }
    
    public void invoke(String location, Scenario scenario) throws Throwable {
        classLanguage.invokeHook(method, scenario);
    }

    public Method getMethod() {
        return method;
    }
}
