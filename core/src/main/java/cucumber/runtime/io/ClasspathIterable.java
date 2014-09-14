package cucumber.runtime.io;

import cucumber.runtime.CucumberException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Iterator;

public class ClasspathIterable implements Iterable<Resource> {
    private final ClassLoader cl;
    private final ResourceIteratorFactory resourceIteratorFactory;
    private final String path;
    private final String suffix;

    public ClasspathIterable(ClassLoader cl, String path, String suffix) {
        this.cl = cl;
        this.resourceIteratorFactory = new DelegatingResourceIteratorFactory();
        this.path = path;
        this.suffix = suffix;
    }

    @Override
    public Iterator<Resource> iterator() {
        try {
            FlatteningIterator iterator = new FlatteningIterator();
            Enumeration<URL> resources = cl.getResources(path);
            boolean somethingFound = false;
            while (resources.hasMoreElements()) {
                somethingFound = true;
                URL url = resources.nextElement();
                iterator.push(this.resourceIteratorFactory.createIterator(url, path, suffix));
            }
            if (!somethingFound) { // maybe not a folder but directly the resource
                final String newPath = path + suffix;
                Enumeration<URL> directResources = cl.getResources(newPath);
                while (directResources.hasMoreElements()) {
                    final URL url = directResources.nextElement();
                    iterator.push(this.resourceIteratorFactory.createIterator(url, newPath, suffix));
                }
            }
            return iterator;
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }

    static String filePath(URL jarUrl) throws UnsupportedEncodingException, MalformedURLException {
        String path = new File(new URL(jarUrl.getFile()).getFile()).getAbsolutePath();
        String pathToJar = path.substring(0, path.lastIndexOf("!"));
        return URLDecoder.decode(pathToJar, "UTF-8");
    }

    static boolean hasSuffix(String suffix, String name) {
        return suffix == null || name.endsWith(suffix);
    }

    static String getPath(URL url) {
        try {
            return URLDecoder.decode(url.getPath(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new CucumberException("Encoding problem", e);
        }
    }
}
