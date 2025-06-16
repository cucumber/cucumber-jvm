package io.cucumber.core.resource;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import org.apiguardian.api.API;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
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
import static org.apiguardian.api.API.Status.INTERNAL;

@API(status = INTERNAL)
public class PathScanner {

    private static final Logger log = LoggerFactory.getLogger(PathScanner.class);

    void findResourcesForUri(URI baseUri, Predicate<Path> filter, Function<Path, Consumer<Path>> consumer) {
        try (CloseablePath closeablePath = open(baseUri)) {
            Path baseDir = closeablePath.getPath();
            findResourcesForPath(baseDir, filter, consumer);
        } catch (FileSystemNotFoundException e) {
            log.warn(e, () -> "Failed to find resources for '" + baseUri + "'");
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
        findResourcesForPath(path, filter, consumer.apply(path));
    }

    public void findResourcesForPath(Path path, Predicate<Path> filter, Consumer<Path> consumer) {
        try {
            EnumSet<FileVisitOption> options = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
            ResourceFileVisitor visitor = new ResourceFileVisitor(filter, consumer);
            walkFileTree(path, options, Integer.MAX_VALUE, visitor);
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
