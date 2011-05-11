package cucumber.classpath;

import cucumber.runtime.CucumberException;

import java.io.*;
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
            public void consume(Input input) {
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

    private static List<URL> classpathUrls(String path) {
        try {
            Enumeration<URL> resources = cl().getResources(path);
            if(!resources.hasMoreElements()) {
                throw new CucumberException("No resources at path " + path);
            }
            return Collections.list(resources);
        } catch (IOException e) {
            throw new CucumberException("Failed to look up resources at path " + path, e);
        }
    }

    public static <T> T instantiateSubclass(Class<T> type, Object... constructorArguments) {
        Collection<T> instances = instantiateSubclasses(type, constructorArguments);
        if (instances.size() == 1) {
            return instances.iterator().next();
        } else if (instances.size() == 0) {
            throw new CucumberException("Couldn't find a suitable instance");
        } else {
            throw new CucumberException("Expected only one instance, but found too many: " + instances);
        }
    }

    public static <T> List<T> instantiateSubclasses(Class<T> type, Object... constructorArguments) {
        List<T> result = new ArrayList<T>();

        Collection<Class<? extends T>> classes = getInstantiableSubclassesOf(type, "cucumber.runtime");
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

    private static abstract class AbstractInput implements Input {
        public String getString() {
            return read(getReader());
        }

        public Reader getReader() {
            try {
                return new InputStreamReader(getInputStream(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new CucumberException("Failed to open path " + getPath(), e);
            }
        }

        private String read(Reader reader) {
            try {
                StringBuffer sb = new StringBuffer();
                int n;
                while ((n = reader.read()) != -1) {
                    sb.append((char) n);
                }
                return sb.toString();
            } catch (IOException e) {
                throw new CucumberException("Failed to read", e);
            }
        }
    }

    private static class ZipInput extends AbstractInput {
        private final ZipFile jarFile;
        private final ZipEntry jarEntry;

        public ZipInput(ZipFile jarFile, ZipEntry jarEntry) {
            this.jarFile = jarFile;
            this.jarEntry = jarEntry;
        }

        public String getPath() {
            return jarEntry.getName();
        }

        public InputStream getInputStream() {
            try {
                return jarFile.getInputStream(jarEntry);
            } catch (IOException e) {
                throw new CucumberException("Failed to read from jar file", e);
            }
        }
    }

    private static class FileInput extends AbstractInput {
        private final File rootDir;
        private final File file;

        public FileInput(File rootDir, File file) {
            this.rootDir = rootDir;
            this.file = file;
        }

        public String getPath() {
            return file.getAbsolutePath().substring(rootDir.getAbsolutePath().length() + 1, file.getAbsolutePath().length());
        }

        public InputStream getInputStream() {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new CucumberException("Failed to read from file " + file.getAbsolutePath(), e);
            }
        }
    }

    private static void scanJar(URL jarDir, String pathPrefix, String suffix, Consumer consumer) {
        String url = jarDir.toExternalForm();
        String pathWithProtocol = url.substring(0, jarDir.toExternalForm().indexOf("!/"));
        String[] segments = pathWithProtocol.split(":");
        try {
            ZipFile jarFile = new ZipFile(segments[2]);
            Enumeration<? extends ZipEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry jarEntry = entries.nextElement();
                String entryName = jarEntry.getName();
                if (entryName.startsWith(pathPrefix) && hasSuffix(suffix, entryName)) {
                    consumer.consume(new ZipInput(jarFile, jarEntry));
                }
            }
        } catch (IOException t) {
            throw new CucumberException("Failed to scan jar", t);
        }
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
                consumer.consume(new FileInput(rootDir, file));
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
