package io.cucumber.compatibility;

import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.feature.GluePath;
import org.junit.platform.commons.support.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.comparing;
import static org.junit.platform.commons.support.ReflectionSupport.findAllResourcesInPackage;

final class TestCase {

    private static final String TEST_CASES_PACKAGE = "io.cucumber.compatibilitykit";
    private static final String GLUE_PACKAGE = "io.cucumber.compatibility";

    private final String id;
    private final String testCaseResourceName;
    private final Resource expected;

    private TestCase(Resource expected) {
        this.expected = expected;
        String expectedResourceName = expected.getName();
        this.testCaseResourceName = expectedResourceName.substring(0, expectedResourceName.lastIndexOf('/'));
        this.id = testCaseResourceName.substring(testCaseResourceName.lastIndexOf('/') + 1);
    }

    static List<TestCase> testCases() {
        List<Resource> expectedFiles = findAllResourcesInPackage(TEST_CASES_PACKAGE,
            resource -> resource.getName().endsWith(".ndjson"));
        List<TestCase> testCases = new ArrayList<>();
        for (Resource expected : expectedFiles) {
            testCases.add(new TestCase(expected));
        }
        testCases.sort(comparing(TestCase::getId));
        return testCases;
    }

    String getId() {
        return id;
    }

    URI getGlue() {
        return GluePath.parse(GLUE_PACKAGE + "." + id.replace("-", ""));
    }

    FeatureWithLines getFeatures() {
        return FeatureWithLines.parse("classpath:" + testCaseResourceName);
    }

    InputStream getExpectedFile() throws IOException {
        return expected.getInputStream();
    }

    @Override
    public String toString() {
        return id;
    }

}
