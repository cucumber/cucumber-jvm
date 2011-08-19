package cucumber.classpath;

import cucumber.io.FileResource;
import cucumber.io.Resource;
import cucumber.io.ZipResource;
import cucumber.runtime.CucumberException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Static utility methods for looking up classes and resources on the classpath.
 */
public class Classpath {
    public static Set<Class<?>> getInstantiableClasses(final String packagePrefix) {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        final Consumer consumer = new Consumer() {
            public void consume(Resource input) {
                String path = input.getPath();
                String className = className(path);
                try {
                    addClassIfInstantiable(classes, className);
                } catch (NoClassDefFoundError ignore) {
                } catch (ClassNotFoundException ignore) {
                }
            }
        };

        scan(packagePrefix.replace('.', '/'), ".class", consumer);
        return classes;
    }

    public static <T> Set<Class<? extends T>> getInstantiableSubclassesOf(Class<T> type, String packagePrefix) {
        Set<Class<? extends T>> result = new HashSet<Class<? extends T>>();
        Collection<Class<?>> classes = getInstantiableClasses(packagePrefix);
        for (Class<?> clazz : classes) {
            if (!type.equals(clazz) && type.isAssignableFrom(clazz)) {
                result.add(clazz.asSubclass(type));
            }
        }
        return result;
    }

    public static void scan(String pathPrefix, String suffix, Consumer consumer) {
        final List<URL> startUrls = classpathUrls(pathPrefix);
        for (URL startUrl : startUrls) {
            if (startUrl.getProtocol().equals("jar")) {
                scanJar(startUrl, pathPrefix, suffix, consumer);
            } else {
                scanFilesystem(startUrl, pathPrefix, suffix, consumer);
            }
        }
    }

    public static void scan(String pathName, Consumer consumer) {
        final List<URL> startUrls = classpathUrls(pathName);
        for (URL startUrl : startUrls) {
            if (startUrl.getProtocol().equals("jar")) {
                scanJar(startUrl, pathName, null, consumer);
            } else {
                scanFilesystem(startUrl, pathName, null, consumer);
            }
        }
    }

    private static List<URL> classpathUrls(String path) throws NoSuchResourceException {
        try {
            Enumeration<URL> resources = cl().getResources(path);
            if (!resources.hasMoreElements()) {
                throw new NoSuchResourceException("No resources at path " + path);
            }
            return Collections.list(resources);
        } catch (IOException e) {
            throw new CucumberException("Failed to look up resources at path " + path, e);
        }
    }

    public static <T> T instantiateExactlyOneSubclass(Class<T> type, String packagePrefix, Object... constructorArguments) {
        Collection<T> instances = instantiateSubclasses(type, packagePrefix, constructorArguments);
        if (instances.size() == 1) {
            return instances.iterator().next();
        } else if (instances.size() == 0) {
            throw new CucumberException("Couldn't find a single implementation of " + type);
        } else {
            throw new CucumberException("Expected only one instance, but found too many: " + instances);
        }
    }

    public static <T> List<T> instantiateSubclasses(Class<T> type, String packagePrefix, Object... constructorArguments) {
        List<T> result = new ArrayList<T>();

        Collection<Class<? extends T>> classes = getInstantiableSubclassesOf(type, packagePrefix);
        for (Class<? extends T> clazz : classes) {
            try {
                Class[] argumentTypes = new Class[constructorArguments.length];
                for (int i = 0; i < constructorArguments.length; i++) {
                    argumentTypes[i] = constructorArguments[i].getClass();
                }
                result.add(clazz.getConstructor(argumentTypes).newInstance(constructorArguments));
            } catch (InstantiationException e) {
                throw new CucumberException("Couldn't instantiate " + clazz, e);
            } catch (IllegalAccessException e) {
                throw new CucumberException("Couldn't instantiate " + clazz, e);
            } catch (NoSuchMethodException e) {
                throw new CucumberException("Couldn't instantiate " + clazz, e);
            } catch (InvocationTargetException e) {
                throw new CucumberException("Couldn't instantiate " + clazz, e);
            }

        }
        return result;
    }

    private static void scanJar(URL jarDir, String pathPrefix, String suffix, Consumer consumer) {
        String jarUrl = jarDir.toExternalForm();
        String path = filePath(jarUrl);
        try {
            ZipFile jarFile = new ZipFile(path);
            Enumeration<? extends ZipEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry jarEntry = entries.nextElement();
                String entryName = jarEntry.getName();
                if (entryName.startsWith(pathPrefix) && hasSuffix(suffix, entryName)) {
                    consumer.consume(new ZipResource(jarFile, jarEntry));
                }
            }
        } catch (IOException t) {
            throw new CucumberException("Failed to scan jar", t);
        }
    }

    private static String filePath(String jarUrl) {
        String pathWithProtocol = jarUrl.substring(0, jarUrl.indexOf("!/"));
        String[] segments = pathWithProtocol.split(":");
        // WINDOWS: jar:file:/C:/Users/ahellesoy/scm/cucumber-jvm/java/target/java-0.4.3-SNAPSHOT.jar
        // POSIX:   jar:file:/Users/ahellesoy/scm/cucumber-jvm/java/target/java-0.4.3-SNAPSHOT.jar
        return segments.length == 4 ?  segments[2].substring(1) + ":" + segments[3] : segments[2];
    }

    private static void scanFilesystem(URL startDir, String pathPrefix, String suffix, Consumer consumer) {
        File dir = new File(startDir.getFile());
        String rootPath = startDir.getFile().substring(0, startDir.getFile().length() - pathPrefix.length() - 1);
        File rootDir = new File(rootPath);
        scanFilesystem(rootDir, dir, suffix, consumer);
    }

    private static void scanFilesystem(File rootDir, File file, String suffix, Consumer consumer) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                scanFilesystem(rootDir, child, suffix, consumer);
            }
        } else {
            if (hasSuffix(suffix, file.getName())) {
                consumer.consume(new FileResource(rootDir, file));
            }
        }
    }

    private static boolean hasSuffix(String suffix, String name) {
        return suffix == null || name.endsWith(suffix);
    }

    private static String className(String pathToClass) {
        return pathToClass.substring(0, pathToClass.length() - 6).replace("/", ".");
    }

    private static void addClassIfInstantiable(Collection<Class<?>> classes, String className) throws ClassNotFoundException, NoClassDefFoundError {
        Class<?> clazz = cl().loadClass(className);
        boolean isInstantiable = Modifier.isStatic(clazz.getModifiers()) || clazz.getEnclosingClass() == null;
        if (Modifier.isPublic(clazz.getModifiers()) && isInstantiable) {
            classes.add(clazz);
        }
    }

    private static ClassLoader cl() {
        return Thread.currentThread().getContextClassLoader();
    }
}
