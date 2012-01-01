package cucumber.io;

import cucumber.runtime.CucumberException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Iterator;

class ClasspathIterable implements Iterable<Resource> {
    private final ClassLoader cl;
    private final String path;
    private final String suffix;

    public ClasspathIterable(ClassLoader cl, String path, String suffix) {
        this.cl = cl;
        this.path = path;
        this.suffix = suffix;
    }

    @Override
    public Iterator<Resource> iterator() {
        try {
            FlatteningIterator iterator = new FlatteningIterator();
            Enumeration<URL> resources = cl.getResources(path);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if (url.getProtocol().equals("jar")) {
                    String jarPath = filePath(url.toExternalForm());
                    iterator.push(new ZipResourceIterator(jarPath, path, suffix));
                } else {
                    File file = new File(getPath(url));
                    File rootDir = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - path.length()));
                    iterator.push(new FileResourceIterator(rootDir, file, suffix));
                }
            }
            return iterator;
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }

    static String filePath(String jarUrl) throws UnsupportedEncodingException {
        String pathWithProtocol = jarUrl.substring(0, jarUrl.indexOf("!/"));
        String[] segments = pathWithProtocol.split(":");
        // WINDOWS: jar:file:/C:/Users/ahellesoy/scm/cucumber-jvm/java/target/java-1.0.0-SNAPSHOT.jar
        // POSIX:   jar:file:/Users/ahellesoy/scm/cucumber-jvm/java/target/java-1.0.0-SNAPSHOT.jar
        String pathToJar = segments.length == 4 ? segments[2].substring(1) + ":" + segments[3] : segments[2];
        return URLDecoder.decode(pathToJar, "UTF-8");
    }

    static boolean hasSuffix(String suffix, String name) {
        return suffix == null || name.endsWith(suffix);
    }

    private static String getPath(URL url) {
        try {
            return URLDecoder.decode(url.getPath(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new CucumberException("Encoding problem", e);
        }
    }
}
