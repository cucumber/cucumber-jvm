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
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

final class TestCase {

    private static final String FEATURES_DIRECTORY = "src/test/resources/features";
    private static final String FEATURES_PACKAGE = "io.cucumber.compatibility";

    private final String packageName;
    private final String id;
    private final List<String> featurePaths;

    private TestCase(String packageName, String id, List<String> featurePaths) {
        this.packageName = packageName;
        this.id = id;
        this.featurePaths = featurePaths;
    }

    static List<TestCase> testCases() throws IOException {
        List<TestCase> testCases = new ArrayList<>();
        Path dir = Paths.get(FEATURES_DIRECTORY);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                if (path.toFile().isDirectory()) {
                    String id = path.getFileName().toString();
                    List<String> featurePaths = discoverFeaturePaths(path);
                    testCases.add(new TestCase(id.replace("-", ""), id, featurePaths));
                }
            }
        }
        testCases.sort(comparing(TestCase::getId));
        return testCases;
    }

    private static List<String> discoverFeaturePaths(Path testCaseDir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(testCaseDir, "*.feature")) {
            List<String> featurePaths = new ArrayList<>();
            for (Path featurePath : stream) {
                featurePaths.add(featurePath.toUri().toString());
            }
            return featurePaths.stream()
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    String getId() {
        return id;
    }

    URI getGlue() {
        return GluePath.parse(FEATURES_PACKAGE + "." + packageName);
    }

    List<FeatureWithLines> getFeatures() {
        return featurePaths.stream()
                .map(FeatureWithLines::parse)
                .collect(Collectors.toList());
    }

    Path getExpectedFile() {
        return Paths.get(FEATURES_DIRECTORY + "/" + id + "/" + id + ".ndjson");
    }

    @Override
    public String toString() {
        return id;
    }

}
