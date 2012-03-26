package cucumber.formatter.usage;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class AverageUsageStatisticStrategyTest
{
    @Test
    public void calculate() throws Exception
    {
        AverageUsageStatisticStrategy averageUsageStatisticStrategy = new AverageUsageStatisticStrategy();
        Long result = averageUsageStatisticStrategy.calculate(Arrays.asList(1L, 2L, 3L));
        assertEquals(result, Long.valueOf(2));
    }

    @Test
    public void calculateNull() throws Exception
    {
        AverageUsageStatisticStrategy averageUsageStatisticStrategy = new AverageUsageStatisticStrategy();
        Long result = averageUsageStatisticStrategy.calculate(null);
        assertEquals(result, Long.valueOf(0));
    }

    @Test
    public void calculateEmptylist() throws Exception
    {
        AverageUsageStatisticStrategy averageUsageStatisticStrategy = new AverageUsageStatisticStrategy();
        Long result = averageUsageStatisticStrategy.calculate(Collections.<Long>emptyList());
        assertEquals(result, Long.valueOf(0));
    }

    @Test
    public void calculateListWithNulls() throws Exception
    {
        AverageUsageStatisticStrategy averageUsageStatisticStrategy = new AverageUsageStatisticStrategy();
        Long result = averageUsageStatisticStrategy.calculate(Arrays.<Long>asList(3L, null));
        assertEquals(result, Long.valueOf(0));
    }
}
