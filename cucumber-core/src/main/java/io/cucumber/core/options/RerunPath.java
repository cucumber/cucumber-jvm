package io.cucumber.core.options;

import io.cucumber.core.feature.FeatureWithLines;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Either a path to a rerun file or a directory containing exclusively rerun
 * files.
 */
final class RerunPath {

    private RerunPath() {
        /* no-op */
    }

    static Collection<FeatureWithLines> parse(Path rerunFileOrDirectory) {
        return listRerunFiles(rerunFileOrDirectory).stream()
                .map(FeatureWithLines::parseFile)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private static Set<Path> listRerunFiles(Path path) {
        class FileCollector extends SimpleFileVisitor<Path> {
            final Set<Path> paths = new HashSet<>();

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (!Files.isDirectory(file)) {
                    paths.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        }

        try {
            FileCollector collector = new FileCollector();
            Files.walkFileTree(path, collector);
            return collector.paths;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

}
