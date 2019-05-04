package io.cucumber.core.plugin;

import org.junit.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

import static java.time.Duration.ofMillis;
import static org.junit.Assert.assertEquals;

public class MedianUsageStatisticStrategyTest {
    @Test
    public void calculateOddEntries() throws Exception {
        UsageFormatter.MedianUsageStatisticStrategy medianUsageStatisticStrategy = new UsageFormatter.MedianUsageStatisticStrategy();
        Duration result = medianUsageStatisticStrategy.calculate(Arrays.asList(ofMillis(1L), ofMillis(2L), ofMillis(3L)));
        assertEquals(result, ofMillis(2));
    }

    @Test
    public void calculateEvenEntries() throws Exception {
        UsageFormatter.MedianUsageStatisticStrategy medianUsageStatisticStrategy = new UsageFormatter.MedianUsageStatisticStrategy();
        Duration result = medianUsageStatisticStrategy.calculate(Arrays.asList(ofMillis(1L), ofMillis(3L), ofMillis(10L), ofMillis(5L)));
        assertEquals(result, ofMillis(4));
    }

    @Test
    public void calculateNull() throws Exception {
        UsageFormatter.MedianUsageStatisticStrategy medianUsageStatisticStrategy = new UsageFormatter.MedianUsageStatisticStrategy();
        Duration result = medianUsageStatisticStrategy.calculate(null);
        assertEquals(result, ofMillis(0));
    }

    @Test
    public void calculateEmptylist() throws Exception {
        UsageFormatter.MedianUsageStatisticStrategy medianUsageStatisticStrategy = new UsageFormatter.MedianUsageStatisticStrategy();
        Duration result = medianUsageStatisticStrategy.calculate(Collections.<Duration>emptyList());
        assertEquals(result, ofMillis(0));
    }

    @Test
    public void calculateListWithNulls() throws Exception {
        UsageFormatter.MedianUsageStatisticStrategy medianUsageStatisticStrategy = new UsageFormatter.MedianUsageStatisticStrategy();
        Duration result = medianUsageStatisticStrategy.calculate(Arrays.<Duration>asList(ofMillis(1L), null, ofMillis(3L)));
        assertEquals(result, ofMillis(0));
    }
}
