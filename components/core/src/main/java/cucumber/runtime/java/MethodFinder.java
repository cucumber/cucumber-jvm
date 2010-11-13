package cucumber.runtime.java;

import java.lang.reflect.Method;
import java.util.Set;

public interface MethodFinder {
    Set<Method> getStepDefinitionMethods();
}
