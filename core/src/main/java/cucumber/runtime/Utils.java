package cucumber.runtime;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Utils {
    public static <T> List<T> listOf(int size, T obj) {
        List<T> list = new ArrayList<T>();
        for (int i = 0; i < size; i++) {
            list.add(obj);
        }
        return list;
    }

    public static boolean isInstantiable(Class<?> clazz) {
        boolean isNonStaticInnerClass = !Modifier.isStatic(clazz.getModifiers()) && clazz.getEnclosingClass() != null;
        return Modifier.isPublic(clazz.getModifiers()) && !Modifier.isAbstract(clazz.getModifiers()) && !isNonStaticInnerClass;
    }

    public static boolean hasConstructor(Class<?> clazz, Class[] paramTypes) {
        try {
            clazz.getConstructor(paramTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static String packagePath(Class clazz) {
        return packagePath(packageName(clazz.getName()));
    }

    public static String packagePath(String packageName) {
        return packageName.replace('.', '/');
    }

    public static String packageName(Class clazz) {
        return packageName(clazz.getName());
    }

    public static String toPackage(String path) {
        return path.replace('/', '.').replace('\\', '.');
    }

    static String packageName(String className) {
        return className.substring(0, Math.max(0, className.lastIndexOf(".")));
    }

    public static <T> Iterator<T> emptyIterator() {
        return new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public T next() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}