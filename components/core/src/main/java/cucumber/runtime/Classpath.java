package cucumber.runtime;

import java.io.*;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Classpath {
    public static Set<Class<?>> getPublicClasses(final String packagePrefix) throws IOException {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        final Consumer consumer = new Consumer() {
            public void consume(Input input) {
                String className = className(input.getPath());
                try {
                    addClassIfPublic(classes, className);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Failed to load class " + className + " from " + input.getPath(), e);
                }
            }
        };

        scan(packagePrefix, ".class", consumer);
        return classes;
    }

    public static <T> Set<Class<? extends T>> getPublicSubclassesOf(Class<T> type, String packagePrefix) throws IOException {
        Set<Class<? extends T>> result = new HashSet<Class<? extends T>>();
        Set<Class<?>> classes = getPublicClasses(packagePrefix);
        for (Class<?> clazz : classes) {
            if (!type.equals(clazz) && type.isAssignableFrom(clazz)) {
                result.add(clazz.asSubclass(type));
            }
        }
        return result;
    }

    public static void scan(String packagePrefix, String suffix, Consumer consumer) throws IOException {
        String pathPrefix = packagePrefix.replace(".", "/");
        final List<URL> startUrls = classpathUrls(pathPrefix);
        for (URL startDir : startUrls) {
            if (startDir.getProtocol().equals("jar")) {
                scanJar(pathPrefix, startDir, suffix, consumer);
            } else {
                scanFilesystem(pathPrefix, startDir, suffix, consumer);
            }
        }
    }

    private static List<URL> classpathUrls(String path) throws IOException {
        return Collections.list(cl().getResources(path));
    }

    private static abstract class AbstractInput implements Input {
        public String getString() throws IOException {
            return read(getReader());
        }

        public Reader getReader() throws IOException {
            return new InputStreamReader(getInputStream(), "UTF-8");
        }

        private String read(Reader reader) throws IOException {
            StringBuffer sb = new StringBuffer();
            int n;
            while ((n = reader.read()) != -1) {
                sb.append((char) n);
            }
            return sb.toString();
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

        public InputStream getInputStream() throws IOException {
            return jarFile.getInputStream(jarEntry);
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

        public InputStream getInputStream() throws IOException {
            return new FileInputStream(file);
        }
    }

    private static void scanJar(String pathPrefix, URL jarDir, String suffix, Consumer consumer) {
        String url = jarDir.toExternalForm();
        String pathWithProtocol = url.substring(0, jarDir.toExternalForm().indexOf("!/"));
        String[] segments = pathWithProtocol.split(":");
        try {
            ZipFile jarFile = new ZipFile(segments[2]);
            Enumeration<? extends ZipEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry jarEntry = entries.nextElement();
                String entryName = jarEntry.getName();
                if (entryName.startsWith(pathPrefix) && entryName.endsWith(suffix)) {
                    consumer.consume(new ZipInput(jarFile, jarEntry));
                }
            }
        } catch (IOException t) {
            throw new CucumberException("Failed to scan jar", t);
        }
    }

    private static void scanFilesystem(String pathPrefix, URL startDir, String suffix, Consumer consumer) throws IOException {
        File dir = new File(startDir.getFile());
        String rootPath = startDir.getFile().substring(0, startDir.getFile().length() - pathPrefix.length() - 1);
        File rootDir = new File(rootPath);
        scanFilesystem(rootDir, dir, suffix, consumer);
    }

    private static void scanFilesystem(File rootDir, File file, String suffix, Consumer consumer) throws IOException {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                scanFilesystem(rootDir, child, suffix, consumer);
            }
        } else {
            if (file.getName().endsWith(suffix)) {
                consumer.consume(new FileInput(rootDir, file));
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
