package io.cucumber.core.resource;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.cucumber.core.resource.ClasspathSupport.determineFullyQualifiedClassName;
import static io.cucumber.core.resource.ClasspathSupport.getRootUrisForPackage;
import static io.cucumber.core.resource.ClasspathSupport.requireValidPackageName;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public final class ClasspathScanner {

    private static final String CLASS_FILE_SUFFIX = ".class";
    private static final String PACKAGE_INFO_FILE_NAME = "package-info" + CLASS_FILE_SUFFIX;
    private static final String MODULE_INFO_FILE_NAME = "module-info" + CLASS_FILE_SUFFIX;
    private static final Predicate<Class<?>> NULL_FILTER = aClass -> true;

    private final PathScanner pathScanner = new PathScanner();

    private final Supplier<ClassLoader> classLoaderSupplier;

    public ClasspathScanner(Supplier<ClassLoader> classLoaderSupplier) {
        this.classLoaderSupplier = classLoaderSupplier;
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

    private static <T> Predicate<Class<?>> isSubClassOf(Class<T> parentClass) {
        return aClass -> !parentClass.equals(aClass) && parentClass.isAssignableFrom(aClass);
    }

    public <T> List<Class<? extends T>> scanForSubClassesInPackage(String basePackageName, Class<T> parentClass) {
        return scanForClassesInPackage(basePackageName, isSubClassOf(parentClass))
            .stream()
            .map(aClass -> (Class<? extends T>) aClass.asSubclass(parentClass))
            .collect(toList());
    }

    public List<Class<?>> scanForClassesInPackage(String basePackageName) {
        return scanForClassesInPackage(basePackageName, NULL_FILTER);
    }

    private List<Class<?>> scanForClassesInPackage(String basePackageName, Predicate<Class<?>> classFilter) {
        requireValidPackageName(basePackageName);
        requireNonNull(classFilter, "classFilter must not be null");
        basePackageName = basePackageName.trim();
        List<URI> rootUris = getRootUrisForPackage(getClassLoader(), basePackageName);
        return findClassesForUris(rootUris, basePackageName, classFilter);
    }

    private List<Class<?>> findClassesForUris(List<URI> baseUris, String basePackageName, Predicate<Class<?>> classFilter) {
        return baseUris.stream()
            .map(baseUri -> findClassesForUri(baseUri, basePackageName, classFilter))
            .flatMap(Collection::stream)
            .distinct()
            .collect(toList());
    }

    private List<Class<?>> findClassesForUri(URI baseUri, String basePackageName, Predicate<Class<?>> classFilter) {
        List<Class<?>> classes = new ArrayList<>();
        pathScanner.findResourcesForUri(
            baseUri,
            path -> isNotModuleInfo(path) && isNotPackageInfo(path) && isClassFile(path),
            processClassFiles(basePackageName, classFilter, classes::add)
        );
        return classes;
    }

    private Function<Path, Consumer<Path>> processClassFiles(String basePackageName,
                                                             Predicate<Class<?>> classFilter,
                                                             Consumer<Class<?>> classConsumer) {
        return baseDir -> classFile -> {
            String fqn = determineFullyQualifiedClassName(baseDir, basePackageName, classFile);
            try {
                Optional.of(getClassLoader().loadClass(fqn))
                    .filter(classFilter)
                    .ifPresent(classConsumer);
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                throw new IllegalArgumentException("Unable to load " + fqn, e);
            }
        };
    }

    private ClassLoader getClassLoader() {
        return this.classLoaderSupplier.get();
    }

}
