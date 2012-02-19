package cucumber.formatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cucumber.formatter.usage.UsageStatisticStrategy;
import gherkin.formatter.Format;
import gherkin.formatter.Formatter;
import gherkin.formatter.MonochromeFormats;
import gherkin.formatter.NiceAppendable;
import gherkin.formatter.Reporter;
import gherkin.formatter.StepPrinter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

/**
 * Formatter to measure performance of steps. Aggregated results for all steps can be computed
 * by adding {@link UsageStatisticStrategy} to the usageFormatter
 */
public class UsageFormatter implements Formatter, Reporter
{
    private final MonochromeFormats monochromeFormat = new MonochromeFormats();
    private final StepPrinter stepPrinter = new StepPrinter();

    final Map<String, List<Long>> usageMap = new HashMap<String, List<Long>>();
    final Map<String, UsageStatisticStrategy> statisticStrategies = new HashMap<String, UsageStatisticStrategy>();

    private final List<Step> steps = new ArrayList<Step>();
    private final NiceAppendable out;

    private Match match;

    /**
     * Constructor
     * @param out {@link Appendable} to print the result
     */
    public UsageFormatter(Appendable out)
    {
        this.out = new NiceAppendable(out);
    }

    @Override
    public void uri(String uri)
    {
    }

    @Override
    public void feature(Feature feature)
    {
    }

    @Override
    public void background(Background background)
    {
    }

    @Override
    public void scenario(Scenario scenario)
    {
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline)
    {
    }

    @Override
    public void examples(Examples examples)
    {
    }

    @Override
    public void step(Step step)
    {
        steps.add(step);
    }

    @Override
    public void eof()
    {
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, int line)
    {
    }

    @Override
    public void done()
    {
        for (Map.Entry<String, List<Long>> usageEntry : usageMap.entrySet())
        {
            printAggregatedResult(usageEntry);
            
            String key = usageEntry.getKey();
            for (Long duration : usageEntry.getValue())
            {
                printSingleDurationEntry(key, duration);
            }
        }
    }

    private void printAggregatedResult(Map.Entry<String, List<Long>> usageEntry)
    {
        String stepName = usageEntry.getKey();
        for (Map.Entry<String, UsageStatisticStrategy> calculatorEntry : statisticStrategies.entrySet())
        {
            String calculatorName = calculatorEntry.getKey();
            UsageStatisticStrategy statisticStrategy = calculatorEntry.getValue();

            Long calculationResult = statisticStrategy.calculate(usageEntry.getValue());
            out.println(String.format("%s (%s) : %s", formatDuration(calculationResult), calculatorName, stepName));
        }
    }

    private void printSingleDurationEntry(String key, Long duration)
    {
        String formattedDuration = formatDuration(duration);
        out.println("\t" + formattedDuration + " : " + key);
    }

    private String formatDuration(Long duration)
    {
        long seconds = TimeUnit.MICROSECONDS.toSeconds(duration);
        long microSeconds = duration - TimeUnit.SECONDS.toMicros(seconds);
        return String.format("%d.%06d", seconds, microSeconds);
    }

    @Override
    public void close()
    {
        out.close();
    }

    @Override
    public void result(Result result)
    {
        if (steps.size() > 0)
        {
            Step step = steps.remove(0);
            String stepNameWithArgs = formatStepNameWithArgs(result, step);
            addUsageEntry(result, stepNameWithArgs);
        }
    }

    private String formatStepNameWithArgs(Result result, Step step)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(step.getKeyword()).append(" ");
        Format format = getFormat(result.getStatus());
        Format argFormat = getArgFormat(result.getStatus());
        stepPrinter.writeStep(new NiceAppendable(buffer), format, argFormat, step.getName(), match.getArguments());

        return buffer.toString();
    }

    private void addUsageEntry(Result result, String stepNameWithArgs)
    {
        List<Long> durationEntries = usageMap.get(stepNameWithArgs);
        if (durationEntries == null)
        {
            durationEntries = new ArrayList<Long>();
            usageMap.put(stepNameWithArgs, durationEntries);
        }
        durationEntries.add(durationInMillis(result));
    }

    private Long durationInMillis(Result result)
    {
        long duration;
        if (result.getDuration() == null)
        {
            duration = 0;
        } else
        {
            duration = result.getDuration() / 1000;
        }
        return duration;
    }

    @Override
    public void match(Match match)
    {
        this.match = match;
    }

    @Override
    public void embedding(String mimeType, byte[] data)
    {
    }

    private Format getFormat(String key) {
        return monochromeFormat.get(key);
    }

    private Format getArgFormat(String key) {
        return monochromeFormat.get(key + "_arg");
    }

    /**
     * Add a {@link UsageStatisticStrategy} to the formatter
     * @param key the key, will be displayed in the output
     * @param strategy the strategy
     */
    public void addUsageStatisticStrategy(String key, UsageStatisticStrategy strategy)
    {
        statisticStrategies.put(key, strategy);
    }
}
