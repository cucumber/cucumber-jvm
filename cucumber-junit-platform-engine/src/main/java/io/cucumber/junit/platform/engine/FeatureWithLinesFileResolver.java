package io.cucumber.junit.platform.engine;

import io.cucumber.junit.platform.engine.CucumberDiscoverySelectors.FeatureWithLinesSelector;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.support.discovery.SelectorResolver;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static io.cucumber.core.feature.FeatureWithLines.parseFile;

final class FeatureWithLinesFileResolver implements SelectorResolver {

    static boolean isTxtFile(Path path) {
        return path.getFileName().toString().endsWith(".txt");
    }

    @Override
    public Resolution resolve(FileSelector selector, Context context) {
        Path path = selector.getPath();
        if (!isTxtFile(path)) {
            return Resolution.unresolved();
        }

        Set<FeatureWithLinesSelector> selectors = parseFile(path)
                .stream()
                .map(FeatureWithLinesSelector::from)
                .collect(Collectors.toSet());
        return Resolution.selectors(selectors);
    }
}
