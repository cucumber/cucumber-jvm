package io.cucumber.core.plugin;

import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.time.format.DateTimeFormatter;

import org.junit.Test;

import io.cucumber.core.runtime.Runtime;

//TODO: Merge with the existing test
public class JsonParallelRuntimeTest {

    @Test
    public void testSingleFeature() {
        StringBuilder parallel = new StringBuilder();
        
        JSONFormatter jsonFormatterParallel = new JSONFormatter(parallel);
        jsonFormatterParallel.setDateTimeFormatter(DateTimeFormatter.ISO_DATE);
        
        Runtime.builder()
            .withArgs("--threads", "3",
                "src/test/resources/io/cucumber/core/plugin/JSONPrettyFormatterTest.feature")
            .withAdditionalPlugins(jsonFormatterParallel)
            .build()
            .run();
        
       
        StringBuilder serial = new StringBuilder();
        JSONFormatter jsonFormatterSerial = new JSONFormatter(serial);
        jsonFormatterSerial.setDateTimeFormatter(DateTimeFormatter.ISO_DATE);
       
        Runtime.builder()
            .withArgs("--threads", "1",
                "src/test/resources/io/cucumber/core/plugin/JSONPrettyFormatterTest.feature")
            .withAdditionalPlugins(jsonFormatterSerial)
            .build()
            .run();

        assertThat(parallel.toString(), sameJSONAs(serial.toString()).allowingAnyArrayOrdering());
    }

    @Test
    public void testMultipleFeatures() {
        StringBuilder parallel = new StringBuilder();

        JSONFormatter jsonFormatterParallel = new JSONFormatter(parallel);
        jsonFormatterParallel.setDateTimeFormatter(DateTimeFormatter.ISO_DATE);
        
        Runtime.builder()
            .withArgs("--threads", "3",
                "src/test/resources/io/cucumber/core/plugin/JSONPrettyFormatterTest.feature",
                "src/test/resources/io/cucumber/core/plugin/FormatterInParallel.feature")
            .withAdditionalPlugins(jsonFormatterParallel)
            .build()
            .run();


        StringBuilder serial = new StringBuilder();
        
        JSONFormatter jsonFormatterSerial = new JSONFormatter(serial);
        jsonFormatterSerial.setDateTimeFormatter(DateTimeFormatter.ISO_DATE);
        
        Runtime.builder()
            .withArgs("--threads", "1",
                "src/test/resources/io/cucumber/core/plugin/JSONPrettyFormatterTest.feature",
                "src/test/resources/io/cucumber/core/plugin/FormatterInParallel.feature")
            .withAdditionalPlugins(jsonFormatterSerial)
            .build()
            .run();

        assertThat(parallel.toString(), sameJSONAs(serial.toString()).allowingAnyArrayOrdering());
    }
}
