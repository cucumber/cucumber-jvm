package io.cucumber.core.options;

import io.cucumber.core.feature.FeatureWithLines;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;

/**
 * Identifies either:
 * <li>
 * <ul>a single rerun file,</ul>
 * <ul>a directory of containing exclusively rerun files,</ul>
 * <ul>a directory containing feature files,</ul>
 * <ul>a specific feature,</ul>
 * <ul>or specific scenarios and examples (pickles) in a feature</ul>
 * </li>
 * <p>
 * The syntax is either a {@link FeatureWithLines} or an {@code @} followed by a {@link RerunPath}.
 */
class FeatureWithLinesOrRerunPath {

  private final FeatureWithLines featureWithLines;
  private final Collection<FeatureWithLines> featuresWithLinesToRerun;

  FeatureWithLinesOrRerunPath(FeatureWithLines featureWithLines, Collection<FeatureWithLines> featuresWithLinesToRerun) {
    this.featureWithLines = featureWithLines;
    this.featuresWithLinesToRerun = featuresWithLinesToRerun;
  }

  static FeatureWithLinesOrRerunPath parse(String arg) {
    if (arg.startsWith("@")) {
      Path rerunFileOrDirectory = Paths.get(arg.substring(1));
      return new FeatureWithLinesOrRerunPath(null, RerunPath.parse(rerunFileOrDirectory));
    } else {
      return new FeatureWithLinesOrRerunPath(FeatureWithLines.parse(arg), null);
    }
  }

  Optional<Collection<FeatureWithLines>> getFeaturesToRerun() {
    return Optional.ofNullable(featuresWithLinesToRerun);
  }

  Optional<FeatureWithLines> getFeatureWithLines() {
    return Optional.ofNullable(featureWithLines);
  }

}
