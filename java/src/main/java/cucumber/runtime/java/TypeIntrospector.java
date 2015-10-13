package cucumber.runtime.java;

import java.lang.reflect.Type;

public interface TypeIntrospector {
    Type[] getGenericTypes(Class<?> clazz) throws Exception;
}
