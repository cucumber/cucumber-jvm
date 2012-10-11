package cucumber.runtime.formatter;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class AverageUsageStatisticStrategyTest {
    @Test
    public void calculate() throws Exception {
        UsageFormatter.AverageUsageStatisticStrategy averageUsageStatisticStrategy = new UsageFormatter.AverageUsageStatisticStrategy();
        Long result = averageUsageStatisticStrategy.calculate(Arrays.asList(1L, 2L, 3L));
        assertEquals(result, Long.valueOf(2));
    }

    @Test
    public void calculateNull() throws Exception {
        UsageFormatter.AverageUsageStatisticStrategy averageUsageStatisticStrategy = new UsageFormatter.AverageUsageStatisticStrategy();
        Long result = averageUsageStatisticStrategy.calculate(null);
        assertEquals(result, Long.valueOf(0));
    }

    @Test
    public void calculateEmptylist() throws Exception {
        UsageFormatter.AverageUsageStatisticStrategy averageUsageStatisticStrategy = new UsageFormatter.AverageUsageStatisticStrategy();
        Long result = averageUsageStatisticStrategy.calculate(Collections.<Long>emptyList());
        assertEquals(result, Long.valueOf(0));
    }

    @Test
    public void calculateListWithNulls() throws Exception {
        UsageFormatter.AverageUsageStatisticStrategy averageUsageStatisticStrategy = new UsageFormatter.AverageUsageStatisticStrategy();
        Long result = averageUsageStatisticStrategy.calculate(Arrays.<Long>asList(3L, null));
        assertEquals(result, Long.valueOf(0));
    }
}
