package io.cucumber.core.gherkin.vintage;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.plugin.event.Node;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

import static java.nio.file.Files.readAllBytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureParserTest {

    private final GherkinVintageFeatureParser parser = new GherkinVintageFeatureParser();

    @Test
    void empty_feature_file_is_parsed_but_produces_no_feature() throws IOException {
        URI uri = URI.create("classpath:com/example.feature");
        String source = new String(readAllBytes(Paths.get("src/test/resources/io/cucumber/core/gherkin/vintage/empty.feature")));
        Optional<Feature> feature = parser.parse(uri, source, UUID::randomUUID);
        assertFalse(feature.isPresent());
    }

    @Test
    void feature_file_without_pickles_is_parsed_but_produces_no_feature() throws IOException {
        URI uri = URI.create("classpath:com/example.feature");
        String source = new String(readAllBytes(Paths.get("src/test/resources/io/cucumber/core/gherkin/vintage/empty.feature")));
        Optional<Feature> feature = parser.parse(uri, source, UUID::randomUUID);
        assertFalse(feature.isPresent());
    }

    @Test
    void unnamed_elements_return_empty_strings_as_name() throws IOException {
        URI uri = URI.create("classpath:com/example.feature");
        String source = new String(readAllBytes(Paths.get("src/test/resources/io/cucumber/core/gherkin/vintage/unnamed.feature")));
        Feature feature = parser.parse(uri, source, UUID::randomUUID).get();
        assertEquals("", feature.getName());
        Iterator<Node> featureElements = feature.elements().iterator();
        Node.Scenario scenario = (Node.Scenario) featureElements.next();
        assertEquals("", scenario.getName());
        assertEquals("Scenario", scenario.getKeyword());
        Node.ScenarioOutline scenarioOutline = (Node.ScenarioOutline) featureElements.next();
        assertEquals("", scenarioOutline.getName());
        assertEquals("Scenario Outline", scenarioOutline.getKeyword());
        Node.Examples examples = scenarioOutline.elements().iterator().next();
        assertEquals("", examples.getName());
        assertEquals("Examples", examples.getKeyword());
        Node.Example example = examples.elements().iterator().next();

        // Example is the exception.
        assertEquals("Example #1", example.getName());
        assertNull(example.getKeyword());
    }
}
