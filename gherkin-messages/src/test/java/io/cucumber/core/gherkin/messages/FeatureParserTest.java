package io.cucumber.core.gherkin.messages;

import io.cucumber.core.gherkin.Feature;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import static java.nio.file.Files.readAllBytes;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FeatureParserTest {

    private final GherkinMessagesFeatureParser parser = new GherkinMessagesFeatureParser();

    @Test
    void feature_file_without_pickles_is_parsed_but_produces_no_feature() throws IOException {
        URI uri = URI.create("classpath:com/example.feature");
        String source = new String(readAllBytes(Paths.get("src/test/resources/io/cucumber/core/gherkin/messages/no-pickles.feature")));
        Optional<Feature> feature = parser.parse(uri, source, UUID::randomUUID);
        assertFalse(feature.isPresent());
    }

    @Test
    void empty_feature_file_is_parsed_but_produces_no_feature() throws IOException {
        URI uri = URI.create("classpath:com/example.feature");
        String source = new String(readAllBytes(Paths.get("src/test/resources/io/cucumber/core/gherkin/messages/empty.feature")));
        Optional<Feature> feature = parser.parse(uri, source, UUID::randomUUID);
        assertFalse(feature.isPresent());
    }

}
