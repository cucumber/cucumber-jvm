package io.cucumber.core.gherkin;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FeatureParserTest {

    @Test
    void test() throws IOException {
        AtomicReference<URI> receivedPath = new AtomicReference<>();
        AtomicReference<String> recievedSource = new AtomicReference<>();
        AtomicReference<Supplier<UUID>> recievedIdGenerator = new AtomicReference<>();

        FeatureParser parser = new FeatureParser() {
            @Override
            public Optional<Feature> parse(URI path, String source, Supplier<UUID> idGenerator) {
                receivedPath.set(path);
                recievedSource.set(source);
                recievedIdGenerator.set(idGenerator);
                return Optional.empty();
            }

            @Override
            public String version() {
                return "Test";
            }
        };
        URI path = URI.create("classpath:com/example.feature");
        String source = "# comment";
        Supplier<UUID> idGenerator = UUID::randomUUID;
        parser.parse(path, new ByteArrayInputStream(source.getBytes(UTF_8)), idGenerator);
        assertEquals(path, receivedPath.get());
        assertEquals(source, recievedSource.get());
        assertEquals(idGenerator, recievedIdGenerator.get());

    }

}
