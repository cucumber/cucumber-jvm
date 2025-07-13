package io.cucumber.junit.platform.engine;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.logging.LogRecordListener;
import io.cucumber.junit.platform.engine.CucumberTestDescriptor.FeatureDescriptor;
import io.cucumber.junit.platform.engine.CucumberTestDescriptor.PickleDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.support.Resource;
import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.FilePosition;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.ClasspathResourceSource;
import org.junit.platform.engine.support.descriptor.FileSource;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource;
import org.junit.platform.engine.support.hierarchical.Node;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.testkit.engine.EngineDiscoveryResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static io.cucumber.junit.platform.engine.Constants.EXECUTION_EXCLUSIVE_RESOURCES_PREFIX;
import static io.cucumber.junit.platform.engine.Constants.EXECUTION_MODE_FEATURE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.EXECUTION_ORDER_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.EXECUTION_ORDER_RANDOM_SEED_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.FEATURES_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.FILTER_NAME_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.JUNIT_PLATFORM_DISCOVERY_AS_ROOT_ENGINE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.JUNIT_PLATFORM_LONG_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.JUNIT_PLATFORM_SHORT_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.READ_SUFFIX;
import static io.cucumber.junit.platform.engine.Constants.READ_WRITE_SUFFIX;
import static io.cucumber.junit.platform.engine.CucumberEngineDescriptor.ENGINE_ID;
import static io.cucumber.junit.platform.engine.CucumberEventConditions.emptySource;
import static io.cucumber.junit.platform.engine.CucumberEventConditions.engine;
import static io.cucumber.junit.platform.engine.CucumberEventConditions.example;
import static io.cucumber.junit.platform.engine.CucumberEventConditions.examples;
import static io.cucumber.junit.platform.engine.CucumberEventConditions.feature;
import static io.cucumber.junit.platform.engine.CucumberEventConditions.prefix;
import static io.cucumber.junit.platform.engine.CucumberEventConditions.rule;
import static io.cucumber.junit.platform.engine.CucumberEventConditions.scenario;
import static io.cucumber.junit.platform.engine.CucumberEventConditions.source;
import static io.cucumber.junit.platform.engine.CucumberEventConditions.tags;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.UniqueId.forEngine;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathResource;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathRoots;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectDirectory;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectFile;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUri;
import static org.junit.platform.engine.discovery.PackageNameFilter.includePackageNames;
import static org.junit.platform.engine.support.descriptor.FilePosition.from;
import static org.junit.platform.engine.support.hierarchical.Node.ExecutionMode.CONCURRENT;
import static org.junit.platform.engine.support.hierarchical.Node.ExecutionMode.SAME_THREAD;
import static org.junit.platform.testkit.engine.EventConditions.displayName;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.skippedWithReason;
import static org.junit.platform.testkit.engine.EventConditions.test;

// TODO: Split out tests to multiple classes, but do use EngineTestKit everywhere

@WithLogRecordListener
class CucumberTestEngineTest {

    private final CucumberTestEngine engine = new CucumberTestEngine();

    private static Set<UniqueId> discoverUniqueIds(DiscoverySelector discoverySelector) {
        return EngineTestKit.engine(ENGINE_ID)
                .selectors(discoverySelector)
                .execute()
                .allEvents()
                .map(Event::getTestDescriptor)
                .filter(Predicate.not(TestDescriptor::isRoot))
                .map(TestDescriptor::getUniqueId)
                .collect(toSet());
    }

    @Test
    void id() {
        assertEquals(ENGINE_ID, engine.getId());
    }

    @Test
    void version() {
        assertEquals(Optional.of("DEVELOPMENT"), engine.getVersion());
    }

    @Test
    void empty() {
        EngineTestKit.engine(ENGINE_ID)
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(0, event(test()));
    }

    @Test
    void notCucumber() {
        EngineTestKit.engine(ENGINE_ID)
                .selectors(selectUniqueId(forEngine("not-cucumber")))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(0, event(test()));
    }

    @Test
    void supportsClassSelector() {
        EngineTestKit.engine(ENGINE_ID)
                .selectors(selectClass(RunCucumberTest.class))
                .execute()
                .containerEvents()
                .assertEventsMatchLooselyInOrder(
                    feature("disabled.feature"),
                    feature("empty-scenario.feature"),
                    feature("scenario-outline.feature"),
                    feature("rule.feature"),
                    feature("single.feature"),
                    feature("with%20space.feature"));
    }

    @Test
    void warnsAboutClassSelector() {
        EngineDiscoveryResults results = EngineTestKit.engine(ENGINE_ID)
                .selectors(selectClass(RunCucumberTest.class))
                .discover();

        DiscoveryIssue discoveryIssue = results.getDiscoveryIssues().get(0);
        assertThat(discoveryIssue.message())
                .isEqualTo("The @Cucumber annotation has been deprecated. See the Javadoc for more details.");
    }

