package io.cucumber.junit.platform.engine;

import io.cucumber.core.resource.PathScanner;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.support.discovery.SelectorResolver;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectFile;

class FileContainerSelectorResolver implements SelectorResolver {

    private final PathScanner pathScanner = new PathScanner();
    private final Predicate<Path> filter;

    FileContainerSelectorResolver(Predicate<Path> filter) {
        this.filter = filter;
    }

    @Override
    public Resolution resolve(DirectorySelector selector, Context context) {
        Set<DiscoverySelector> selectors = new HashSet<>();
        pathScanner.findResourcesForPath(selector.getPath(), filter, path -> selectors.add(selectFile(path.toFile())));
        if (selectors.isEmpty()) {
            return Resolution.unresolved();
        }
        return Resolution.selectors(selectors);
    }
}
