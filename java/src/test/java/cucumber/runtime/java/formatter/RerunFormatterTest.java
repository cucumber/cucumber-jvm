package cucumber.runtime.java.formatter;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import gherkin.util.FixJava;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.notification.RunNotifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RerunFormatterTest {

    public static final String TARGET_SAMPLE_OUTPUT = "target/sample.txt";

    @Before
    public void setUp() throws Exception {
        File file = new File(TARGET_SAMPLE_OUTPUT);
        if (file.exists()) {
            assertTrue("Formatter output must be cleaned up", file.delete());
        }
    }

    @Test
    public void testRerunFormatterIsReportingFailedFeatures() throws Exception {
        new Cucumber(RerunFormatterSampleFeatures.class).run(new RunNotifier());
        assertOnOutput("cucumber/runtime/java/formatter/exaple-table.feature:6 cucumber/runtime/java/formatter/rerun_failure.feature:8:14:20 cucumber/runtime/java/formatter/rerun_failure2.feature:11");
    }

    private void assertOnOutput(String expectedMessage) throws IOException {

        try {
            final FileReader fileReader = new FileReader(TARGET_SAMPLE_OUTPUT);
            String output = FixJava.readReader(fileReader);
            fileReader.close();
            assertEquals(expectedMessage, output);
        } catch (FileNotFoundException e) {
            fail("Rerun formatter output not available " + e.getMessage());
        }
    }

    @Test
    public void testRerunFormatterNotReportingSuccessTest() throws Exception {
        new Cucumber(RerunFormatterPassingFeature.class).run(new RunNotifier());
        assertProducedOutputIsEmpty();
    }

    private void assertProducedOutputIsEmpty() {
        assertTrue(new File(TARGET_SAMPLE_OUTPUT).exists());
        assertEquals(0, new File(TARGET_SAMPLE_OUTPUT).length());
    }

    @CucumberOptions(format = "rerun:target/sample.txt", features = {"classpath:cucumber/runtime/java/formatter"})
    private class RerunFormatterSampleFeatures {

    }

    @Cucumber.Options(format = "rerun:target/sample.txt", features = {"classpath:cucumber/runtime/java/formatter/passing.feature"})
    private class RerunFormatterPassingFeature {

    }
}