    @Test
    void supportsClasspathResourceSelector() {
        EngineTestKit.engine(ENGINE_ID)
                .selectors(selectClasspathResource("io/cucumber/junit/platform/engine/single.feature"))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(1, event( //
                    scenario("scenario:3", "A single scenario"), //
                    finishedSuccessfully()));
    }

    @Test
    void warnWhenResourceSelectorIsUsedToSelectAPackage() {
        EngineTestKit.Builder selectors = EngineTestKit.engine(ENGINE_ID)
                .selectors(selectClasspathResource("io/cucumber/junit/platform/engine"));

        EngineDiscoveryResults discoveryResults = selectors.discover();
        DiscoveryIssue discoveryIssue = discoveryResults.getDiscoveryIssues().get(0);
        assertThat(discoveryIssue.message())
                .isEqualTo(
                    "The classpath resource selector 'io/cucumber/junit/platform/engine' should not be " +
                            "used to select features in a package. Use the package selector with " +
                            "'io.cucumber.junit.platform.engine' instead");

        // It should also still work
        selectors
                .execute()
                .allEvents()
                .assertEventsMatchLooselyInOrder(
                    feature("disabled.feature"),
                    feature("empty-scenario.feature"),
                    feature("scenario-outline.feature"),
                    feature("rule.feature"),
                    feature("single.feature"),
                    feature("with%20space.feature"));

    }

