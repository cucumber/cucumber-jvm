package cucumber.runtime.android;

import cucumber.runtime.io.Reflections;
import ext.android.test.ClassPathPackageInfoSource;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AndroidReflections implements Reflections {
    private final ClassPathPackageInfoSource source;

    public AndroidReflections(ClassPathPackageInfoSource source) {
        this.source = source;
    }

    @Override
    public Collection<Class<? extends Annotation>> getAnnotations(String packageName) {
        return getDescendants(Annotation.class, packageName);
    }

    @Override
    public <T> Collection<Class<? extends T>> getDescendants(Class<T> parentType, String packageName) {
        List<Class<? extends T>> result = new ArrayList<Class<? extends T>>();
        for (Class clazz : source.getPackageInfo(packageName).getTopLevelClassesRecursive()) {
            if (clazz != null && !parentType.equals(clazz) && parentType.isAssignableFrom(clazz)) {
                result.add(clazz.asSubclass(parentType));
            }
        }
        return result;
    }

    @Override
    public <T> T instantiateExactlyOneSubclass(Class<T> parentType, String packageName, Class[] constructorParams, Object[] constructorArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Collection<? extends T> instantiateSubclasses(Class<T> parentType, String packageName, Class[] constructorParams, Object[] constructorArgs) {
        throw new UnsupportedOperationException();
    }
}
