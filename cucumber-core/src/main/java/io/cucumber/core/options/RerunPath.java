package io.cucumber.core.options;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.feature.FeatureWithLines;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.nio.file.Files.readAllLines;

/**
 * Either a path to a rerun file or a directory containing exclusively rerun files.
 */
class RerunPath {

  private static final Pattern RERUN_PATH_SPECIFICATION = Pattern.compile("(?m:^| |)(.*?\\.feature(?:(?::\\d+)*))");

  static Collection<FeatureWithLines> parse(Path rerunFileOrDirectory) {
    return listRerunFiles(rerunFileOrDirectory).stream()
        .map(RerunPath::parseFeatureWithLinesFile)
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

  private static Collection<FeatureWithLines> parseFeatureWithLinesFile(Path path) {
    try {
      List<FeatureWithLines> featurePaths = new ArrayList<>();
      readAllLines(path).forEach(line -> {
        Matcher matcher = RERUN_PATH_SPECIFICATION.matcher(line);
        while (matcher.find()) {
          featurePaths.add(FeatureWithLines.parse(matcher.group(1)));
        }
      });
      return featurePaths;
    } catch (Exception e) {
      throw new CucumberException(format("Failed to parse '%s'", path), e);
    }
  }
}
