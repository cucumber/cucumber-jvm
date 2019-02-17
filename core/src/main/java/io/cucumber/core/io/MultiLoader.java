package io.cucumber.core.io;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public final class MultiLoader implements ResourceLoader {

    static final String CLASSPATH_SCHEME = "classpath";
    public static final String CLASSPATH_SCHEME_PREFIX = CLASSPATH_SCHEME + ":";
    static final String FILE_SCHEME = "file";
    public static final String FILE_SCHEME_PREFIX = FILE_SCHEME + ":";

    private final ClasspathResourceLoader classpath;
    private final FileResourceLoader fs;

    public MultiLoader(ClassLoader classLoader) {
        classpath = new ClasspathResourceLoader(classLoader);
        fs = new FileResourceLoader();
    }

    @Override
    public Iterable<Resource> resources(URI path, String suffix) {
        if (CLASSPATH_SCHEME.equals(path.getScheme())) {
            return classpath.resources(path, suffix);
        } else if (FILE_SCHEME.equals(path.getScheme())){
            return fs.resources(path, suffix);
        } else {
            throw new IllegalArgumentException("Unsupported scheme: " + path);
        }
    }

    public static List<String> packageName(List<URI> glue) {
        List<String> packageNames = new ArrayList<>(glue.size());
        for (URI gluePath : glue) {
            if(CLASSPATH_SCHEME.equals(gluePath.getScheme())) {
                packageNames.add(packageName(gluePath.getSchemeSpecificPart()));
            }
        }
        return packageNames;
    }

    private static String packageName(String gluePath) {
        if (isClasspathPath(gluePath)) {
            gluePath = stripClasspathPrefix(gluePath);
        }

        if(gluePath.startsWith("/")){
            gluePath = gluePath.substring(1);
        }

        return gluePath.replace('/', '.');
    }

    private static boolean isClasspathPath(String path) {
        return path.startsWith(CLASSPATH_SCHEME_PREFIX);
    }

    private static String stripClasspathPrefix(String path) {
        return path.substring(CLASSPATH_SCHEME_PREFIX.length());
    }

}
