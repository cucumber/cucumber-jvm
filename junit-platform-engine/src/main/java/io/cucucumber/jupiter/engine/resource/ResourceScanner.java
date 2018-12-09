package io.cucucumber.jupiter.engine.resource;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.PackageUtils;
import org.junit.platform.commons.util.Preconditions;

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

import static io.cucucumber.jupiter.engine.resource.ClasspathSupport.DEFAULT_PACKAGE_NAME;
import static io.cucucumber.jupiter.engine.resource.ClasspathSupport.determinePackageName;
import static io.cucucumber.jupiter.engine.resource.ClasspathSupport.getRootUrisForPackage;
import static io.cucucumber.jupiter.engine.resource.ClasspathSupport.getUrisForResource;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.util.BlacklistedExceptions.rethrowIfBlacklisted;

public class ResourceScanner<R> {

    private static final Predicate<String> NULL_FILTER = x -> true;
    private static final Logger logger = LoggerFactory.getLogger(PathScanner.ResourceFileVisitor.class);
    private final PathScanner pathScanner = new PathScanner();
    private final Supplier<ClassLoader> classLoaderSupplier;
    private final ResourceProcessor<R> processor;


    public ResourceScanner(Supplier<ClassLoader> classLoaderSupplier, Predicate<Path> canLoad, BiFunction<Path, Path, Optional<R>> load) {
        this(classLoaderSupplier, new SimpleResourceProcessor<>(canLoad, load));
    }

    public ResourceScanner(Supplier<ClassLoader> classLoaderSupplier, Predicate<Path> canLoad, Function<Path, Optional<R>> load) {
        this(classLoaderSupplier, canLoad, (baseDir, resource) -> load.apply(resource));
    }


    private ResourceScanner(Supplier<ClassLoader> classLoaderSupplier, ResourceProcessor<R> processor) {
        this.classLoaderSupplier = classLoaderSupplier;
        this.processor = processor;
    }

    public List<R> scanForResourcesInClasspathRoot(URI root, Predicate<String> packageFilter) {
        Preconditions.notNull(root, "root must not be null");
        Preconditions.notNull(packageFilter, "packageFilter must not be null");
        return findResourcesForUri(root, DEFAULT_PACKAGE_NAME, packageFilter);
    }

    public List<R> scanForResourcesInPackage(String basePackageName, Predicate<String> packageFilter) {
        PackageUtils.assertPackageNameIsValid(basePackageName);
        Preconditions.notNull(packageFilter, "packageFilter must not be null");
        basePackageName = basePackageName.trim();
        return findResourcesForUris(getRootUrisForPackage(getClassLoader(), basePackageName), basePackageName, packageFilter);
    }

    public List<R> scanForClasspathResource(String resourceName, Predicate<String> packageFilter) {
        Preconditions.notNull(resourceName, "resourceName must not be null");
        Preconditions.notNull(packageFilter, "packageFilter must not be null");
        resourceName = resourceName.trim();
        return findResourcesForUris(getUrisForResource(getClassLoader(), resourceName), DEFAULT_PACKAGE_NAME, packageFilter);
    }

    public List<R> scanForResourcesPath(Path resourcePath) {
        Preconditions.notNull(resourcePath, "path must not be null");
        List<R> classes = new ArrayList<>();
        pathScanner.findResourcesForPath(
            resourcePath,
            processor,
            baseDir -> path -> processResourceFileSafely(baseDir, DEFAULT_PACKAGE_NAME, NULL_FILTER, path, classes::add)
        );
        return classes;
    }

    public List<R> scanForResourcesUri(URI resourcePath) {
        Preconditions.notNull(resourcePath, "path must not be null");
        return findResourcesForUri(resourcePath, DEFAULT_PACKAGE_NAME, x -> true);
    }


    private ClassLoader getClassLoader() {
        return this.classLoaderSupplier.get();
    }

    private List<R> findResourcesForUris(List<URI> baseUris, String basePackageName, Predicate<String> packageFilter) {
        return baseUris.stream()
            .map(baseUri -> findResourcesForUri(baseUri, basePackageName, packageFilter))
            .flatMap(Collection::stream)
            .distinct()
            .collect(toList());
    }

    private List<R> findResourcesForUri(URI baseUri, String basePackageName, Predicate<String> packageFilter) {
        List<R> resources = new ArrayList<>();
        pathScanner.findResourcesForUri(
            baseUri,
            processor,
            baseDir -> resource -> processResourceFileSafely(baseDir, basePackageName, packageFilter, resource, resources::add)
        );
        return resources;
    }

    private void processResourceFileSafely(Path baseDir, String basePackageName, Predicate<String> classFilter,
                                           Path resource, Consumer<R> consumer) {
        try {
            String packageName = determinePackageName(baseDir, basePackageName, resource);
            if (classFilter.test(packageName)) {
                processor.apply(baseDir, resource).ifPresent(consumer);
            }
        } catch (Throwable throwable) {
            handleThrowable(resource, throwable);
        }
    }

    private void handleThrowable(Path classFile, Throwable throwable) {
        rethrowIfBlacklisted(throwable);
        logGenericFeatureProcessingException(classFile, throwable);
    }

    private void logGenericFeatureProcessingException(Path featureFile, Throwable throwable) {
        logger.debug(throwable, () -> format("Failed to load file for path [%s] during scanning.",
            featureFile.toAbsolutePath()));
    }

    interface ResourceProcessor<R> extends Predicate<Path>, BiFunction<Path, Path, Optional<R>> {

    }

    private static class SimpleResourceProcessor<R> implements ResourceProcessor<R> {

        private final Predicate<Path> canProcess;
        private final BiFunction<Path, Path, Optional<R>> load;

        private SimpleResourceProcessor(Predicate<Path> canProcess, BiFunction<Path, Path, Optional<R>> load) {
            this.canProcess = canProcess;
            this.load = load;
        }

        @Override
        public Optional<R> apply(Path basePath, Path path) {
            return load.apply(basePath, path);
        }

        @Override
        public boolean test(Path path) {
            return canProcess.test(path);
        }
    }

}
