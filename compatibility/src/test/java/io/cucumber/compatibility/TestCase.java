package io.cucumber.compatibility;

import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.feature.GluePath;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

final class TestCase {
    private static final String FEATURES_DIRECTORY = "src/test/resources/features";

    private final String packageName;
    private final String id;

    TestCase(String packageName, String id) {
        this.packageName = packageName;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    URI getGlue() {
        return GluePath.parse("io.cucumber.compatibility." + packageName);
    }

    FeatureWithLines getFeature() {
        return FeatureWithLines.parse("file:" + FEATURES_DIRECTORY + "/" + id + "/" + id + ".feature");
    }

    Path getExpectedFile() {
        return Paths.get(FEATURES_DIRECTORY + "/" + id + "/" + id + ".ndjson");
    }

    @Override
    public String toString() {
        return id;
    }

    static List<TestCase> testCases() throws IOException {
        List<TestCase> testCases = new ArrayList<>();
        Path dir = Paths.get(FEATURES_DIRECTORY);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                if (path.toFile().isDirectory()) {
                    String id = path.getFileName().toString();
                    testCases.add(new TestCase(id.replace("-", ""), id));
                }
            }
        }
        return testCases;
    }
}