    @Test
    void classpathResourceSelectorThrowIfDuplicateResources() {
        class TestResource implements Resource {

            private final String name;
            private final File source;

            TestResource(String name, File source) {
                this.name = name;
                this.source = source;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public URI getUri() {
                return source.toURI();
            }
        }
        Set<Resource> resources = new LinkedHashSet<>(Arrays.asList(
            new TestResource("io/cucumber/junit/platform/engine/single.feature",
                new File("src/test/resources/io/cucumber/junit/platform/engine/single.feature")),
            new TestResource("io/cucumber/junit/platform/engine/single.feature",
                new File("src/test/resources/io/cucumber/junit/platform/engine/single.feature")),
            new TestResource("io/cucumber/junit/platform/engine/single.feature",
                new File("src/test/resources/io/cucumber/junit/platform/engine/single.feature"))));

        Throwable exception = EngineTestKit.engine(ENGINE_ID) //
                .selectors(selectClasspathResource(resources)) //
                .discover() //
                .getDiscoveryIssues() //
                .get(0) //
                .cause() //
                .orElseThrow();

        assertThat(exception) //
                .isInstanceOf(IllegalArgumentException.class) //
                .hasMessage( //
                    "Found %s resources named %s on the classpath %s.", //
                    resources.size(), //
                    "io/cucumber/junit/platform/engine/single.feature", //
                    resources.stream().map(Resource::getUri).collect(toList()));
    }

    @Test
    void supportsClasspathResourceSelectorWithFilePosition() {
        EngineTestKit.engine(ENGINE_ID)
                .selectors(selectClasspathResource("io/cucumber/junit/platform/engine/rule.feature", //
                    FilePosition.from(5)))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(2, event(scenario("scenario:5", "An example of this rule")));
    }

    @Test
    void supportsMultipleClasspathResourceSelectors() {
        EngineTestKit.engine(ENGINE_ID)
                .selectors(
                    selectClasspathResource("io/cucumber/junit/platform/engine/single.feature"),
                    selectClasspathResource("io/cucumber/junit/platform/engine/scenario-outline.feature"))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(2, event(feature("single.feature", "A feature with a single scenario")))
                .haveExactly(2, event(feature("scenario-outline.feature", "A feature with scenario outlines")));
    }

    @Test
    void supportsClasspathResourceSelectorWithSpaceInResourceName() {
        EngineTestKit.engine(ENGINE_ID)
                .selectors(selectClasspathResource("io/cucumber/junit/platform/engine/with space.feature"))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(1, event(scenario(), finishedSuccessfully()));
    }

    @Test
    void supportsClasspathRootSelector() {
        Path classpathRoot = Paths.get("src/test/resources/");
        EngineTestKit.engine(ENGINE_ID)
                .selectors(selectClasspathRoots(singleton(classpathRoot)).get(0))
                .execute()
                .containerEvents()
                .assertEventsMatchLooselyInOrder(
                    feature("disabled.feature"),
                    feature("empty-scenario.feature"),
                    feature("scenario-outline.feature"),
                    feature("rule.feature"),
                    feature("single.feature"),
                    feature("with%20space.feature"),
                    feature("root.feature"));
    }

    @Test
    void supportsDirectorySelector() {
        EngineTestKit.engine(ENGINE_ID)
                .selectors(selectDirectory("src/test/resources/io/cucumber/junit/platform/engine"))
                .execute()
                .containerEvents()
                .assertEventsMatchLooselyInOrder(
                    feature("disabled.feature"),
                    feature("empty-scenario.feature"),
                    feature("scenario-outline.feature"),
                    feature("rule.feature"),
                    feature("single.feature"),
                    feature("with%20space.feature"));
    }

    @Test
    void supportsFileSelector() {
        EngineTestKit.engine(ENGINE_ID)
                .selectors(selectFile("src/test/resources/io/cucumber/junit/platform/engine/single.feature"))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(1, event( //
                    scenario("scenario:3", "A single scenario"), //
                    finishedSuccessfully()));
    }

    @Test
    void supportsFileSelectorWithFilePosition() {
        EngineTestKit.engine(ENGINE_ID)
                .selectors(selectFile("src/test/resources/io/cucumber/junit/platform/engine/rule.feature", //
                    FilePosition.from(5)))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(1, event( //
                    scenario("scenario:5", "An example of this rule"), //
                    finishedSuccessfully()));
    }

    @Test
    void supportsPackageSelector() {
        EngineTestKit.engine(ENGINE_ID)
                .selectors(selectPackage("io.cucumber.junit.platform.engine"))
                .execute()
                .containerEvents()
                .assertEventsMatchLooselyInOrder(
                    feature("disabled.feature"),
                    feature("empty-scenario.feature"),
                    feature("scenario-outline.feature"),
                    feature("rule.feature"),
                    feature("single.feature"),
                    feature("with%20space.feature"));
    }

    @Test
    void supportsUriSelector() {
        File file = new File("src/test/resources/io/cucumber/junit/platform/engine/single.feature");
        EngineTestKit.engine(ENGINE_ID)
                .selectors(selectUri(file.toURI()))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(1, event( //
                    scenario("scenario:3", "A single scenario"), //
                    finishedSuccessfully()));
    }

    @Test
    void supportsUriSelectorWithFilePosition() {
        File file = new File("src/test/resources/io/cucumber/junit/platform/engine/rule.feature");
        EngineTestKit.engine(ENGINE_ID)
                .selectors(selectUri(file.toURI() + "?line=5"))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(1, event(scenario("scenario:5", "An example of this rule"), finishedSuccessfully()));
    }

    @ParameterizedTest
    @MethodSource({
            "supportsUniqueIdSelectorFromClasspathUri",
            "supportsUniqueIdSelectorFromFileUri",
            "supportsUniqueIdSelectorFromJarFileUri"
    })
    void supportsUniqueIdSelector(UniqueId selected) {
        EngineTestKit.engine(ENGINE_ID)
                .selectors(DiscoverySelectors.selectUniqueId(selected))
                .execute()
                .testEvents()
                .assertThatEvents()
                .haveAtLeastOne(event(prefix(selected), finishedSuccessfully()));
    }

    static Set<UniqueId> supportsUniqueIdSelectorFromClasspathUri() {
        return discoverUniqueIds(selectPackage("io.cucumber.junit.platform.engine"));

    }

    static Set<UniqueId> supportsUniqueIdSelectorFromFileUri() {
        return discoverUniqueIds(selectDirectory("src/test/resources/io/cucumber/junit/platform/engine"));

    }

    static Set<UniqueId> supportsUniqueIdSelectorFromJarFileUri() {
        URI uri = new File("src/test/resources/feature.jar").toURI();
        return discoverUniqueIds(selectUri(uri));
    }

    @Test
    void supportsUniqueIdSelectorWithMultipleSelectors() {
        UniqueId a = EngineTestKit.engine(ENGINE_ID)
                .selectors(selectClasspathResource("io/cucumber/junit/platform/engine/scenario-outline.feature"))
                .execute()
                .allEvents()
                .map(Event::getTestDescriptor)
                .filter(PickleDescriptor.class::isInstance)
                .map(TestDescriptor::getUniqueId)
                .findAny()
                .orElseThrow();

        UniqueId b = EngineTestKit.engine(ENGINE_ID)
                .selectors(selectClasspathResource("io/cucumber/junit/platform/engine/single.feature"))
                .execute()
                .allEvents()
                .map(Event::getTestDescriptor)
                .filter(PickleDescriptor.class::isInstance)
                .map(TestDescriptor::getUniqueId)
                .findAny()
                .orElseThrow();

        EngineTestKit.engine(ENGINE_ID)
                .selectors(selectUniqueId(a), selectUniqueId(b))
                .execute()
                .testEvents()
                .assertThatEvents()
                .haveAtLeastOne(event(prefix(a), finishedSuccessfully()))
                .haveAtLeastOne(event(prefix(b), finishedSuccessfully()));
    }

    @Test
    void supportsUniqueIdSelectorCachesParsedFeaturesAndPickles() {
        DiscoverySelector featureSelector = selectClasspathResource(
            "io/cucumber/junit/platform/engine/scenario-outline.feature");
        DiscoverySelector[] uniqueIdsFromFeature = discoverUniqueIds(featureSelector)
                .stream()
                .map(DiscoverySelectors::selectUniqueId)
                .toArray(DiscoverySelector[]::new);

        EngineDiscoveryResults results = EngineTestKit.engine(ENGINE_ID)
                .selectors(featureSelector)
                .selectors(uniqueIdsFromFeature)
                .discover();

        Set<String> pickleIdsFromFeature = results
                .getEngineDescriptor().getChildren().stream()
                .filter(FeatureDescriptor.class::isInstance)
                .map(FeatureDescriptor.class::cast)
                .map(FeatureDescriptor::getFeature)
                .map(Feature::getPickles)
                .flatMap(Collection::stream)
                .map(Pickle::getId)
                .collect(toSet());

        Set<String> pickleIdsFromPickles = results
                .getEngineDescriptor().getDescendants().stream()
                .filter(PickleDescriptor.class::isInstance)
                .map(PickleDescriptor.class::cast)
                .map(PickleDescriptor::getPickle)
                .map(Pickle::getId)
                .collect(toSet());

        assertEquals(pickleIdsFromFeature, pickleIdsFromPickles);
    }

    @Test
    void supportsFilePositionFeature() {
        EngineTestKit.engine(ENGINE_ID)
                .selectors(
                    selectFile("src/test/resources/io/cucumber/junit/platform/engine/scenario-outline.feature", //
                        FilePosition.from(2)))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(2, event(feature("scenario-outline.feature", "A feature with scenario outlines")));
    }

    @Test
    void supportsFilePositionScenario() {
        EngineTestKit.engine(ENGINE_ID)
                .selectors(
                    selectFile("src/test/resources/io/cucumber/junit/platform/engine/scenario-outline.feature", //
                        FilePosition.from(5)))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(1, event( //
                    scenario("scenario:5", "A scenario"), //
                    finishedSuccessfully()));
    }

    @Test
    void supportsFilePositionScenarioOutline() {
        EngineTestKit.engine(ENGINE_ID)
                .selectors(
                    selectFile("src/test/resources/io/cucumber/junit/platform/engine/scenario-outline.feature", //
                        FilePosition.from(11)))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(1, event( //
                    scenario("scenario:11", "A scenario outline"), //
                    finishedSuccessfully()));
    }

    @Test
    void supportsFilePositionExamples() {
        EngineTestKit.engine(ENGINE_ID)
                .selectors(
                    selectFile("src/test/resources/io/cucumber/junit/platform/engine/scenario-outline.feature", //
                        FilePosition.from(17)))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(1, event( //
                    examples("examples:17", "With some text"), //
                    finishedSuccessfully()));
    }

    @Test
    void supportsFilePositionExample() {
        EngineTestKit.engine(ENGINE_ID)
                .selectors(
                    selectFile("src/test/resources/io/cucumber/junit/platform/engine/scenario-outline.feature", //
                        FilePosition.from(19)))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(1, event( //
                    example("example:19", "Example #1.1"), //
                    finishedSuccessfully()));
    }

    @Test
    void supportsFilePositionRule() {
        EngineTestKit.engine(ENGINE_ID)
                .selectors(selectClasspathResource("io/cucumber/junit/platform/engine/rule.feature", //
                    FilePosition.from(3)))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(2, event(rule("rule:3", "A rule")));
    }

    @Test
    void executesFeaturesInUriOrderByDefault() {
        EngineTestKit.engine(ENGINE_ID)
                .selectors(selectPackage(""))
                .execute()
                .containerEvents()
                .started()
                .assertEventsMatchLooselyInOrder(
                    feature("disabled.feature"),
                    feature("empty-scenario.feature"),
                    feature("scenario-outline.feature"),
                    feature("rule.feature"),
                    feature("single.feature"),
                    feature("with%20space.feature"),
                    feature("root.feature"));
    }

    @Test
    void supportsFeaturesProperty() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(FEATURES_PROPERTY_NAME,
                    "src/test/resources/io/cucumber/junit/platform/engine/single.feature")
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(2, event(engine(source(ClassSource.from(CucumberTestEngine.class)))))
                .haveExactly(1, event(test(finishedSuccessfully())));
    }

