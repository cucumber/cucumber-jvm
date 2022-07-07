package io.cucumber.core.resource;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.cucumber.core.resource.ClasspathSupport.classPathScanningExplanation;
import static io.cucumber.core.resource.ClasspathSupport.determineFullyQualifiedClassName;
import static io.cucumber.core.resource.ClasspathSupport.getUrisForPackage;
import static io.cucumber.core.resource.ClasspathSupport.requireValidPackageName;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public final class ClasspathScanner {

    private static final Logger log = LoggerFactory.getLogger(ClasspathScanner.class);

    private static final String CLASS_FILE_SUFFIX = ".class";
    private static final String PACKAGE_INFO_FILE_NAME = "package-info" + CLASS_FILE_SUFFIX;
    private static final String MODULE_INFO_FILE_NAME = "module-info" + CLASS_FILE_SUFFIX;
    private static final Predicate<Class<?>> NULL_FILTER = aClass -> true;

    private final PathScanner pathScanner = new PathScanner();

    private final Supplier<ClassLoader> classLoaderSupplier;

    public ClasspathScanner(Supplier<ClassLoader> classLoaderSupplier) {
        this.classLoaderSupplier = classLoaderSupplier;
    }

    public <T> List<Class<? extends T>> scanForSubClasses(String packageOrClassName, Class<T> parentClass) {
        Optional<Class<?>> classFromName = safelyLoadClass(packageOrClassName, false);

        return classFromName.isPresent() && !parentClass.equals(classFromName.get())
                && parentClass.isAssignableFrom(classFromName.get())
                        ? Arrays.asList((Class<? extends T>) classFromName.get())
                        : scanForSubClassesInPackage(packageOrClassName, parentClass);
    }

    public <T> List<Class<? extends T>> scanForSubClassesInPackage(String packageName, Class<T> parentClass) {
        return scanForClassesInPackage(packageName, isSubClassOf(parentClass))
                .stream()
                .map(aClass -> (Class<? extends T>) aClass.asSubclass(parentClass))
                .collect(toList());
    }

    private List<Class<?>> scanForClassesInPackage(String packageName, Predicate<Class<?>> classFilter) {
        requireValidPackageName(packageName);
        requireNonNull(classFilter, "classFilter must not be null");
        List<URI> rootUris = getUrisForPackage(getClassLoader(), packageName);
        return findClassesForUris(rootUris, packageName, classFilter);
    }

    private static <T> Predicate<Class<?>> isSubClassOf(Class<T> parentClass) {
        return aClass -> !parentClass.equals(aClass) && parentClass.isAssignableFrom(aClass);
    }

    private ClassLoader getClassLoader() {
        return this.classLoaderSupplier.get();
    }

    private List<Class<?>> findClassesForUris(List<URI> baseUris, String packageName, Predicate<Class<?>> classFilter) {
        return baseUris.stream()
                .map(baseUri -> findClassesForUri(baseUri, packageName, classFilter))
                .flatMap(Collection::stream)
                .distinct()
                .collect(toList());
    }

    private List<Class<?>> findClassesForUri(URI baseUri, String packageName, Predicate<Class<?>> classFilter) {
        List<Class<?>> classes = new ArrayList<>();
        pathScanner.findResourcesForUri(
            baseUri,
            path -> isNotModuleInfo(path) && isNotPackageInfo(path) && isClassFile(path),
            processClassFiles(packageName, classFilter, classes::add));
        return classes;
    }

    private static boolean isNotModuleInfo(Path path) {
        return !path.endsWith(MODULE_INFO_FILE_NAME);
    }

    private static boolean isNotPackageInfo(Path path) {
        return !path.endsWith(PACKAGE_INFO_FILE_NAME);
    }

    private static boolean isClassFile(Path file) {
        return file.getFileName().toString().endsWith(CLASS_FILE_SUFFIX);
    }

    private Function<Path, Consumer<Path>> processClassFiles(
            String basePackageName,
            Predicate<Class<?>> classFilter,
            Consumer<Class<?>> classConsumer
    ) {
        return baseDir -> classFile -> {
            String fqn = determineFullyQualifiedClassName(baseDir, basePackageName, classFile);
            safelyLoadClass(fqn, true)
                    .filter(classFilter)
                    .ifPresent(classConsumer);
        };
    }

    private Optional<Class<?>> safelyLoadClass(String fqn, boolean logWarning) {
        try {
            return Optional.ofNullable(getClassLoader().loadClass(fqn));
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            if (logWarning) {
                log.warn(e, () -> "Failed to load class '" + fqn + "'.\n" + classPathScanningExplanation());
            }
        }
        return Optional.empty();
    }

    public List<Class<?>> scanForClassesInPackage(String packageName) {
        return scanForClassesInPackage(packageName, NULL_FILTER);
    }

    public List<Class<?>> getClasses(String packageOrClassName) {
        Optional<Class<?>> classFromName = safelyLoadClass(packageOrClassName, false);
        return classFromName.isPresent() ? Arrays.asList(classFromName.get())
                : scanForClassesInPackage(packageOrClassName, NULL_FILTER);
    }

}
