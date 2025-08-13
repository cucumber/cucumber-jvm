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

    private static final Path FEATURES_DIRECTORY = Paths.get("src/test/resources/features");
    private static final String FEATURES_PACKAGE = "io.cucumber.compatibility";

    private final String packageName;
    private final String id;
    private final FeatureWithLines features;

    private TestCase(String packageName, String id, FeatureWithLines features) {
        this.packageName = packageName;
        this.id = id;
        this.features = features;
    }

    static List<TestCase> testCases() throws IOException {
        List<TestCase> testCases = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(FEATURES_DIRECTORY)) {
            for (Path path : stream) {
                if (path.toFile().isDirectory()) {
                    String id = path.getFileName().toString();
                    String packageName = id.replace("-", "");
                    testCases.add(new TestCase(packageName, id, FeatureWithLines.parse(path.toString())));
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

    FeatureWithLines getFeatures() {
        return features;
    }

    Path getExpectedFile() {
        return Paths.get(FEATURES_DIRECTORY + "/" + id + "/" + id + ".ndjson");
    }

    @Override
    public String toString() {
        return id;
    }

}
