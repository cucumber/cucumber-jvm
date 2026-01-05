package io.cucumber.compatibility;

import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.feature.GluePath;
import org.junit.platform.commons.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

final class TestCase {

    static final String TEST_CASES_PACKAGE = "io.cucumber.compatibilitykit";
    static final String GLUE_PACKAGE = "io.cucumber.compatibility";

    private final String id;
    private final String testCaseResourceName;
    private final Resource expected;

    TestCase(Resource expected) {
        this.expected = expected;
        String expectedResourceName = expected.getName();
        this.testCaseResourceName = expectedResourceName.substring(0, expectedResourceName.lastIndexOf('/'));
        this.id = testCaseResourceName.substring(testCaseResourceName.lastIndexOf('/') + 1);
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
