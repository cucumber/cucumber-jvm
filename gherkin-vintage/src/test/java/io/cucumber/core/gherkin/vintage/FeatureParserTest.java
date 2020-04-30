package io.cucumber.core.gherkin.vintage;

import io.cucumber.core.gherkin.DataTableArgument;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.gherkin.Step;
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

class FeatureParserTest {

    private final GherkinVintageFeatureParser parser = new GherkinVintageFeatureParser();
    @Test
    void can_parse_single_scenario() throws IOException {
        URI uri = URI.create("classpath:com/example.feature");
        String source = new String(readAllBytes(Paths.get("src/test/resources/io/cucumber/core/gherkin/vintage/single.feature")));
        Optional<Feature> feature = parser.parse(uri, source, UUID::randomUUID);
        assertEquals(1, feature.get().getPickles().size());
    }

    @Test
    void background_elements_are_not_scenarios() throws IOException {
        URI uri = URI.create("classpath:com/example.feature");
        String source = new String(readAllBytes(Paths.get("src/test/resources/io/cucumber/core/gherkin/vintage/background.feature")));
        Optional<Feature> feature = parser.parse(uri, source, UUID::randomUUID);
        assertEquals(1, feature.get().getPickles().size());
    }

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
    void empty_table_is_parsed() throws IOException {
        URI uri = URI.create("classpath:com/example.feature");
        String source = new String(readAllBytes(Paths.get("src/test/resources/io/cucumber/core/gherkin/vintage/empty-table.feature")));
        Feature feature = parser.parse(uri, source, UUID::randomUUID).get();
        Pickle pickle = feature.getPickles().get(0);
        Step step = pickle.getSteps().get(0);
        DataTableArgument argument = (DataTableArgument) step.getArgument();
        assertEquals(5, argument.getLine());
    }

    @Test
    void unnamed_elements_return_empty_strings_as_name() throws IOException {
        URI uri = URI.create("classpath:com/example.feature");
        String source = new String(readAllBytes(Paths.get("src/test/resources/io/cucumber/core/gherkin/vintage/unnamed.feature")));
        Feature feature = parser.parse(uri, source, UUID::randomUUID).get();
        assertEquals(Optional.empty(), feature.getName());
        Iterator<Node> featureElements = feature.elements().iterator();
        Node.Scenario scenario = (Node.Scenario) featureElements.next();
        assertEquals(Optional.empty(), scenario.getName());
        assertEquals(Optional.of("Scenario"), scenario.getKeyWord());
        Node.ScenarioOutline scenarioOutline = (Node.ScenarioOutline) featureElements.next();
        assertEquals(Optional.empty(), scenarioOutline.getName());
        assertEquals(Optional.of("Scenario Outline"), scenarioOutline.getKeyWord());
        Node.Examples examples = scenarioOutline.elements().iterator().next();
        assertEquals(Optional.empty(), examples.getName());
        assertEquals(Optional.of("Examples"), examples.getKeyWord());
        Node.Example example = examples.elements().iterator().next();

        // Example is the exception.
        assertEquals(Optional.of("Example #1"), example.getName());
        assertEquals(Optional.empty(), example.getKeyWord());
    }
}
