package cucumber.runtime;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Reflections {
    public static Set<Class<?>> getClasses(String packagePrefix) {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        List<URL> urls = classpathUrls(packagePrefix.replace(".", "/"));
        for (URL rootDir : urls) {
            if (rootDir.getProtocol().equals("jar")) {
                addClassesFromJar(packagePrefix, classes, rootDir);
            } else {
                addClassesFromFilesystem(packagePrefix, classes, rootDir);
            }
        }
        return classes;
    }

    public static <T> Set<Class<? extends T>> getSubtypesOf(Class<T> type, String packagePrefix) {
        Set<Class<? extends T>> result = new HashSet<Class<? extends T>>();
        Set<Class<?>> classes = getClasses(packagePrefix);
        for (Class<?> clazz : classes) {
            if(!type.equals(clazz) && type.isAssignableFrom(clazz)) {
                result.add(clazz.asSubclass(type));
            }
        }
        return result;
    }

    private static List<URL> classpathUrls(String path) {
        try {
            return Collections.list(cl().getResources(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addClassesFromJar(String packagePrefix, Set<Class<?>> classes, URL jarDir) {
        String url = jarDir.toExternalForm();
        String pathWithProtocol = url.substring(0, jarDir.toExternalForm().indexOf("!/"));
        String[] segments = pathWithProtocol.split(":");
        try {
            ZipFile jarFile = new ZipFile(segments[2]);
            Enumeration<? extends ZipEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry jarEntry = entries.nextElement();
                String entryName = jarEntry.getName();
                if (entryName.endsWith(".class")) {
                    String className = className(entryName);
                    if (className.startsWith(packagePrefix)) {
                        try {
                            addClassIfPublic(classes, className);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException("Failed to load class " + className + " from " + jarDir, e);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addClassesFromFilesystem(String packagePrefix, Set<Class<?>> classes, URL rootDir) {
        File dir = new File(rootDir.getFile());
        addClassesFromFilesystem(packagePrefix + ".", classes, dir);
    }

    private static void addClassesFromFilesystem(String currentPackagePrefixWithDot, Set<Class<?>> classes, File dir) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                addClassesFromFilesystem(currentPackagePrefixWithDot + file.getName() + ".", classes, file);
            } else if (file.getName().endsWith(".class")) {
                String className = currentPackagePrefixWithDot + className(file.getName());
                try {
                    addClassIfPublic(classes, className);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Failed to load class " + className + " from " + file.getAbsolutePath(), e);
                }
            }
        }
    }

    private static String className(String pathToClass) {
        return pathToClass.substring(0, pathToClass.length() - 6).replace("/", ".");
    }

    private static void addClassIfPublic(Set<Class<?>> classes, String className) throws ClassNotFoundException {
        Class<?> clazz = cl().loadClass(className);
        if (isPublic(clazz.getModifiers())) {
            classes.add(clazz);
        }
    }

    public static boolean isPublic(int modifiers) {
        return (modifiers & Modifier.PUBLIC) != 0;
    }

    private static ClassLoader cl() {
        return Thread.currentThread().getContextClassLoader();
    }
}
