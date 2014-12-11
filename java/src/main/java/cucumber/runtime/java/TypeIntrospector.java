package cucumber.runtime.java;

import java.lang.reflect.Type;

public interface TypeIntrospector {
    public Type[] getGenericTypes(Class<?> clazz) throws Exception;
}
