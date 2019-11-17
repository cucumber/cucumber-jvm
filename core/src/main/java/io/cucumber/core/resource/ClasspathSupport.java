package io.cucumber.core.resource;

import javax.lang.model.SourceVersion;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.of;

public final class ClasspathSupport {
    public static final String CLASSPATH_SCHEME = "classpath";
    public static final String CLASSPATH_SCHEME_PREFIX = CLASSPATH_SCHEME + ":";
    static final String DEFAULT_PACKAGE_NAME = "";
    private static final String CLASS_FILE_SUFFIX = ".class";
    public static final char RESOURCE_SEPARATOR_CHAR = '/';
    public static final String RESOURCE_SEPARATOR_STRING = String.valueOf(RESOURCE_SEPARATOR_CHAR);
    private static final char PACKAGE_SEPARATOR_CHAR = '.';
    public static final String PACKAGE_SEPARATOR_STRING = String.valueOf(PACKAGE_SEPARATOR_CHAR);
    private static final Pattern DOT_PATTERN = Pattern.compile("\\.");

    private ClasspathSupport() {

    }

    static void requireValidPackageName(String packageName) {
        requireNonNull(packageName, "packageName must not be null");
        if (packageName.equals(DEFAULT_PACKAGE_NAME)) {
            return;
        }
        boolean valid = stream(DOT_PATTERN.split(packageName, -1)).allMatch(SourceVersion::isName);
        if (!valid) {
            throw new IllegalArgumentException("Invalid part(s) in package name: " + packageName);
        }
    }


    static List<URI> getUrisForResource(ClassLoader classLoader, String resourceName) {
        try {
            Enumeration<URL> resources = classLoader.getResources(resourceName);
            List<URI> uris = new ArrayList<>();
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                uris.add(resource.toURI());
            }
            return uris;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    static List<URI> getRootUrisForPackage(ClassLoader classLoader, String basePackageName) {
        return getUrisForResource(classLoader, packagePath(basePackageName));
    }

    public static String packagePath(String packageName) {
        return packageName.replace(PACKAGE_SEPARATOR_CHAR, RESOURCE_SEPARATOR_CHAR);
    }

    public static String resourcePath(URI resourceUri) {
        if (!CLASSPATH_SCHEME.equals(resourceUri.getScheme())) {
            throw new IllegalArgumentException("uri must have classpath scheme " + resourceUri);
        }

        String resourcePath = resourceUri.getSchemeSpecificPart();
        if (resourcePath.startsWith(RESOURCE_SEPARATOR_STRING)) {
            return resourcePath.substring(1);
        }
        return resourcePath;
    }

    static String determinePackageName(Path baseDir, String basePackageName, Path classFile) {
        String subPackageName = determineSubpackageName(baseDir, classFile);
        return of(basePackageName, subPackageName)
            .filter(value -> !value.isEmpty()) // default package
            .collect(joining(PACKAGE_SEPARATOR_STRING));
    }

    private static String determineSubpackageResourceName(Path baseDir, Path resource) {
        Path relativePath = baseDir.relativize(resource.getParent());
        return relativePath.toString();
    }

    static String determineFullyQualifiedResourceName(Path baseDir, String packagePath, Path resource) {
        String subPackageName = determineSubpackageResourceName(baseDir, resource);
        String resourceName = resource.getFileName().toString();
        return of(packagePath, subPackageName, resourceName)
            .filter(value -> !value.isEmpty()) // default package .
            .collect(joining(RESOURCE_SEPARATOR_STRING));
    }

    private static String determineSubpackageName(Path baseDir, Path classFile) {
        Path relativePath = baseDir.relativize(classFile.getParent());
        String pathSeparator = baseDir.getFileSystem().getSeparator();
        return relativePath.toString().replace(pathSeparator, PACKAGE_SEPARATOR_STRING);
    }

    static String determineFullyQualifiedClassName(Path baseDir, String basePackageName, Path classFile) {
        String subPackageName = determineSubpackageName(baseDir, classFile);
        String simpleClassName = determineSimpleClassName(classFile);
        return of(basePackageName, subPackageName, simpleClassName)
            .filter(value -> !value.isEmpty()) // default package
            .collect(joining(PACKAGE_SEPARATOR_STRING));
    }

    private static String determineSimpleClassName(Path classFile) {
        String fileName = classFile.getFileName().toString();
        return fileName.substring(0, fileName.length() - CLASS_FILE_SUFFIX.length());
    }

    public static String packageNameOfResource(String classpathResourceName) {
        Path parent = Paths.get(classpathResourceName).getParent();
        if (parent == null) {
            return DEFAULT_PACKAGE_NAME;
        }

        String packagePath = parent.toString();
        return resourceName(packagePath);
    }

    public static String resourceName(String resourcePath) {
        return resourcePath.replace(RESOURCE_SEPARATOR_CHAR, PACKAGE_SEPARATOR_CHAR);
    }

    public static URI rootPackage() {
        try {
            return new URI(CLASSPATH_SCHEME, RESOURCE_SEPARATOR_STRING, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
