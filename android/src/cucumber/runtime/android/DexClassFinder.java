package cucumber.runtime.android;

import cucumber.runtime.CucumberException;
import cucumber.runtime.ClassFinder;
import dalvik.system.DexFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

public class DexClassFinder implements ClassFinder {
    private static final ClassLoader CLASS_LOADER = DexClassFinder.class.getClassLoader();
    private final DexFile dexFile;

    public DexClassFinder(DexFile dexFile) {
        this.dexFile = dexFile;
    }

    @Override
    public <T> Collection<Class<? extends T>> getDescendants(Class<T> parentType, String packageName) {
        List<Class<? extends T>> result = new ArrayList<Class<? extends T>>();

        Enumeration<String> entries = dexFile.entries();
        while (entries.hasMoreElements()) {
            String className = entries.nextElement();
            if (isInPackage(className, packageName) && !isGenerated(className)) {
                Class<? extends T> clazz = loadClass(className);
                if (clazz != null && !parentType.equals(clazz) && parentType.isAssignableFrom(clazz)) {
                    result.add(clazz.asSubclass(parentType));
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> Class<? extends T> loadClass(String className) {
        try {
            return (Class<? extends T>) Class.forName(className, false, CLASS_LOADER);
        } catch (ClassNotFoundException e) {
            throw new CucumberException(e);
        }
    }

    private boolean isInPackage(String className, String packageName) {
        int lastDotIndex = className.lastIndexOf(".");
        String classPackage = lastDotIndex == -1 ? "" : className.substring(0, lastDotIndex);
        return classPackage.startsWith(packageName);
    }

    private boolean isGenerated(String className) {
        int lastDotIndex = className.lastIndexOf(".");
        String shortName = lastDotIndex == -1 ? className : className.substring(lastDotIndex + 1);
        return shortName.equals("Manifest") || shortName.equals("R") || shortName.startsWith("R$");
    }
}
