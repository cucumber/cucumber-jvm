package io.cucumber.junit.platform.engine;

import io.cucumber.core.logging.LogRecordListener;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.junit.platform.engine.nofeatures.NoFeatures;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.FilePosition;
import org.junit.platform.engine.discovery.UniqueIdSelector;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathResource;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathRoots;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectDirectory;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectFile;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUri;

class DiscoverySelectorResolverTest {

    private final DiscoverySelectorResolver resolver = new DiscoverySelectorResolver();
    private final LogRecordListener logRecordListener = new LogRecordListener();
    private CucumberEngineDescriptor testDescriptor;

    @BeforeEach
    void before() {
        LoggerFactory.addListener(logRecordListener);
        UniqueId id = UniqueId.forEngine(new CucumberTestEngine().getId());
        testDescriptor = new CucumberEngineDescriptor(id);
        assertEquals(0, testDescriptor.getChildren().size());
    }

    @AfterEach
    void after() {
        LoggerFactory.removeListener(logRecordListener);
    }

    @Test
    void resolveRequestWithClasspathResourceSelector() {
        DiscoverySelector resource = selectClasspathResource("io/cucumber/junit/platform/engine/single.feature");
        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);
        assertEquals(1, testDescriptor.getChildren().size());
    }

    @Test
    void resolveRequestWithClasspathResourceSelectorAndFilePosition() {
        String feature = "io/cucumber/junit/platform/engine/rule.feature";
        FilePosition line = FilePosition.from(5);
        DiscoverySelector resource = selectClasspathResource(feature, line);
        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);
        assertEquals(1L, testDescriptor.getDescendants()
                .stream()
                .filter(TestDescriptor::isTest)
                .count());
    }

    @Test
    void resolveRequestWithClasspathResourceSelectorAndFilePositionOfContainer() {
        String feature = "io/cucumber/junit/platform/engine/rule.feature";
        FilePosition line = FilePosition.from(3);
        DiscoverySelector resource = selectClasspathResource(feature, line);
        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);
        assertEquals(2L, testDescriptor.getDescendants()
                .stream()
                .filter(TestDescriptor::isTest)
                .count());
    }

    @Test
    void resolveRequestWithMultipleClasspathResourceSelector() {
        DiscoverySelector resource1 = selectClasspathResource("io/cucumber/junit/platform/engine/single.feature");
        DiscoverySelector resource2 = selectClasspathResource(
            "io/cucumber/junit/platform/engine/feature-with-outline.feature");
        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(resource1, resource2);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);
        assertEquals(2, testDescriptor.getChildren().size());
    }

    @Test
    void resolveRequestWithClasspathRootSelector() {
        Path classpathRoot = Paths.get("src/test/resources/");
        DiscoverySelector resource = selectClasspathRoots(singleton(classpathRoot)).get(0);
        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);
        assertEquals(6, testDescriptor.getChildren().size());
    }

    @Test
    void resolveFeatureTestDescriptorsInUriOrder() {
        Path classpathRoot = Paths.get("src/test/resources/");
        DiscoverySelector resource = selectClasspathRoots(singleton(classpathRoot)).get(0);
        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);

        Set<? extends TestDescriptor> features = testDescriptor.getChildren();
        List<TestDescriptor> unsorted = new ArrayList<>(features);
        List<TestDescriptor> sorted = new ArrayList<>(features);
        // Sorts by URI
        sorted.sort(comparing(feature -> feature.getUniqueId().getSegments().get(1).getValue()));
        assertEquals(unsorted, sorted);
    }

    @Test
    void resolveRequestWithUriSelectorWithScenarioOutlineLine() {
        File file = new File("src/test/resources/io/cucumber/junit/platform/engine/feature-with-outline.feature");
        URI uri = URI.create(file.toURI() + "?line=11");
        DiscoverySelector resource = selectUri(uri);
        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);
        List<? extends TestDescriptor> tests = testDescriptor.getDescendants().stream()
                .filter(TestDescriptor::isTest)
                .collect(Collectors.toList());
        assertEquals(4, tests.size()); // 4 examples in the outline
    }

    @Test
    void resolveRequestWithUriSelectorWithExamplesSectionLine() {
        File file = new File("src/test/resources/io/cucumber/junit/platform/engine/feature-with-outline.feature");
        URI uri = URI.create(file.toURI() + "?line=17");
        DiscoverySelector resource = selectUri(uri);
        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);
        List<? extends TestDescriptor> tests = testDescriptor.getDescendants().stream()
                .filter(TestDescriptor::isTest)
                .collect(Collectors.toList());
        assertEquals(2, tests.size()); // 2 examples in the examples section
    }

    @Test
    void resolveRequestWithUriSelectorWithExampleLine() {
        File file = new File("src/test/resources/io/cucumber/junit/platform/engine/feature-with-outline.feature");
        URI uri1 = URI.create(file.toURI() + "?line=20");
        DiscoverySelector resource = selectUri(uri1);
        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);
        List<? extends TestDescriptor> tests = testDescriptor.getDescendants().stream()
                .filter(TestDescriptor::isTest)
                .collect(Collectors.toList());
        assertEquals(1, tests.size());
    }

    @Test
    void resolveRequestWithClassPathUriSelectorWithLine() {
        URI uri = URI.create("classpath:/io/cucumber/junit/platform/engine/feature-with-outline.feature?line=20");
        DiscoverySelector resource = selectUri(uri);
        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);
        List<? extends TestDescriptor> tests = testDescriptor.getDescendants().stream()
                .filter(TestDescriptor::isTest)
                .collect(Collectors.toList());
        assertEquals(1, tests.size());
    }

    @Test
    void resolveRequestWithFileSelector() {
        DiscoverySelector resource = selectFile("src/test/resources/io/cucumber/junit/platform/engine/single.feature");
        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);
        assertEquals(1, testDescriptor.getChildren().size());
    }

    @Test
    void resolveRequestWithFileSelectorAndPosition() {
        String feature = "src/test/resources/io/cucumber/junit/platform/engine/rule.feature";
        FilePosition line = FilePosition.from(5);
        DiscoverySelector resource = selectFile(feature, line);
        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);
        assertEquals(1L, testDescriptor.getDescendants()
                .stream()
                .filter(TestDescriptor::isTest)
                .count());
    }

    @Test
    void resolveRequestWithFileSelectorAndPositionOfContainer() {
        String feature = "src/test/resources/io/cucumber/junit/platform/engine/rule.feature";
        FilePosition line = FilePosition.from(3);
        DiscoverySelector resource = selectFile(feature, line);
        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);
        assertEquals(2L, testDescriptor.getDescendants()
                .stream()
                .filter(TestDescriptor::isTest)
                .count());
    }

    @Test
    void resolveRequestWithDirectorySelector() {
        DiscoverySelector resource = selectDirectory("src/test/resources/io/cucumber/junit/platform/engine");
        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);
        assertEquals(5, testDescriptor.getChildren().size());
    }

    @Test
    void resolveRequestWithPackageSelector() {
        DiscoverySelector resource = selectPackage("io.cucumber.junit.platform.engine");
        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);
        assertEquals(5, testDescriptor.getChildren().size());
    }

    @Test
    void resolveRequestWithUniqueIdSelectorFromClasspath() {
        DiscoverySelector resource = selectPackage("io.cucumber.junit.platform.engine");
        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);

        Set<? extends TestDescriptor> descendants = testDescriptor.getDescendants();

        descendants.forEach(targetDescriptor -> {
            resetTestDescriptor();
            resolveRequestWithUniqueIdSelector(targetDescriptor.getUniqueId());
            assertEquals(1, testDescriptor.getChildren().size());
            assertThat(testDescriptor, allDescriptorsPrefixedBy(targetDescriptor.getUniqueId()));
        });
    }

    private void resetTestDescriptor() {
        Set<? extends TestDescriptor> descendants = new HashSet<>(testDescriptor.getDescendants());
        descendants.forEach(o -> testDescriptor.removeChild(o));
    }

    private void resolveRequestWithUniqueIdSelector(UniqueId targetId) {
        UniqueIdSelector uniqueIdSelector = selectUniqueId(targetId);
        EngineDiscoveryRequest descendantRequest = new SelectorRequest(uniqueIdSelector);
        resolver.resolveSelectors(descendantRequest, testDescriptor);
    }

    private static Matcher<TestDescriptor> allDescriptorsPrefixedBy(UniqueId targetId) {
        return new CustomTypeSafeMatcher<TestDescriptor>("All descendants are prefixed by " + targetId) {
            @Override
            protected boolean matchesSafely(TestDescriptor descriptor) {
                return descriptor.getDescendants()
                        .stream()
                        .filter(TestDescriptor::isTest)
                        .map(TestDescriptor::getUniqueId)
                        .allMatch(selectedId -> selectedId.hasPrefix(targetId));
            }
        };
    }

    @Test
    void resolveRequestWithUniqueIdSelectorFromFileUri() {
        DiscoverySelector resource = selectDirectory("src/test/resources/io/cucumber/junit/platform/engine");
        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);

        Set<? extends TestDescriptor> descendants = testDescriptor.getDescendants();

        descendants.forEach(targetDescriptor -> {
            resetTestDescriptor();
            resolveRequestWithUniqueIdSelector(targetDescriptor.getUniqueId());
            assertEquals(1, testDescriptor.getChildren().size());
            assertThat(testDescriptor, allDescriptorsPrefixedBy(targetDescriptor.getUniqueId()));
        });
    }

    @Test
    void resolveRequestWithUniqueIdSelectorFromJarFileUri() {
        URI uri = new File("src/test/resources/feature.jar").toURI();
        DiscoverySelector resource = selectUri(uri);
        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);

        assertEquals(1, testDescriptor.getChildren().size());
    }

    @Test
    void resolveRequestWithUniqueIdSelectorFromJarUri() {
        String root = Paths.get("").toAbsolutePath().toUri().getSchemeSpecificPart();
        URI uri = URI.create("jar:file:" + root + "/src/test/resources/feature.jar!/single.feature");

        DiscoverySelector resource = selectUri(uri);
        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);

        assertEquals(1, testDescriptor.getChildren().size());
    }

    @Test
    void resolveRequestWithMultipleUniqueIdSelector() {
        Set<UniqueId> selectors = new HashSet<>();

        DiscoverySelector resource = selectDirectory(
            "src/test/resources/io/cucumber/junit/platform/engine/feature-with-outline.feature");
        selectSomePickle(resource).ifPresent(selectors::add);

        DiscoverySelector resource2 = selectDirectory(
            "src/test/resources/io/cucumber/junit/platform/engine/single.feature");
        selectSomePickle(resource2).ifPresent(selectors::add);

        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(
            selectors.stream()
                    .map(DiscoverySelectors::selectUniqueId)
                    .collect(Collectors.toList()));

        resolver.resolveSelectors(discoveryRequest, testDescriptor);

        assertEquals(
            selectors,
            testDescriptor.getDescendants()
                    .stream()
                    .filter(PickleDescriptor.class::isInstance)
                    .map(TestDescriptor::getUniqueId)
                    .collect(toSet()));
    }

    private Optional<UniqueId> selectSomePickle(DiscoverySelector resource) {
        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);
        Set<? extends TestDescriptor> descendants = testDescriptor.getDescendants();
        resetTestDescriptor();
        return descendants.stream()
                .filter(PickleDescriptor.class::isInstance)
                .map(TestDescriptor::getUniqueId)
                .findFirst();
    }

    @Test
    void resolveRequestWithClassSelector() {
        DiscoverySelector resource = selectClass(RunCucumberTest.class);
        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);
        assertEquals(5, testDescriptor.getChildren().size());
    }

    @Test
    void resolveRequestWithClassSelectorShouldLogWarnIfNoFeaturesFound() {
        DiscoverySelector resource = selectClass(NoFeatures.class);
        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);
        assertEquals(0, testDescriptor.getChildren().size());
        assertEquals(1, logRecordListener.getLogRecords().size());
        LogRecord logRecord = logRecordListener.getLogRecords().get(0);
        assertEquals(Level.WARNING, logRecord.getLevel());
        assertEquals("No features found in package 'io.cucumber.junit.platform.engine.nofeatures'",
            logRecord.getMessage());
    }

    private static class SelectorRequest implements EngineDiscoveryRequest {

        private final Map<Class<?>, List<DiscoverySelector>> resources = new HashMap<>();

        SelectorRequest(DiscoverySelector... selectors) {
            this(Arrays.asList(selectors));
        }

        SelectorRequest(List<DiscoverySelector> selectors) {
            for (DiscoverySelector discoverySelector : selectors) {
                resources.putIfAbsent(discoverySelector.getClass(), new ArrayList<>());
                resources.get(discoverySelector.getClass()).add(discoverySelector);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends DiscoverySelector> List<T> getSelectorsByType(Class<T> selectorType) {
            if (resources.containsKey(selectorType)) {
                return (List<T>) resources.get(selectorType);
            }

            return Collections.emptyList();
        }

        @Override
        public <T extends DiscoveryFilter<?>> List<T> getFiltersByType(Class<T> filterType) {
            return Collections.emptyList();
        }

        @Override
        public ConfigurationParameters getConfigurationParameters() {
            return new EmptyConfigurationParameters();
        }

    }

}
