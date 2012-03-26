package cucumber.formatter.usage;

import java.util.List;

/**
 * Calculate a statistical value to be displayed in the usage-file
 */
public interface UsageStatisticStrategy
{
    /**
     * @param durationEntries list of execution times of steps as nanoseconds
     * @return a statistical value (e.g. median, average, ..)
     */
    Long calculate(List<Long> durationEntries);
}
