package io.cucumber.core.resource;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.walkFileTree;

class PathScanner {

    void findResourcesForUri(URI baseUri, Predicate<Path> filter, Function<Path, Consumer<Path>> consumer) {
        try (CloseablePath closeablePath = open(baseUri)) {
            Path baseDir = closeablePath.getPath();
            findResourcesForPath(baseDir, filter, consumer);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private CloseablePath open(URI uri) throws IOException, URISyntaxException {
        if (JarUriFileSystemService.supports(uri)) {
            return JarUriFileSystemService.open(uri);
        }

        return CloseablePath.open(uri);
    }

    void findResourcesForPath(Path path, Predicate<Path> filter, Function<Path, Consumer<Path>> consumer) {
        if (!exists(path)) {
            throw new IllegalArgumentException("path must exist: " + path);
        }

        try {
            walkFileTree(path, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                new ResourceFileVisitor(filter, consumer.apply(path)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static class ResourceFileVisitor extends SimpleFileVisitor<Path> {

        private static final Logger logger = LoggerFactory.getLogger(ResourceFileVisitor.class);

        private final Predicate<Path> resourceFileFilter;
        private final Consumer<Path> resourceFileConsumer;

        ResourceFileVisitor(Predicate<Path> resourceFileFilter, Consumer<Path> resourceFileConsumer) {
            this.resourceFileFilter = resourceFileFilter;
            this.resourceFileConsumer = resourceFileConsumer;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
            if (resourceFileFilter.test(file)) {
                resourceFileConsumer.accept(file);
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException e) {
            logger.warn(e, () -> "IOException visiting file: " + file);
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException e) {
            if (e != null) {
                logger.warn(e, () -> "IOException visiting directory: " + dir);
            }
            return CONTINUE;
        }

    }

}
