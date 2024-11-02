package io.cucumber.junit.platform.engine;

import io.cucumber.core.logging.LogRecordListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.support.Resource;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.FilePosition;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

import static io.cucumber.junit.platform.engine.Constants.FEATURES_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.FILTER_NAME_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PUBLISH_QUIET_PROPERTY_NAME;
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
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.platform.engine.UniqueId.forEngine;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathResource;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathRoots;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectDirectory;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectFile;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUri;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.skippedWithReason;
import static org.junit.platform.testkit.engine.EventConditions.test;

@WithLogRecordListener
class CucumberTestEngineTest {

    private final CucumberTestEngine engine = new CucumberTestEngine();

    @Test
    void id() {
        assertEquals(ENGINE_ID, engine.getId());
    }

    @Test
    void version() {
        assertEquals(Optional.of("DEVELOPMENT"), engine.getVersion());
    }

    @Test
    void createExecutionContext() {
        EngineExecutionListener listener = new EmptyEngineExecutionListener();
        ConfigurationParameters configuration = new EmptyConfigurationParameters();
        EngineDiscoveryRequest discoveryRequest = new EmptyEngineDiscoveryRequest(configuration);
        UniqueId id = forEngine(engine.getId());
        TestDescriptor testDescriptor = engine.discover(discoveryRequest, id);
        ExecutionRequest execution = new ExecutionRequest(testDescriptor, listener, configuration);
        assertNotNull(engine.createExecutionContext(execution));
    }