    @Test
    void supportsFeaturesPropertyWillIgnoreOtherSelectors() {
        EngineDiscoveryResults discoveryResult = EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(FEATURES_PROPERTY_NAME,
                    "src/test/resources/io/cucumber/junit/platform/engine/single.feature")
                .selectors(selectClasspathResource("io/cucumber/junit/platform/engine/rule.feature"))
                .discover();

        DiscoveryIssue discoveryIssue = discoveryResult.getDiscoveryIssues().get(0);
        assertThat(discoveryIssue.message())
                .startsWith(
                    "Discovering tests using the cucumber.features property. Other discovery selectors are ignored!");
    }

    @Test
    void onlySetsEngineSourceWhenFeaturesPropertyIsUsed() {
        EngineTestKit.engine(ENGINE_ID)
                .selectors(selectFile("src/test/resources/io/cucumber/junit/platform/engine/single.feature"))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(2, event(engine(emptySource())))
                .haveExactly(1, event(test(finishedSuccessfully())));
    }

    @Suite
    @IncludeEngines("cucumber")
    @SelectClasspathResource("io/cucumber/junit/platform/engine/single.feature")
    static class SuiteTestCase {

    }

    @Test
    void supportsDisablingDiscoveryAsRootEngine() {
        DiscoverySelector selector = selectClasspathResource("io/cucumber/junit/platform/engine/single.feature");

        // Ensure classpath resource exists.
        assertThat(EngineTestKit.engine(ENGINE_ID)
                .selectors(selector)
                .discover()
                .getEngineDescriptor()
                .getChildren())
                .isNotEmpty();

        assertThat(EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(JUNIT_PLATFORM_DISCOVERY_AS_ROOT_ENGINE_PROPERTY_NAME, "false")
                .selectors(selector)
                .discover()
                .getEngineDescriptor()
                .getChildren())
                .isEmpty();

        assertThat(EngineTestKit.engine("junit-platform-suite")
                .configurationParameter(JUNIT_PLATFORM_DISCOVERY_AS_ROOT_ENGINE_PROPERTY_NAME, "false")
                .selectors(selectClass(SuiteTestCase.class))
                .discover()
                .getEngineDescriptor()
                .getChildren())
                .isNotEmpty();
    }

