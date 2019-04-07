package io.cucumber.core.plugin;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class AverageUsageStatisticStrategyTest {
    @Test
    public void calculate() {
        UsageFormatter.AverageUsageStatisticStrategy averageUsageStatisticStrategy = new UsageFormatter.AverageUsageStatisticStrategy();
        Long result = averageUsageStatisticStrategy.calculate(Arrays.asList(1L, 2L, 3L));
        assertEquals(result, Long.valueOf(2));
    }

    @Test
    public void calculateNull() {
        UsageFormatter.AverageUsageStatisticStrategy averageUsageStatisticStrategy = new UsageFormatter.AverageUsageStatisticStrategy();
        Long result = averageUsageStatisticStrategy.calculate(null);
        assertEquals(result, Long.valueOf(0));
    }

    @Test
    public void calculateEmptyList() {
        UsageFormatter.AverageUsageStatisticStrategy averageUsageStatisticStrategy = new UsageFormatter.AverageUsageStatisticStrategy();
        Long result = averageUsageStatisticStrategy.calculate(Collections.<Long>emptyList());
        assertEquals(result, Long.valueOf(0));
    }

    @Test
    public void calculateListWithNulls() {
        UsageFormatter.AverageUsageStatisticStrategy averageUsageStatisticStrategy = new UsageFormatter.AverageUsageStatisticStrategy();
        Long result = averageUsageStatisticStrategy.calculate(Arrays.asList(3L, null));
        assertEquals(result, Long.valueOf(0));
    }
}
