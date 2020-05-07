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
    public static final char RESOURCE_SEPARATOR_CHAR = '/';
    public static final String RESOURCE_SEPARATOR_STRING = String.valueOf(RESOURCE_SEPARATOR_CHAR);
    static final String DEFAULT_PACKAGE_NAME = "";
    private static final String CLASS_FILE_SUFFIX = ".class";
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
        boolean valid = stream(DOT_PATTERN.split(packageName))
                .allMatch(SourceVersion::isName);
        if (!valid) {
            throw new IllegalArgumentException("Invalid part(s) in package name: " + packageName);
        }
    }

    static List<URI> getUrisForPackage(ClassLoader classLoader, String packageName) {
        return getUrisForResource(classLoader, resourceNameOfPackageName(packageName));
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

    public static String resourceNameOfPackageName(String packageName) {
        return packageName.replace(PACKAGE_SEPARATOR_CHAR, RESOURCE_SEPARATOR_CHAR);
    }

    static String determinePackageName(Path baseDir, String basePackageName, Path classFile) {
        String subPackageName = determineSubpackageName(baseDir, classFile);
        return of(basePackageName, subPackageName)
                .filter(value -> !value.isEmpty()) // default package
                .collect(joining(PACKAGE_SEPARATOR_STRING));
    }

    private static String determineSubpackageName(Path baseDir, Path resource) {
        Path relativePath = baseDir.relativize(resource.getParent());
        String pathSeparator = baseDir.getFileSystem().getSeparator();
        return relativePath.toString().replace(pathSeparator, PACKAGE_SEPARATOR_STRING);
    }

    static URI determineClasspathResourceUri(Path baseDir, String basePackagePath, Path resource) {
        String subPackageName = determineSubpackagePath(baseDir, resource);
        String resourceName = resource.getFileName().toString();
        String classpathResourcePath = of(basePackagePath, subPackageName, resourceName)
                .filter(value -> !value.isEmpty()) // default package .
                .collect(joining(RESOURCE_SEPARATOR_STRING));
        return classpathResourceUri(classpathResourcePath);
    }

    private static String determineSubpackagePath(Path baseDir, Path resource) {
        Path relativePath = baseDir.relativize(resource.getParent());
        String pathSeparator = baseDir.getFileSystem().getSeparator();
        return relativePath.toString().replace(pathSeparator, RESOURCE_SEPARATOR_STRING);
    }

    static URI classpathResourceUri(String classpathResourceName) {
        try {
            // Unlike URI.create the constructor escapes reserved characters
            return new URI(CLASSPATH_SCHEME, classpathResourceName, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    static String determineFullyQualifiedClassName(Path baseDir, String basePackageName, Path classFile) {
        String subpackageName = determineSubpackageName(baseDir, classFile);
        String simpleClassName = determineSimpleClassName(classFile);
        return of(basePackageName, subpackageName, simpleClassName)
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
        String pathSeparator = parent.getFileSystem().getSeparator();
        return parent.toString().replace(pathSeparator, PACKAGE_SEPARATOR_STRING);
    }

    static URI classpathResourceUri(Path resourcePath) {
        String pathSeparator = resourcePath.getFileSystem().getSeparator();
        String classpathResourceName = resourcePath.toString().replace(pathSeparator, RESOURCE_SEPARATOR_STRING);
        return classpathResourceUri(classpathResourceName);
    }

    public static String packageName(URI classpathResourceUri) {
        String resourceName = resourceName(classpathResourceUri);
        return resourceName.replace(RESOURCE_SEPARATOR_CHAR, PACKAGE_SEPARATOR_CHAR);
    }

    public static String resourceName(URI classpathResourceUri) {
        if (!CLASSPATH_SCHEME.equals(classpathResourceUri.getScheme())) {
            throw new IllegalArgumentException("uri must have classpath scheme " + classpathResourceUri);
        }

        String classpathResourcePath = classpathResourceUri.getSchemeSpecificPart();
        if (classpathResourcePath.startsWith(RESOURCE_SEPARATOR_STRING)) {
            return classpathResourcePath.substring(1);
        }
        return classpathResourcePath;
    }

    public static URI rootPackageUri() {
        return URI.create(CLASSPATH_SCHEME_PREFIX + RESOURCE_SEPARATOR_CHAR);
    }

}