    @Test
    void selectAndSkipDisabledScenarioByTags() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(FILTER_TAGS_PROPERTY_NAME, "@Integration and not @Disabled")
                .selectors(selectFile("src/test/resources/io/cucumber/junit/platform/engine/single.feature"))
                .execute()
                .testEvents()
                .assertThatEvents()
                .haveExactly(1, event(test()))
                .haveExactly(1, event(skippedWithReason(
                    "'cucumber.filter.tags=( @Integration and not ( @Disabled ) )' did not match this scenario")));
    }

    @Test
    void selectAndSkipDisabledScenarioByName() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(FILTER_NAME_PROPERTY_NAME, "^Nothing$")
                .selectors(selectFile("src/test/resources/io/cucumber/junit/platform/engine/single.feature"))
                .execute()
                .testEvents()
                .assertThatEvents()
                .haveExactly(1, event(test(),
                    event(skippedWithReason("'cucumber.filter.name=^Nothing$' did not match this scenario"))));
    }

    @Test
    void cucumberTagsAreConvertedToJunitTags() {
        EngineTestKit.engine(ENGINE_ID)
                .selectors(selectClasspathResource("io/cucumber/junit/platform/engine/scenario-outline.feature"))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveAtLeastOne(event(feature(), tags(emptySet())))
                .haveAtLeastOne(
                    event(scenario("scenario:5"), tags("FeatureTag", "ScenarioTag")))
                .haveAtLeastOne(event(scenario("scenario:11"), tags(emptySet())))
                .haveAtLeastOne(event(examples("examples:17"), tags(emptySet())))
                .haveAtLeastOne(event(example("example:19"), tags("FeatureTag", "ScenarioOutlineTag", "Example1Tag")));
    }

    @Test
    void providesClasspathSourceWhenClasspathResourceIsSelected() {
        String feature = "io/cucumber/junit/platform/engine/scenario-outline.feature";
        EngineTestKit.engine(ENGINE_ID)
                .selectors(selectClasspathResource(feature))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveAtLeastOne(event(feature(), source(ClasspathResourceSource.from(feature, from(2, 1)))))
                .haveAtLeastOne(
                    event(scenario("scenario:5"), source(ClasspathResourceSource.from(feature, from(5, 3)))))
                .haveAtLeastOne(
                    event(scenario("scenario:11"), source(ClasspathResourceSource.from(feature, from(11, 3)))))
                .haveAtLeastOne(
                    event(examples("examples:17"), source(ClasspathResourceSource.from(feature, from(17, 5)))))
                .haveAtLeastOne(
                    event(example("example:19"), source(ClasspathResourceSource.from(feature, from(19, 7)))));
    }

    @Test
    void providesFileSourceWhenFileIsSelected() {
        File feature = new File("src/test/resources/io/cucumber/junit/platform/engine/scenario-outline.feature");
        EngineTestKit.engine(ENGINE_ID)
                .selectors(selectFile(feature))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveAtLeastOne(event(feature(), source(FileSource.from(feature, from(2, 1)))))
                .haveAtLeastOne(event(scenario("scenario:5"), source(FileSource.from(feature, from(5, 3)))))
                .haveAtLeastOne(event(scenario("scenario:11"), source(FileSource.from(feature, from(11, 3)))))
                .haveAtLeastOne(event(examples("examples:17"), source(FileSource.from(feature, from(17, 5)))))
                .haveAtLeastOne(event(example("example:19"), source(FileSource.from(feature, from(19, 7)))));
    }

    @Test
    void supportsPackageFilterForClasspathResources() {
        Path classpathRoot = Paths.get("src/test/resources/");
        EngineTestKit.engine(ENGINE_ID)
                .selectors(selectClasspathRoots(singleton(classpathRoot)).get(0))
                .filters(includePackageNames("io.cucumber.junit.platform"))
                .execute()
                .containerEvents()
                .assertEventsMatchLooselyInOrder(
                    feature("disabled.feature"),
                    feature("empty-scenario.feature"),
                    feature("scenario-outline.feature"),
                    feature("rule.feature"),
                    feature("single.feature"),
                    feature("with%20space.feature"));
    }

    @Test
    void defaultsToShortWithNumberAndPickleIfParameterizedNamingStrategy() {
        EngineTestKit.engine(ENGINE_ID)
                .selectors(
                    selectClasspathResource("io/cucumber/junit/platform/engine/parameterized-scenario-outline.feature"))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveAtLeastOne(event(feature(), displayName("A feature with a parameterized scenario outline")))
                .haveAtLeastOne(event(scenario(), displayName("A scenario full of <vegetable>s")))
                .haveAtLeastOne(event(examples(), displayName("Of the Gherkin variety")))
                .haveAtLeastOne(event(example(), displayName("Example #1.1: A scenario full of Cucumbers")));
    }

    @Test
    void supportsLongWithNumberNamingStrategy() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME, "long")
                .configurationParameter(JUNIT_PLATFORM_LONG_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME, "number")
                .selectors(
                    selectClasspathResource("io/cucumber/junit/platform/engine/parameterized-scenario-outline.feature"))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveAtLeastOne(event(feature(), displayName("A feature with a parameterized scenario outline")))
                .haveAtLeastOne(event(scenario(),
                    displayName("A feature with a parameterized scenario outline - A scenario full of <vegetable>s")))
                .haveAtLeastOne(event(examples(), displayName(
                    "A feature with a parameterized scenario outline - A scenario full of <vegetable>s - Of the Gherkin variety")))
                .haveAtLeastOne(event(example(), displayName(
                    "A feature with a parameterized scenario outline - A scenario full of <vegetable>s - Of the Gherkin variety - Example #1.1")));
    }

    @Test
    void supportsLongWithPickleNamingStrategy() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME, "long")
                .configurationParameter(JUNIT_PLATFORM_LONG_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME, "pickle")
                .selectors(
                    selectClasspathResource("io/cucumber/junit/platform/engine/parameterized-scenario-outline.feature"))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveAtLeastOne(event(feature(), displayName("A feature with a parameterized scenario outline")))
                .haveAtLeastOne(event(scenario(),
                    displayName("A feature with a parameterized scenario outline - A scenario full of <vegetable>s")))
                .haveAtLeastOne(event(examples(), displayName(
                    "A feature with a parameterized scenario outline - A scenario full of <vegetable>s - Of the Gherkin variety")))
                .haveAtLeastOne(event(example(), displayName(
                    "A feature with a parameterized scenario outline - A scenario full of <vegetable>s - Of the Gherkin variety - A scenario full of Cucumbers")));
    }

    @Test
    void supportsLongWithNumberAndPickleIfParameterizedNamingStrategy() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME, "long")
                .configurationParameter(JUNIT_PLATFORM_SHORT_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME,
                    "number-and-pickle-if-parameterized")
                .selectors(
                    selectClasspathResource("io/cucumber/junit/platform/engine/parameterized-scenario-outline.feature"))
                .execute()
                .allEvents()

                .assertThatEvents()
                .haveAtLeastOne(event(feature(), displayName("A feature with a parameterized scenario outline")))
                .haveAtLeastOne(event(scenario(),
                    displayName("A feature with a parameterized scenario outline - A scenario full of <vegetable>s")))
                .haveAtLeastOne(event(examples(), displayName(
                    "A feature with a parameterized scenario outline - A scenario full of <vegetable>s - Of the Gherkin variety")))
                .haveAtLeastOne(event(example(), displayName(
                    "A feature with a parameterized scenario outline - A scenario full of <vegetable>s - Of the Gherkin variety - Example #1.1: A scenario full of Cucumbers")));
    }

    @Test
    void supportsShortWithPickleNamingStrategy() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME, "short")
                .configurationParameter(JUNIT_PLATFORM_SHORT_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME, "pickle")
                .selectors(
                    selectClasspathResource("io/cucumber/junit/platform/engine/parameterized-scenario-outline.feature"))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveAtLeastOne(event(feature(), displayName("A feature with a parameterized scenario outline")))
                .haveAtLeastOne(event(scenario(), displayName("A scenario full of <vegetable>s")))
                .haveAtLeastOne(event(examples(), displayName("Of the Gherkin variety")))
                .haveAtLeastOne(event(example(), displayName("A scenario full of Cucumbers")));
    }

    @Test
    void supportsShortWithNumberNamingStrategy() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME, "short")
                .configurationParameter(JUNIT_PLATFORM_SHORT_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME, "number")
                .selectors(
                    selectClasspathResource("io/cucumber/junit/platform/engine/parameterized-scenario-outline.feature"))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveAtLeastOne(event(feature(), displayName("A feature with a parameterized scenario outline")))
                .haveAtLeastOne(event(scenario(), displayName("A scenario full of <vegetable>s")))
                .haveAtLeastOne(event(examples(), displayName("Of the Gherkin variety")))
                .haveAtLeastOne(event(example(), displayName("Example #1.1")));
    }

    @Test
    void supportsShortWithNumberAndPickleIfParameterizedNamingStrategy() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME, "short")
                .configurationParameter(JUNIT_PLATFORM_SHORT_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME,
                    "number-and-pickle-if-parameterized")
                .selectors(
                    selectClasspathResource("io/cucumber/junit/platform/engine/parameterized-scenario-outline.feature"))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveAtLeastOne(event(feature(), displayName("A feature with a parameterized scenario outline")))
                .haveAtLeastOne(event(scenario(), displayName("A scenario full of <vegetable>s")))
                .haveAtLeastOne(event(examples(), displayName("Of the Gherkin variety")))
                .haveAtLeastOne(event(example(), displayName("Example #1.1: A scenario full of Cucumbers")));
    }

    @Test
    void defaultsToLexicalOrder() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME, "long")
                .selectors(
                    selectClasspathResource("io/cucumber/junit/platform/engine/single.feature"),
                    selectClasspathResource("io/cucumber/junit/platform/engine/ordering.feature"))
                .execute()
                .allEvents()
                .started()
                .assertThatEvents()
                .extracting(Event::getTestDescriptor)
                .extracting(TestDescriptor::getDisplayName)
                .containsExactly("Cucumber",
                    "1. A feature to order scenarios",
                    "1. A feature to order scenarios - 1.1",
                    "1. A feature to order scenarios - 1.2",
                    "1. A feature to order scenarios - 1.2 - 1.2.1",
                    "1. A feature to order scenarios - 1.2 - 1.2.1 - Example #1.1",
                    "1. A feature to order scenarios - 1.2 - 1.2.1 - Example #1.2",
                    "1. A feature to order scenarios - 1.2 - 1.2.2",
                    "1. A feature to order scenarios - 1.2 - 1.2.2 - Example #2.1",
                    "1. A feature to order scenarios - 1.2 - 1.2.2 - Example #2.2",
                    "1. A feature to order scenarios - 1.3 A rule",
                    "1. A feature to order scenarios - 1.3 A rule - 1.3.1",
                    "1. A feature to order scenarios - 1.3 A rule - 1.3.2",
                    "1. A feature to order scenarios - 1.4",
                    "1. A feature to order scenarios - 1.4 - 1.4.1",
                    "1. A feature to order scenarios - 1.4 - 1.4.2",
                    "A feature with a single scenario",
                    "A feature with a single scenario - A single scenario");
    }

    @Test
    void supportsReverseOrder() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(EXECUTION_ORDER_PROPERTY_NAME, "reverse")
                .configurationParameter(JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME, "long")
                .selectors(
                    selectClasspathResource("io/cucumber/junit/platform/engine/single.feature"),
                    selectClasspathResource("io/cucumber/junit/platform/engine/ordering.feature"))
                .execute()
                .allEvents()
                .started()
                .assertThatEvents()
                .extracting(Event::getTestDescriptor)
                .extracting(TestDescriptor::getDisplayName)
                .containsExactly("Cucumber",
                    "A feature with a single scenario",
                    "A feature with a single scenario - A single scenario",
                    "1. A feature to order scenarios",
                    "1. A feature to order scenarios - 1.4",
                    "1. A feature to order scenarios - 1.4 - 1.4.2",
                    "1. A feature to order scenarios - 1.4 - 1.4.1",
                    "1. A feature to order scenarios - 1.3 A rule",
                    "1. A feature to order scenarios - 1.3 A rule - 1.3.2",
                    "1. A feature to order scenarios - 1.3 A rule - 1.3.1",
                    "1. A feature to order scenarios - 1.2",
                    "1. A feature to order scenarios - 1.2 - 1.2.2",
                    "1. A feature to order scenarios - 1.2 - 1.2.2 - Example #2.2",
                    "1. A feature to order scenarios - 1.2 - 1.2.2 - Example #2.1",
                    "1. A feature to order scenarios - 1.2 - 1.2.1",
                    "1. A feature to order scenarios - 1.2 - 1.2.1 - Example #1.2",
                    "1. A feature to order scenarios - 1.2 - 1.2.1 - Example #1.1",
                    "1. A feature to order scenarios - 1.1");
    }

    @Test
    void supportsRandomOrder(LogRecordListener logRecordListener) {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(EXECUTION_ORDER_PROPERTY_NAME, "random")
                .discover();

        LogRecord message = logRecordListener.getLogRecords()
                .stream()
                .filter(logRecord -> logRecord.getLoggerName()
                        .equals(DefaultDescriptorOrderingStrategy.class.getCanonicalName()))
                .findFirst()
                .orElseThrow();

        assertAll(
            () -> assertThat(message.getLevel()).isEqualTo(Level.CONFIG),
            () -> assertThat(message.getMessage())
                    .matches(
                        "Using generated seed for configuration parameter 'cucumber\\.execution\\.order\\.random\\.seed' with value '\\d+'."));
    }

    @Test
    void supportsRandomOrderWithSeed() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(EXECUTION_ORDER_PROPERTY_NAME, "random")
                .configurationParameter(EXECUTION_ORDER_RANDOM_SEED_PROPERTY_NAME, "1234")
                .configurationParameter(JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME, "long")
                .selectors(
                    selectClasspathResource("io/cucumber/junit/platform/engine/single.feature"),
                    selectClasspathResource("io/cucumber/junit/platform/engine/ordering.feature"))
                .execute()
                .allEvents()
                .started()
                .assertThatEvents()
                .extracting(Event::getTestDescriptor)
                .extracting(TestDescriptor::getDisplayName)
                .containsExactly("Cucumber",
                    "1. A feature to order scenarios",
                    "1. A feature to order scenarios - 1.4",
                    "1. A feature to order scenarios - 1.4 - 1.4.1",
                    "1. A feature to order scenarios - 1.4 - 1.4.2",
                    "1. A feature to order scenarios - 1.1",
                    "1. A feature to order scenarios - 1.3 A rule",
                    "1. A feature to order scenarios - 1.3 A rule - 1.3.2",
                    "1. A feature to order scenarios - 1.3 A rule - 1.3.1",
                    "1. A feature to order scenarios - 1.2",
                    "1. A feature to order scenarios - 1.2 - 1.2.2",
                    "1. A feature to order scenarios - 1.2 - 1.2.2 - Example #2.1",
                    "1. A feature to order scenarios - 1.2 - 1.2.2 - Example #2.2",
                    "1. A feature to order scenarios - 1.2 - 1.2.1",
                    "1. A feature to order scenarios - 1.2 - 1.2.1 - Example #1.2",
                    "1. A feature to order scenarios - 1.2 - 1.2.1 - Example #1.1",
                    "A feature with a single scenario",
                    "A feature with a single scenario - A single scenario");
    }

    @Test
    void reportsParsErrorsAsDiscoveryIssues() {
        EngineDiscoveryResults results = EngineTestKit.engine(ENGINE_ID)
                .selectors(
                    selectFile("src/test/bad-features/parse-error.feature"))
                .discover();

        DiscoveryIssue issue = results.getDiscoveryIssues().get(0);

        assertAll(() -> {
            assertThat(issue.message()).startsWith("Failed to parse resource at: ");
            assertThat(issue.source())
                    .contains(FileSource.from(new File("src/test/bad-features/parse-error.feature")));
        });
    }

    @Test
    void supportsExclusiveResources() {
        PickleDescriptor pickleDescriptor = EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(EXECUTION_EXCLUSIVE_RESOURCES_PREFIX + "ResourceA" + READ_WRITE_SUFFIX,
                    "resource-a")
                .configurationParameter(EXECUTION_EXCLUSIVE_RESOURCES_PREFIX + "ResourceAReadOnly" + READ_SUFFIX,
                    "resource-a")
                .selectors(
                    selectClasspathResource("io/cucumber/junit/platform/engine/resource.feature"))
                .discover()
                .getEngineDescriptor()
                .getDescendants()
                .stream()
                .filter(PickleDescriptor.class::isInstance)
                .map(PickleDescriptor.class::cast)
                .findAny()
                .orElseThrow();

        assertThat(pickleDescriptor.getExclusiveResources())
                .containsExactlyInAnyOrder(
                    new ExclusiveResource("resource-a", ExclusiveResource.LockMode.READ_WRITE),
                    new ExclusiveResource("resource-a", ExclusiveResource.LockMode.READ));

    }

    @Test
    void supportsConcurrentExecutionOfFeatureElements() {
        Set<Node<?>> testDescriptors = EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(EXECUTION_MODE_FEATURE_PROPERTY_NAME, "concurrent")
                .selectors(
                    selectClasspathResource("io/cucumber/junit/platform/engine/single.feature"))
                .discover()
                .getEngineDescriptor()
                .getDescendants()
                .stream()
                .filter(Node.class::isInstance)
                .map(testDescriptor -> (Node<?>) testDescriptor)
                .collect(toSet());

        assertThat(testDescriptors)
                .isNotEmpty()
                .extracting(Node::getExecutionMode)
                .containsOnly(CONCURRENT);
    }

    @Test
    void supportsSameThreadExecutionOfFeatureElements() {
        Set<? extends TestDescriptor> testDescriptors = EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(EXECUTION_MODE_FEATURE_PROPERTY_NAME, "same_thread")
                .selectors(
                    selectClasspathResource("io/cucumber/junit/platform/engine/single.feature"))
                .discover()
                .getEngineDescriptor()
                .getDescendants();

        Set<? extends TestDescriptor> featureDescriptors = testDescriptors
                .stream()
                .filter(FeatureDescriptor.class::isInstance)
                .collect(toSet());

        assertThat(featureDescriptors)
                .isNotEmpty()
                .map(Node.class::cast)
                .extracting(Node::getExecutionMode)
                .containsOnly(CONCURRENT);

        Set<? extends TestDescriptor> pickleDescriptors = testDescriptors
                .stream()
                .filter(testDescriptor -> !featureDescriptors.contains(testDescriptor))
                .collect(toSet());

        assertThat(pickleDescriptors)
                .isNotEmpty()
                .map(Node.class::cast)
                .extracting(Node::getExecutionMode)
                .containsOnly(SAME_THREAD);
    }
}
