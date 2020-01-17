package io.cucumber.compatibility;

import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.feature.GluePath;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.plugin.MessageFormatter;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.nio.file.Files.readAllLines;
import static java.time.Clock.fixed;
import static java.time.Instant.ofEpochSecond;
import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompatibilityTest {

    private final AtomicLong id = new AtomicLong();
    private final Supplier<UUID> idGenerator = () -> new UUID(0L, id.getAndIncrement());

    public enum TestCase {
        attachments("attachments", "attachments"),
        datatables("datatables", "data-tables"),
        hooks("hooks", "hooks"),
        parametertypes("parametertypes", "parameter-types"),
        stacktraces("stacktraces", "stack-traces");

        private final String packageName;
        private final String id;

        TestCase(String packageName, String id) {
            this.packageName = packageName;
            this.id = id;
        }

        private URI getGlue() {
            return GluePath.parse("io.cucumber.compatibility." + packageName);
        }

        private FeatureWithLines getFeature() {
            return FeatureWithLines.parse("file:src/test/resources/features/" + id + "/" + id + ".feature");
        }

        private Path getExpectedFile() {
            return Paths.get("src/test/resources/features/" + id + "/" + id + ".ndjson");
        }

    }

//    @Disabled
    @ParameterizedTest
    @EnumSource(TestCase.class)
    void produces_expected_output_for(TestCase testCase) throws IOException {
        File parentDir = new File("target/messages/" + testCase.id);
        parentDir.mkdirs();
        File output = new File(parentDir, "out.ndjson");

        Runtime.builder()
            .withRuntimeOptions(new RuntimeOptionsBuilder()
                .addGlue(testCase.getGlue())
                .addFeature(testCase.getFeature())
                .build())
            .withAdditionalPlugins(new MessageFormatter(output))
            .withEventBus(new TimeServiceEventBus(fixed(ofEpochSecond(0), UTC), idGenerator))
            .build()
            .run();

        List<String> actual = readAllLines(output.toPath());
        List<String> expected = readAllLines(testCase.getExpectedFile());

        assertEquals(
            replaceAndSort(expected),
            replaceAndSort(actual)
        );


    }

    private String replaceAndSort(List<String> actual) {
        String file = Paths.get("src/test/resources").toAbsolutePath().toUri().toString();
        // Not intended for the final comparison but to show how many "easy"
        // differences we still have to solve.
        return actual.stream()
            .map(s ->
                s.replaceAll(file, "")
                    .replaceAll("\"nanos\":[0-9]+", "\"nanos\":0")
                    .replaceAll("\"id\":\"[0-9a-z\\-]+\"", "\"id\":\"0\"")
                    .replaceAll("\"pickleId\":\"[0-9a-z\\-]+\"", "\"pickleId\":\"0\"")
                    .replaceAll("\"testStepId\":\"[0-9a-z\\-]+\"", "\"testStepId\":\"0\"")
                    .replaceAll("\"pickleStepId\":\"[0-9a-z\\-]+\"", "\"pickleStepId\":\"0\"")
                    .replaceAll("\"testCaseId\":\"[0-9a-z\\-]+\"", "\"testCaseId\":\"0\"")
                    .replaceAll("\"testCaseStartedId\":\"[0-9a-z\\-]+\"", "\"testCaseStartedId\":\"0\"")
                    .replaceAll("\"astNodeIds\":\\[[0-9a-z\\-\",]+]", "\"astNodeIds\":[1]")
                    .replaceAll("\"stepDefinitionIds\":\\[[0-9a-z\\-\",]+]", "\"astNodeIds\":[1]")
                )
            .sorted()
            .collect(Collectors.joining("\n"));
    }
}
