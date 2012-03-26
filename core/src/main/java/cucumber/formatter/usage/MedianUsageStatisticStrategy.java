package cucumber.formatter.usage;

import java.util.Collections;
import java.util.List;

/**
 * Calculate the median of a list of duration entries
 */
public class MedianUsageStatisticStrategy implements UsageStatisticStrategy
{
    @Override
    public Long calculate(List<Long> durationEntries)
    {
        if (verifyNoNulls(durationEntries))
        {
            return 0L;
        }
        Collections.sort(durationEntries);
        int middle = durationEntries.size() / 2;
        if (durationEntries.size() % 2 == 1)
        {
            return durationEntries.get(middle);
        }
        else
        {
            return (durationEntries.get(middle - 1) + durationEntries.get(middle)) / 2;
        }
    }

    private boolean verifyNoNulls(List<Long> durationEntries)
    {
        return durationEntries == null || durationEntries.isEmpty() || durationEntries.contains(null);
    }
}
