package io.cucumber.jupiter.engine.resource;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;

public class ClasspathSupport {
    static final String DEFAULT_PACKAGE_NAME = "";
    private static final Logger logger = LoggerFactory.getLogger(PathScanner.ResourceFileVisitor.class);
    private static final String CLASS_FILE_SUFFIX = ".class";
    private static final char CLASSPATH_RESOURCE_PATH_SEPARATOR = '/';
    private static final String CLASSPATH_RESOURCE_PATH_SEPARATOR_STRING = String.valueOf(CLASSPATH_RESOURCE_PATH_SEPARATOR);
    private static final char PACKAGE_SEPARATOR_CHAR = '.';
    private static final String PACKAGE_SEPARATOR_STRING = String.valueOf(PACKAGE_SEPARATOR_CHAR);

    private ClasspathSupport() {

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
        } catch (Exception ex) {
            logger.warn(ex, () -> "Error reading URIs from class loader for resource name " + resourceName);
            return emptyList();
        }
    }

    static List<URI> getRootUrisForPackage(ClassLoader classLoader, String basePackageName) {
        return getUrisForResource(classLoader, packagePath(basePackageName));
    }

    static String packagePath(String packageName) {
        return packageName.replace(PACKAGE_SEPARATOR_CHAR, CLASSPATH_RESOURCE_PATH_SEPARATOR);
    }


    static String determinePackageName(Path baseDir, String basePackageName, Path classFile) {
        return Stream.of(
            basePackageName,
            determineSubpackageName(baseDir, classFile)
        )
            .filter(value -> !value.isEmpty()) // Handle default package appropriately.
            .collect(joining(PACKAGE_SEPARATOR_STRING));
    }

    static String determineFullyQualifiedResourceName(Path baseDir, String packagePath, Path resource) {
        return Stream.of(
            packagePath,
            determineSubpackageName(baseDir, resource),
            resource.getFileName().toString()
        )
            .filter(value -> !value.isEmpty()) // Handle default package appropriately.
            .collect(joining(CLASSPATH_RESOURCE_PATH_SEPARATOR_STRING));
    }

    private static String determineSubpackageName(Path baseDir, Path classFile) {
        Path relativePath = baseDir.relativize(classFile.getParent());
        String pathSeparator = baseDir.getFileSystem().getSeparator();
        return relativePath.toString().replace(pathSeparator, PACKAGE_SEPARATOR_STRING);
    }

    static String determineFullyQualifiedClassName(Path baseDir, String basePackageName, Path classFile) {
        return Stream.of(
            basePackageName,
            determineSubpackageName(baseDir, classFile),
            determineSimpleClassName(classFile)
        )
            .filter(value -> !value.isEmpty()) // Handle default package appropriately.
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

    private static String resourceName(String resourcePath) {
        return resourcePath.replace(CLASSPATH_RESOURCE_PATH_SEPARATOR, PACKAGE_SEPARATOR_CHAR);
    }

}
