package cucumber.formatter.usage;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class MedianUsageStatisticStrategyTest
{
    @Test
    public void calculateOddEntries() throws Exception
    {
        MedianUsageStatisticStrategy medianUsageStatisticStrategy = new MedianUsageStatisticStrategy();
        Long result = medianUsageStatisticStrategy.calculate(Arrays.asList(1L, 2L, 3L));
        assertEquals(result, Long.valueOf(2));
    }

    @Test
    public void calculateEvenEntries() throws Exception
    {
        MedianUsageStatisticStrategy medianUsageStatisticStrategy = new MedianUsageStatisticStrategy();
        Long result = medianUsageStatisticStrategy.calculate(Arrays.asList(1L, 3L, 10L, 5L));
        assertEquals(result, Long.valueOf(4));
    }

    @Test
    public void calculateNull() throws Exception
    {
        MedianUsageStatisticStrategy medianUsageStatisticStrategy = new MedianUsageStatisticStrategy();
        Long result = medianUsageStatisticStrategy.calculate(null);
        assertEquals(result, Long.valueOf(0));
    }

    @Test
    public void calculateEmptylist() throws Exception
    {
        MedianUsageStatisticStrategy medianUsageStatisticStrategy = new MedianUsageStatisticStrategy();
        Long result = medianUsageStatisticStrategy.calculate(Collections.<Long>emptyList());
        assertEquals(result, Long.valueOf(0));
    }

    @Test
    public void calculateListWithNulls() throws Exception
    {
        MedianUsageStatisticStrategy medianUsageStatisticStrategy = new MedianUsageStatisticStrategy();
        Long result = medianUsageStatisticStrategy.calculate(Arrays.<Long>asList(1L, null, 3L));
        assertEquals(result, Long.valueOf(0));
    }
}
