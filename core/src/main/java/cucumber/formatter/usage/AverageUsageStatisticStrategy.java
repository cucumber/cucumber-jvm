package cucumber.formatter.usage;

import java.util.List;

/**
 * Calculate the average of a list of duration entries
 */
public class AverageUsageStatisticStrategy implements UsageStatisticStrategy
{
    @Override
    public Long calculate(List<Long> durationEntries)
    {
        if (verifyNoNulls(durationEntries))
        {
            return 0L;
        }
        
        long sum = 0;
        for (Long duration : durationEntries)
        {
            sum += duration;
        }
        return sum / durationEntries.size();
    }

    private boolean verifyNoNulls(List<Long> durationEntries)
    {
        return durationEntries == null || durationEntries.isEmpty() || durationEntries.contains(null);
    }
}
