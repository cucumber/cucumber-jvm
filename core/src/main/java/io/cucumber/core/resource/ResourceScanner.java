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

import static io.cucumber.core.resource.Classpath.resourceName;
import static io.cucumber.core.resource.Resources.createClasspathResource;
import static io.cucumber.core.resource.Resources.createClasspathRootResource;
import static io.cucumber.core.resource.Resources.createPackageResource;
import static io.cucumber.core.resource.Resources.createUriResource;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public final class ResourceScanner implements UriResourceScanner {

    private static final Predicate<String> NULL_FILTER = x -> true;
    private final PathScanner pathScanner = new PathScanner();
    private final Supplier<ClassLoader> classLoaderSupplier;
    private final Predicate<Path> canLoad;

    public ResourceScanner(Supplier<ClassLoader> classLoaderSupplier, Predicate<Path> canLoad) {
        this.classLoaderSupplier = classLoaderSupplier;
        this.canLoad = canLoad;
    }

    public <R> List<R> scanForResourcesInClasspathRoot(URI root, Predicate<String> packageFilter, Function<Resource, Optional<R>> loadResource) {
        requireNonNull(root, "root must not be null");
        requireNonNull(packageFilter, "packageFilter must not be null");
        BiFunction<Path, Path, Resource> createResource = createClasspathRootResource();
        BiFunction<Path, Path, Optional<R>> loadAndCreate = createResource.andThen(loadResource);
        return findResourcesForUri(root, ClasspathSupport.DEFAULT_PACKAGE_NAME, packageFilter, loadAndCreate);
    }

    public <R> List<R> scanForResourcesInPackage(String basePackageName, Predicate<String> packageFilter, Function<Resource, Optional<R>> loadResource) {
        ClasspathSupport.requireValidPackageName(basePackageName);
        requireNonNull(packageFilter, "packageFilter must not be null");
        basePackageName = basePackageName.trim();
        BiFunction<Path, Path, Resource> createResource = createPackageResource(basePackageName);
        List<URI> rootUrisForPackage = ClasspathSupport.getRootUrisForPackage(getClassLoader(), basePackageName);
        BiFunction<Path, Path, Optional<R>> createThenLoad = createResource.andThen(loadResource);
        return findResourcesForUris(rootUrisForPackage, basePackageName, packageFilter, createThenLoad);
    }

    public <R> List<R> scanForClasspathResource(String resourceName, Function<Resource, Optional<R>> loadResource) {
        return scanForClasspathResource(resourceName, NULL_FILTER, loadResource);
    }

    public <R> List<R> scanForClasspathResource(String resourceName, Predicate<String> packageFilter, Function<Resource, Optional<R>> loadResource) {
        requireNonNull(resourceName, "resourceName must not be null");
        requireNonNull(packageFilter, "packageFilter must not be null");
        resourceName = resourceName.trim();
        List<URI> urisForResource = ClasspathSupport.getUrisForResource(getClassLoader(), resourceName);
        BiFunction<Path, Path, Resource> createResource = createClasspathResource(resourceName);
        BiFunction<Path, Path, Optional<R>> createThenLoad = createResource.andThen(loadResource);
        return findResourcesForUris(urisForResource, ClasspathSupport.DEFAULT_PACKAGE_NAME, packageFilter, createThenLoad);
    }

    @Override
    public <R> List<R> scanForResourcesPath(Path resourcePath, Function<Resource, Optional<R>> loadResource) {
        requireNonNull(resourcePath, "resourcePath must not be null");
        List<R> resources = new ArrayList<>();
        pathScanner.findResourcesForPath(
            resourcePath,
            canLoad,
            processResource(ClasspathSupport.DEFAULT_PACKAGE_NAME, NULL_FILTER, createUriResource().andThen(loadResource), resources::add)
        );
        return resources;
    }

    public <R> List<R> scanForResourcesUri(URI resourcePath, Function<Resource, Optional<R>> loadResource) {
        requireNonNull(resourcePath, "resourcePath must not be null");
        if (Classpath.CLASSPATH_SCHEME.equals(resourcePath.getScheme())) {
            return scanForClasspathResource(resourceName(resourcePath), loadResource);
        }

        return findResourcesForUri(resourcePath, ClasspathSupport.DEFAULT_PACKAGE_NAME, NULL_FILTER, createUriResource().andThen(loadResource));
    }

    private ClassLoader getClassLoader() {
        return this.classLoaderSupplier.get();
    }

    private <R> List<R> findResourcesForUris(List<URI> baseUris, String basePackageName, Predicate<String> packageFilter, BiFunction<Path, Path, Optional<R>> createResource) {
        return baseUris.stream()
            .map(baseUri -> findResourcesForUri(baseUri, basePackageName, packageFilter, createResource))
            .flatMap(Collection::stream)
            .distinct()
            .collect(toList());
    }

    private <R> List<R> findResourcesForUri(URI baseUri, String basePackageName, Predicate<String> packageFilter, BiFunction<Path, Path, Optional<R>> createResource) {
        List<R> resources = new ArrayList<>();
        pathScanner.findResourcesForUri(
            baseUri,
            canLoad,
            processResource(basePackageName, packageFilter, createResource, resources::add)
        );
        return resources;
    }

    private <R> Function<Path, Consumer<Path>> processResource(String basePackageName,
                                                               Predicate<String> packageFilter,
                                                               BiFunction<Path, Path, Optional<R>> createResource,
                                                               Consumer<R> consumer
    ) {
        return baseDir -> path -> {
            String packageName = ClasspathSupport.determinePackageName(baseDir, basePackageName, path);
            if (packageFilter.test(packageName)) {
                createResource.apply(baseDir, path).ifPresent(consumer);
            }
        };
    }

}