    @Test
    void empty() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(0, event(test()));
    }

    @Test
    void notCucumber() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .selectors(selectUniqueId(forEngine("not-cucumber")))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(0, event(test()));
    }

    @Test
    void supportsClassSelector() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .selectors(selectClass(RunCucumberTest.class))
                .execute()
                .containerEvents()
                .assertEventsMatchLooselyInOrder(
                    feature("disabled.feature"),
                    feature("empty-scenario.feature"),
                    feature("feature-with-outline.feature"),
                    feature("rule.feature"),
                    feature("single.feature"),
                    feature("with%20space.feature"));
    }

    @Test
    void supportsClasspathResourceSelector() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .selectors(selectClasspathResource("io/cucumber/junit/platform/engine/single.feature"))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(1, event( //
                    scenario("scenario:3", "A single scenario"), //
                    finishedSuccessfully()));
    }

    @Test
    void supportsClasspathResourceSelectorThrowIfDuplicateResources() {
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

        JUnitException exception = assertThrows(JUnitException.class, () -> EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .selectors(selectClasspathResource(resources))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(1, event( //
                    scenario("scenario:3", "A single scenario"), //
                    finishedSuccessfully())));

        assertThat(exception) //
                .hasRootCauseInstanceOf(IllegalArgumentException.class) //
                .hasRootCauseMessage( //
                    "Found %s resources named %s classpath %s. Using the first.", //
                    resources.size(), //
                    "io/cucumber/junit/platform/engine/single.feature", //
                    resources.stream().map(Resource::getUri).collect(toList()));
    }

    @Test
    void supportsClasspathResourceSelectorWithFilePosition() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
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
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .selectors(
                    selectClasspathResource("io/cucumber/junit/platform/engine/single.feature"),
                    selectClasspathResource("io/cucumber/junit/platform/engine/feature-with-outline.feature"))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(2, event(feature("single.feature", "A feature with a single scenario")))
                .haveExactly(2, event(feature("feature-with-outline.feature", "A feature with scenario outlines")));
    }

    @Test
    void supportsClasspathResourceSelectorWithSpaceInResourceName() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
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
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .selectors(selectClasspathRoots(singleton(classpathRoot)).get(0))
                .execute()
                .containerEvents()
                .assertEventsMatchLooselyInOrder(
                    feature("disabled.feature"),
                    feature("empty-scenario.feature"),
                    feature("feature-with-outline.feature"),
                    feature("rule.feature"),
                    feature("single.feature"),
                    feature("with%20space.feature"),
                    feature("root.feature"));
    }

    @Test
    void supportsDirectorySelector() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .selectors(selectDirectory("src/test/resources/io/cucumber/junit/platform/engine"))
                .execute()
                .containerEvents()
                .assertEventsMatchLooselyInOrder(
                    feature("disabled.feature"),
                    feature("empty-scenario.feature"),
                    feature("feature-with-outline.feature"),
                    feature("rule.feature"),
                    feature("single.feature"),
                    feature("with%20space.feature"));
    }

    @Test
    void supportsFileSelector() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
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
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
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
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .selectors(selectPackage("io.cucumber.junit.platform.engine"))
                .execute()
                .containerEvents()
                .assertEventsMatchLooselyInOrder(
                    feature("disabled.feature"),
                    feature("empty-scenario.feature"),
                    feature("feature-with-outline.feature"),
                    feature("rule.feature"),
                    feature("single.feature"),
                    feature("with%20space.feature"));
    }

    @Test
    void supportsUriSelector() {
        File file = new File("src/test/resources/io/cucumber/junit/platform/engine/single.feature");
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
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
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
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
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .selectors(DiscoverySelectors.selectUniqueId(selected))
                .execute()
                .testEvents()
                .assertThatEvents()
                .haveAtLeastOne(event(prefix(selected), finishedSuccessfully()));
    }

    static Set<UniqueId> supportsUniqueIdSelectorFromClasspathUri() {
        return EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .selectors(selectPackage("io.cucumber.junit.platform.engine"))
                .execute()
                .allEvents()
                .map(Event::getTestDescriptor)
                .filter(Predicate.not(TestDescriptor::isRoot))
                .map(TestDescriptor::getUniqueId)
                .collect(Collectors.toSet());
    }

    static Set<UniqueId> supportsUniqueIdSelectorFromFileUri() {
        return EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .selectors(selectDirectory("src/test/resources/io/cucumber/junit/platform/engine"))
                .execute()
                .allEvents()
                .map(Event::getTestDescriptor)
                .filter(Predicate.not(TestDescriptor::isRoot))
                .map(TestDescriptor::getUniqueId)
                .collect(Collectors.toSet());
    }

    static Set<UniqueId> supportsUniqueIdSelectorFromJarFileUri() {
        URI uri = new File("src/test/resources/feature.jar").toURI();
        return EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .selectors(selectUri(uri))
                .execute()
                .allEvents()
                .map(Event::getTestDescriptor)
                .filter(Predicate.not(TestDescriptor::isRoot))
                .map(TestDescriptor::getUniqueId)
                .collect(Collectors.toSet());
    }

    @Test
    void supportsFilePositionFeature() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .selectors(
                    selectFile("src/test/resources/io/cucumber/junit/platform/engine/feature-with-outline.feature", //
                        FilePosition.from(2)))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(2, event(feature("feature-with-outline.feature", "A feature with scenario outlines")));
    }

    @Test
    void supportsFilePositionScenario() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .selectors(
                    selectFile("src/test/resources/io/cucumber/junit/platform/engine/feature-with-outline.feature", //
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
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .selectors(
                    selectFile("src/test/resources/io/cucumber/junit/platform/engine/feature-with-outline.feature", //
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
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .selectors(
                    selectFile("src/test/resources/io/cucumber/junit/platform/engine/feature-with-outline.feature", //
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
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .selectors(
                    selectFile("src/test/resources/io/cucumber/junit/platform/engine/feature-with-outline.feature", //
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
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
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
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .selectors(selectPackage(""))
                .execute()
                .containerEvents()
                .started()
                .assertEventsMatchLooselyInOrder(
                    feature("disabled.feature"),
                    feature("empty-scenario.feature"),
                    feature("feature-with-outline.feature"),
                    feature("rule.feature"),
                    feature("single.feature"),
                    feature("with%20space.feature"),
                    feature("root.feature"));
    }

    @Test
    void supportsFeaturesProperty() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .configurationParameter(FEATURES_PROPERTY_NAME,
                    "src/test/resources/io/cucumber/junit/platform/engine/single.feature")
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(2, event(engine(source(ClassSource.from(CucumberTestEngine.class)))))
                .haveExactly(1, event(test(finishedSuccessfully())));
    }

    @Test
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    void supportsFeaturesPropertyWillIgnoreOtherSelectors(LogRecordListener logRecordListener) {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .configurationParameter(FEATURES_PROPERTY_NAME,
                    "src/test/resources/io/cucumber/junit/platform/engine/single.feature")
                .selectors(selectClasspathResource("io/cucumber/junit/platform/engine/rule.feature"))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(2, event(engine(source(ClassSource.from(CucumberTestEngine.class)))))
                .haveExactly(1, event(test(finishedSuccessfully())));

        LogRecord warning = logRecordListener.getLogRecords()
                .stream()
                .filter(logRecord -> FeaturesPropertyResolver.class.getName().equals(logRecord.getLoggerName()))
                .filter(logRecord -> Level.WARNING.equals(logRecord.getLevel()))
                .findFirst().get();

        assertThat(warning.getMessage())
                .startsWith(
                    "Discovering tests using the cucumber.features property. Other discovery selectors are ignored!");
    }

    @Test
    void onlySetsEngineSourceWhenFeaturesPropertyIsUsed() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .selectors(selectFile("src/test/resources/io/cucumber/junit/platform/engine/single.feature"))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(2, event(engine(emptySource())))
                .haveExactly(1, event(test(finishedSuccessfully())));
    }

    @Test
    void selectAndSkipDisabledScenarioByTags() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
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
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .configurationParameter(FILTER_NAME_PROPERTY_NAME, "^Nothing$")
                .selectors(selectFile("src/test/resources/io/cucumber/junit/platform/engine/single.feature"))
                .execute()
                .testEvents()
                .assertThatEvents()
                .haveExactly(1, event(test()))
                .haveExactly(1,
                    event(skippedWithReason("'cucumber.filter.name=^Nothing$' did not match this scenario")));
    }
}
