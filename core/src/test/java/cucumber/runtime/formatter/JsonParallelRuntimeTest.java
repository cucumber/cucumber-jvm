package cucumber.runtime.formatter;

import cucumber.runtime.Runtime;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

//TODO: Merge with the existing test
public class JsonParallelRuntimeTest {

    @Test
    public void testSingleFeature() {
        StringBuilder parallel = new StringBuilder();
        Runtime.builder()
            .withArgs("--threads", "3",
                "src/test/resources/cucumber/runtime/formatter/JSONPrettyFormatterTest.feature")
            .withAdditionalPlugins(new JSONFormatter(parallel))
            .build()
            .run();

        StringBuilder serial = new StringBuilder();
        Runtime.builder()
            .withArgs("--threads", "1",
                "src/test/resources/cucumber/runtime/formatter/JSONPrettyFormatterTest.feature")
            .withAdditionalPlugins(new JSONFormatter(serial))
            .build()
            .run();

        assertThat(parallel.toString(), sameJSONAs(serial.toString()).allowingAnyArrayOrdering());
    }

    @Test
    public void testMultipleFeatures() {
        StringBuilder parallel = new StringBuilder();
        Runtime.builder()
            .withArgs("--threads", "3",
                "src/test/resources/cucumber/runtime/formatter/JSONPrettyFormatterTest.feature",
                "src/test/resources/cucumber/runtime/formatter/FormatterInParallel.feature")
            .withAdditionalPlugins(new JSONFormatter(parallel))
            .build()
            .run();


        StringBuilder serial = new StringBuilder();
        Runtime.builder()
            .withArgs("--threads", "1",
                "src/test/resources/cucumber/runtime/formatter/JSONPrettyFormatterTest.feature",
                "src/test/resources/cucumber/runtime/formatter/FormatterInParallel.feature")
            .withAdditionalPlugins(new JSONFormatter(serial))
            .build()
            .run();

        assertThat(parallel.toString(), sameJSONAs(serial.toString()).allowingAnyArrayOrdering());
    }
}
