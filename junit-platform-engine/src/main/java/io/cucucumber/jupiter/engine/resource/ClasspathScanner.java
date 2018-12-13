package io.cucucumber.jupiter.engine.resource;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.cucucumber.jupiter.engine.resource.ClasspathSupport.determineFullyQualifiedClassName;
import static io.cucucumber.jupiter.engine.resource.ClasspathSupport.getRootUrisForPackage;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.util.BlacklistedExceptions.rethrowIfBlacklisted;
import static org.junit.platform.commons.util.PackageUtils.assertPackageNameIsValid;

class ClasspathScanner {

    private static final Logger logger = LoggerFactory.getLogger(PathScanner.ResourceFileVisitor.class);

    private static final String CLASS_FILE_SUFFIX = ".class";
    private static final String PACKAGE_INFO_FILE_NAME = "package-info" + CLASS_FILE_SUFFIX;
    private static final String MODULE_INFO_FILE_NAME = "module-info" + CLASS_FILE_SUFFIX;
    private static final String DEFAULT_PACKAGE_NAME = "";

    private final PathScanner pathScanner = new PathScanner();

    private final Supplier<ClassLoader> classLoaderSupplier;

    private final BiFunction<String, ClassLoader, Optional<Class<?>>> loadClass;

    ClasspathScanner(Supplier<ClassLoader> classLoaderSupplier,
                     BiFunction<String, ClassLoader, Optional<Class<?>>> loadClass) {

        this.classLoaderSupplier = classLoaderSupplier;
        this.loadClass = loadClass;
    }

    private static boolean isClassFile(Path file) {
        return file.getFileName().toString().endsWith(CLASS_FILE_SUFFIX);
    }

    private static boolean isNotPackageInfo(Path path) {
        return !path.endsWith(PACKAGE_INFO_FILE_NAME);
    }

    private static boolean isNotModuleInfo(Path path) {
        return !path.endsWith(MODULE_INFO_FILE_NAME);
    }

    <T> List<Class<? extends T>> scanForSubClassesInPackage(String basePackageName, Class<T> parentClass) {
        ClassFilter subclassOf = ClassFilter.of(aClass -> !parentClass.equals(aClass) && parentClass.isAssignableFrom(aClass));
        return scanForClassesInPackage(basePackageName, subclassOf)
            .stream()
            .map(aClass -> (Class<? extends T> ) aClass.asSubclass(parentClass))
            .collect(toList());
    }

    List<Class<?>> scanForClassesInPackage(String basePackageName, ClassFilter classFilter) {
        assertPackageNameIsValid(basePackageName);
        requireNonNull(classFilter, "classFilter must not be null");
        basePackageName = basePackageName.trim();

        return findClassesForUris(getRootUrisForPackage(getClassLoader(), basePackageName), basePackageName, classFilter);
    }

    private List<Class<?>> findClassesForUris(List<URI> baseUris, String basePackageName, ClassFilter classFilter) {
        return baseUris.stream()
            .map(baseUri -> findClassesForUri(baseUri, basePackageName, classFilter))
            .flatMap(Collection::stream)
            .distinct()
            .collect(toList());
    }

    private List<Class<?>> findClassesForUri(URI baseUri, String basePackageName, ClassFilter classFilter) {
        List<Class<?>> classes = new ArrayList<>();
        pathScanner.findResourcesForUri(
            baseUri,
            path -> isNotModuleInfo(path) && isNotPackageInfo(path) && isClassFile(path),
            baseDir -> path -> processClassFileSafely(baseDir, basePackageName, classFilter, path, classes::add)
        );
        return classes;
    }

    List<Class<?>> scanForClassesInClasspathRoot(URI root, ClassFilter classFilter) {
        requireNonNull(root, "root must not be null");
        requireNonNull(classFilter, "classFilter must not be null");

        return findClassesForUri(root, DEFAULT_PACKAGE_NAME, classFilter);
    }

    private void processClassFileSafely(Path baseDir, String basePackageName, ClassFilter classFilter, Path classFile,
                                        Consumer<Class<?>> classConsumer) {
        try {
            String fullyQualifiedClassName = determineFullyQualifiedClassName(baseDir, basePackageName, classFile);
            if (classFilter.match(fullyQualifiedClassName)) {
                loadClass.apply(fullyQualifiedClassName, getClassLoader())
                    .filter(classFilter)
                    .ifPresent(classConsumer);
            }
        } catch (Throwable throwable) {
            handleThrowable(classFile, throwable);
        }
    }

    private void handleThrowable(Path classFile, Throwable throwable) {
        rethrowIfBlacklisted(throwable);
        logGenericFileProcessingException(classFile, throwable);
    }

    private void logGenericFileProcessingException(Path classFile, Throwable throwable) {
        logger.debug(throwable, () -> format("Failed to load java.lang.Class for path [%s] during classpath scanning.",
            classFile.toAbsolutePath()));
    }

    private ClassLoader getClassLoader() {
        return this.classLoaderSupplier.get();
    }

}
