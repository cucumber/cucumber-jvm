package io.cucumber.core.gherkin.messages;

import io.cucumber.core.gherkin.DataTableArgument;
import io.cucumber.core.gherkin.DocStringArgument;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.FeatureParserException;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.gherkin.Step;
import io.cucumber.plugin.event.Node;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.nio.file.Files.readAllBytes;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureParserTest {

    private final GherkinMessagesFeatureParser parser = new GherkinMessagesFeatureParser();

    @Test
    void feature_file_without_pickles_is_parsed_produces_empty_feature() throws IOException {
        URI uri = URI.create("classpath:com/example.feature");
        String source = new String(
            readAllBytes(Paths.get("src/test/resources/io/cucumber/core/gherkin/messages/no-pickles.feature")));
        Optional<Feature> feature = parser.parse(uri, source, UUID::randomUUID);
        assertTrue(feature.isPresent());
        assertEquals(0, feature.get().getPickles().size());
    }

    @Test
    void empty_feature_file_is_parsed_but_produces_no_feature() throws IOException {
        URI uri = URI.create("classpath:com/example.feature");
        String source = new String(
            readAllBytes(Paths.get("src/test/resources/io/cucumber/core/gherkin/messages/empty.feature")));
        Optional<Feature> feature = parser.parse(uri, source, UUID::randomUUID);
        assertFalse(feature.isPresent());
    }

    @Test
    void unnamed_elements_return_empty_strings_as_name() throws IOException {
        URI uri = URI.create("classpath:com/example.feature");
        String source = new String(
            readAllBytes(Paths.get("src/test/resources/io/cucumber/core/gherkin/messages/unnamed.feature")));
        Feature feature = parser.parse(uri, source, UUID::randomUUID).get();
        assertEquals(Optional.empty(), feature.getName());
        Node.Rule rule = (Node.Rule) feature.elements().iterator().next();
        assertEquals(Optional.empty(), rule.getName());
        assertEquals(Optional.of("Rule"), rule.getKeyword());
        Iterator<Node> ruleElements = rule.elements().iterator();
        Node.Scenario scenario = (Node.Scenario) ruleElements.next();
        assertEquals(Optional.empty(), scenario.getName());
        assertEquals(Optional.of("Scenario"), scenario.getKeyword());
        Node.ScenarioOutline scenarioOutline = (Node.ScenarioOutline) ruleElements.next();
        assertEquals(Optional.empty(), scenarioOutline.getName());
        assertEquals(Optional.of("Scenario Outline"), scenarioOutline.getKeyword());
        Node.Examples examples = scenarioOutline.elements().iterator().next();
        assertEquals(Optional.empty(), examples.getName());
        assertEquals(Optional.of("Examples"), examples.getKeyword());
        Node.Example example = examples.elements().iterator().next();

        // Example is the exception.
        assertEquals(Optional.of("Example #1"), example.getName());
        assertEquals(Optional.empty(), example.getKeyword());
    }

    @Test
    void empty_table_is_parsed() throws IOException {
        URI uri = URI.create("classpath:com/example.feature");
        String source = new String(
            readAllBytes(Paths.get("src/test/resources/io/cucumber/core/gherkin/messages/empty-table.feature")));
        Feature feature = parser.parse(uri, source, UUID::randomUUID).get();
        Pickle pickle = feature.getPickles().get(0);
        Step step = pickle.getSteps().get(0);
        DataTableArgument argument = (DataTableArgument) step.getArgument();
        assertEquals(5, argument.getLine());
    }

    @Test
    void empty_doc_string_media_type_is_null() throws IOException {
        URI uri = URI.create("classpath:com/example.feature");
        String source = new String(
            readAllBytes(Paths.get("src/test/resources/io/cucumber/core/gherkin/messages/doc-string.feature")));
        Feature feature = parser.parse(uri, source, UUID::randomUUID).get();
        Pickle pickle = feature.getPickles().get(0);
        List<Step> steps = pickle.getSteps();

        assertAll(() -> {
            assertNull(((DocStringArgument) steps.get(0).getArgument()).getContentType());
            assertEquals("text/plain", ((DocStringArgument) steps.get(1).getArgument()).getContentType());
        });
    }

    @Test
    void backgrounds_can_occur_twice() throws IOException {
        URI uri = URI.create("classpath:com/example.feature");
        String source = new String(
            readAllBytes(Paths.get("src/test/resources/io/cucumber/core/gherkin/messages/background.feature")));
        Feature feature = parser.parse(uri, source, UUID::randomUUID).get();
        Pickle pickle = feature.getPickles().get(0);
        List<Step> steps = pickle.getSteps();
        assertEquals(3, steps.size());
    }

    @Test
    void lexer_error_throws_exception() throws IOException {
        URI uri = URI.create("classpath:com/example.feature");
        String source = new String(
            readAllBytes(Paths.get("src/test/resources/io/cucumber/core/gherkin/messages/lexer-error.feature")));
        FeatureParserException exception = assertThrows(FeatureParserException.class,
            () -> parser.parse(uri, source, UUID::randomUUID));
        assertEquals("" +
                "Failed to parse resource at: classpath:com/example.feature\n" +
                "(1:1): expected: #EOF, #Language, #TagLine, #FeatureLine, #Comment, #Empty, got 'Feature  FA'\n" +
                "(3:3): expected: #EOF, #Language, #TagLine, #FeatureLine, #Comment, #Empty, got 'Scenario SA'\n" +
                "(4:5): expected: #EOF, #Language, #TagLine, #FeatureLine, #Comment, #Empty, got 'Given GA'\n" +
                "(5:5): expected: #EOF, #Language, #TagLine, #FeatureLine, #Comment, #Empty, got 'When GA'\n" +
                "(6:5): expected: #EOF, #Language, #TagLine, #FeatureLine, #Comment, #Empty, got 'Then TA'",
            exception.getMessage());
    }

}
