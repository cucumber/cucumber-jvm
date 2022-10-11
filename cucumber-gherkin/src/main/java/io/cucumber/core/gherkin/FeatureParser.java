package io.cucumber.core.gherkin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;

public interface FeatureParser {

    @Deprecated
    Optional<Feature> parse(URI path, String source, Supplier<UUID> idGenerator);

    default Optional<Feature> parse(URI path, InputStream source, Supplier<UUID> idGenerator) throws IOException {
        final byte[] buffer = new byte[2 * 1024]; // 2KB
        int read;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            while (-1 != (read = source.read(buffer, 0, buffer.length))) {
                outputStream.write(buffer, 0, read);
            }
            String s = new String(outputStream.toByteArray(), UTF_8);
            return parse(path, s, idGenerator);
        }
    }

    String version();

}
