package io.cucumber.core.plugin;

import org.junit.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

import static java.time.Duration.ofMillis;
import static org.junit.Assert.assertEquals;

public class AverageUsageStatisticStrategyTest {
    
    @Test
    public void calculate() {
        UsageFormatter.AverageUsageStatisticStrategy averageUsageStatisticStrategy = new UsageFormatter.AverageUsageStatisticStrategy();
        Duration result = averageUsageStatisticStrategy.calculate(Arrays.asList(ofMillis(1L), ofMillis(2L), ofMillis(3L)));
        assertEquals(result, ofMillis(2));
    }

    @Test
    public void calculateNull() {
        UsageFormatter.AverageUsageStatisticStrategy averageUsageStatisticStrategy = new UsageFormatter.AverageUsageStatisticStrategy();
        Duration result = averageUsageStatisticStrategy.calculate(null);
        assertEquals(result, ofMillis(0));
    }

    @Test
    public void calculateEmptyList() {
        UsageFormatter.AverageUsageStatisticStrategy averageUsageStatisticStrategy = new UsageFormatter.AverageUsageStatisticStrategy();
        Duration result = averageUsageStatisticStrategy.calculate(Collections.<Duration>emptyList());
        assertEquals(result, ofMillis(0));
    }

    @Test
    public void calculateListWithNulls() {
        UsageFormatter.AverageUsageStatisticStrategy averageUsageStatisticStrategy = new UsageFormatter.AverageUsageStatisticStrategy();
        Duration result = averageUsageStatisticStrategy.calculate(Arrays.asList(ofMillis(3L), null));
        assertEquals(result, ofMillis(0));
    }
}
