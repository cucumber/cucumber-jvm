package cucumber.runtime.java;

import cucumber.api.java8.StepdefBody;

import java.lang.reflect.Type;

public interface TypeIntrospector {
    Type[] getGenericTypes(Class<? extends StepdefBody> clazz, Class<? extends StepdefBody> interfac3) throws Exception;
}
