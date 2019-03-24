package cucumber.runtime.formatter;

import cucumber.runtime.Runtime;
import cucumber.util.TimeUtils;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;


//TODO: Merge with the existing test
public class JsonParallelRuntimeTest {

    @Test
    public void testSingleFeature() {
        StringBuilder parallel = new StringBuilder();
        
        TimeUtils timeUtils = Mockito.mock(TimeUtils.class);
        Mockito.when(timeUtils.getDateTimeFromTimeStamp(Mockito.anyLong())).thenReturn("1970-01-01T00:00:00.Z");
        
        JSONFormatter jsonFormatterParallel = new JSONFormatter(parallel, timeUtils);
        
        Runtime.builder()
            .withArgs("--threads", "3",
                "src/test/resources/cucumber/runtime/formatter/JSONPrettyFormatterTest.feature")
            .withAdditionalPlugins(jsonFormatterParallel)
            .build()
            .run();

        StringBuilder serial = new StringBuilder();
        
        JSONFormatter jsonFormatterSerial = new JSONFormatter(serial, timeUtils);
        
        Runtime.builder()
            .withArgs("--threads", "1",
                "src/test/resources/cucumber/runtime/formatter/JSONPrettyFormatterTest.feature")
            .withAdditionalPlugins(jsonFormatterSerial)
            .build()
            .run();

        assertThat(parallel.toString(), sameJSONAs(serial.toString()).allowingAnyArrayOrdering());
    }

    @Test
    public void testMultipleFeatures() {
        StringBuilder parallel = new StringBuilder();
        
        TimeUtils timeUtils = Mockito.mock(TimeUtils.class);
        Mockito.when(timeUtils.getDateTimeFromTimeStamp(Mockito.anyLong())).thenReturn("1970-01-01T00:00:00.Z");
        
        JSONFormatter jsonFormatterParallel = new JSONFormatter(parallel, timeUtils);
        
        Runtime.builder()
            .withArgs("--threads", "3",
                "src/test/resources/cucumber/runtime/formatter/JSONPrettyFormatterTest.feature",
                "src/test/resources/cucumber/runtime/formatter/FormatterInParallel.feature")
            .withAdditionalPlugins(jsonFormatterParallel)
            .build()
            .run();


        StringBuilder serial = new StringBuilder();
        
        JSONFormatter jsonFormatterSerial = new JSONFormatter(serial, timeUtils);
        
        Runtime.builder()
            .withArgs("--threads", "1",
                "src/test/resources/cucumber/runtime/formatter/JSONPrettyFormatterTest.feature",
                "src/test/resources/cucumber/runtime/formatter/FormatterInParallel.feature")
            .withAdditionalPlugins(jsonFormatterSerial)
            .build()
            .run();

        assertThat(parallel.toString(), sameJSONAs(serial.toString()).allowingAnyArrayOrdering());
    }
}
