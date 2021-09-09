package io.cucumber.core.resource;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME;
import static io.cucumber.core.resource.ClasspathSupport.DEFAULT_PACKAGE_NAME;
import static io.cucumber.core.resource.ClasspathSupport.determinePackageName;
import static io.cucumber.core.resource.ClasspathSupport.getUrisForPackage;
import static io.cucumber.core.resource.ClasspathSupport.getUrisForResource;
import static io.cucumber.core.resource.ClasspathSupport.requireValidPackageName;
import static io.cucumber.core.resource.ClasspathSupport.resourceName;
import static io.cucumber.core.resource.Resources.createClasspathResource;
import static io.cucumber.core.resource.Resources.createClasspathRootResource;
import static io.cucumber.core.resource.Resources.createPackageResource;
import static io.cucumber.core.resource.Resources.createUriResource;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public final class ResourceScanner<R> {

    private static final Predicate<String> NULL_FILTER = x -> true;
    private final PathScanner pathScanner = new PathScanner();
    private final Supplier<ClassLoader> classLoaderSupplier;
    private final Predicate<Path> canLoad;
    private final Function<Resource, Optional<R>> loadResource;

    public ResourceScanner(
            Supplier<ClassLoader> classLoaderSupplier,
            Predicate<Path> canLoad,
            Function<Resource, Optional<R>> loadResource
    ) {
        this.classLoaderSupplier = classLoaderSupplier;
        this.canLoad = canLoad;
        this.loadResource = loadResource;
    }

    public List<R> scanForResourcesInClasspathRoot(URI root, Predicate<String> packageFilter) {
        requireNonNull(root, "root must not be null");
        requireNonNull(packageFilter, "packageFilter must not be null");
        BiFunction<Path, Path, Resource> createResource = createClasspathRootResource();
        return findResourcesForUri(root, DEFAULT_PACKAGE_NAME, packageFilter, createResource);
    }

    private List<R> findResourcesForUri(
            URI baseUri,
            String basePackageName,
            Predicate<String> packageFilter,
            BiFunction<Path, Path, Resource> createResource
    ) {
        List<R> resources = new ArrayList<>();
        pathScanner.findResourcesForUri(
            baseUri,
            canLoad,
            processResource(basePackageName, packageFilter, createResource, resources::add));
        return resources;
    }

    private Function<Path, Consumer<Path>> processResource(
            String basePackageName,
            Predicate<String> packageFilter,
            BiFunction<Path, Path, Resource> createResource,
            Consumer<R> consumer
    ) {
        return baseDir -> path -> {
            String packageName = determinePackageName(baseDir, basePackageName, path);
            if (packageFilter.test(packageName)) {
                createResource
                        .andThen(loadResource)
                        .apply(baseDir, path)
                        .ifPresent(consumer);
            }
        };
    }

    public List<R> scanForResourcesInPackage(String packageName, Predicate<String> packageFilter) {
        requireValidPackageName(packageName);
        requireNonNull(packageFilter, "packageFilter must not be null");
        BiFunction<Path, Path, Resource> createResource = createPackageResource(packageName);
        List<URI> rootUrisForPackage = getUrisForPackage(getClassLoader(), packageName);
        return findResourcesForUris(rootUrisForPackage, packageName, packageFilter, createResource);
    }

    private ClassLoader getClassLoader() {
        return this.classLoaderSupplier.get();
    }

    private List<R> findResourcesForUris(
            List<URI> baseUris,
            String basePackageName,
            Predicate<String> packageFilter,
            BiFunction<Path, Path, Resource> createResource
    ) {
        return baseUris.stream()
                .map(baseUri -> findResourcesForUri(baseUri, basePackageName, packageFilter, createResource))
                .flatMap(Collection::stream)
                .distinct()
                .collect(toList());
    }

    public List<R> scanForClasspathResource(String resourceName, Predicate<String> packageFilter) {
        requireNonNull(resourceName, "resourceName must not be null");
        requireNonNull(packageFilter, "packageFilter must not be null");
        List<URI> urisForResource = getUrisForResource(getClassLoader(), resourceName);
        BiFunction<Path, Path, Resource> createResource = createClasspathResource(resourceName);
        return findResourcesForUris(urisForResource, DEFAULT_PACKAGE_NAME, packageFilter, createResource);
    }

    public List<R> scanForResourcesPath(Path resourcePath) {
        requireNonNull(resourcePath, "resourcePath must not be null");
        List<R> resources = new ArrayList<>();
        pathScanner.findResourcesForPath(
            resourcePath,
            canLoad,
            processResource(DEFAULT_PACKAGE_NAME, NULL_FILTER, createUriResource(), resources::add));
        return resources;
    }

    public List<R> scanForResourcesUri(URI classpathResourceUri) {
        requireNonNull(classpathResourceUri, "classpathResourceUri must not be null");
        if (CLASSPATH_SCHEME.equals(classpathResourceUri.getScheme())) {
            return scanForClasspathResource(resourceName(classpathResourceUri), NULL_FILTER);
        }

        return findResourcesForUri(classpathResourceUri, DEFAULT_PACKAGE_NAME, NULL_FILTER, createUriResource());
    }

}
