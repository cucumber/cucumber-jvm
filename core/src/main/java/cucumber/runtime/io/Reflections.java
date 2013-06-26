package cucumber.runtime.io;

import java.lang.annotation.Annotation;
import java.util.Collection;

public interface Reflections {
    Collection<Class<? extends Annotation>> getAnnotations(String packageName);

    <T> Collection<Class<? extends T>> getDescendants(Class<T> parentType, String packageName);

    <T> T instantiateExactlyOneSubclass(Class<T> parentType, String packageName, Class[] constructorParams, Object[] constructorArgs);

    <T> Collection<? extends T> instantiateSubclasses(Class<T> parentType, String packageName, Class[] constructorParams, Object[] constructorArgs);
}
