package io.cucumber.core.resource;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.walkFileTree;
import static java.util.Collections.emptyMap;

class PathScanner {

    void findResourcesForUri(URI baseUri, Predicate<Path> filter, Function<Path, Consumer<Path>> consumer) {
        try (CloseablePath closeablePath = CloseablePath.create(baseUri)) {
            Path baseDir = closeablePath.getPath();
            findResourcesForPath(baseDir, filter, consumer);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    void findResourcesForPath(Path baseDir, Predicate<Path> filter, Function<Path, Consumer<Path>> consumer) {
        if (!exists(baseDir)) {
            throw new IllegalArgumentException("baseDir must exist: " + baseDir);
        }

        try {
            walkFileTree(baseDir, new ResourceFileVisitor(filter, consumer.apply(baseDir)));
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

    static class CloseablePath implements Closeable {

        private static final Closeable NULL_CLOSEABLE = () -> {
        };

        private final Path path;
        private final Closeable delegate;

        private CloseablePath(Path path, Closeable delegate) {
            this.path = path;
            this.delegate = delegate;
        }

        static CloseablePath create(URI uri) throws IOException, URISyntaxException {

            if (JarUriFileSystemService.supports(uri)) {
                return JarUriFileSystemService.create(uri);
            }

            return new CloseablePath(Paths.get(uri), NULL_CLOSEABLE);
        }

        Path getPath() {
            return path;
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }

    static class JarUriFileSystemService {

        private static final String FILE_URI_SCHEME = "file";
        private static final String JAR_URI_SCHEME = "jar";
        private static final String JAR_URI_SCHEME_PREFIX = JAR_URI_SCHEME + ":";
        private static final String JAR_FILE_EXTENSION = ".jar";
        private static final String JAR_URI_SEPARATOR = "!";

        private static CloseablePath createForJarFileSystem(URI jarUri, Function<FileSystem, Path> pathProvider)
            throws IOException {
            FileSystem fileSystem = FileSystems.newFileSystem(jarUri, emptyMap());
            Path path = pathProvider.apply(fileSystem);
            return new CloseablePath(path, fileSystem);
        }

        static boolean supports(URI uri) {
            return hasJarUriScheme(uri) || hasFileUriSchemeWithJarExtension(uri);
        }

        static CloseablePath create(URI uri) throws URISyntaxException, IOException {
            if (hasJarUriScheme(uri)) {
                String[] parts = uri.toString().split(JAR_URI_SEPARATOR);
                String jarUri = parts[0];
                String jarEntry = parts[1];
                return createForJarFileSystem(new URI(jarUri), fileSystem -> fileSystem.getPath(jarEntry));
            }
            if (hasFileUriSchemeWithJarExtension(uri)) {
                return createForJarFileSystem(new URI(JAR_URI_SCHEME_PREFIX + uri),
                    fileSystem -> fileSystem.getRootDirectories().iterator().next());
            }

            return null;
        }

        private static boolean hasFileUriSchemeWithJarExtension(URI uri) {
            return FILE_URI_SCHEME.equals(uri.getScheme()) && uri.getPath().endsWith(JAR_FILE_EXTENSION);
        }

        private static boolean hasJarUriScheme(URI uri) {
            return JAR_URI_SCHEME.equals(uri.getScheme());
        }
    }

}
