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

import static java.util.Comparator.comparing;

final class TestCase {

    private static final String FEATURES_DIRECTORY = "src/test/resources/features";
    private static final String FEATURES_PACKAGE = "io.cucumber.compatibility";

    private final String packageName;
    private final String id;

    private TestCase(String packageName, String id) {
        this.packageName = packageName;
        this.id = id;
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
        testCases.sort(comparing(TestCase::getId));
        return testCases;
    }

    String getId() {
        return id;
    }

    URI getGlue() {
        return GluePath.parse(FEATURES_PACKAGE + "." + packageName);
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

}
