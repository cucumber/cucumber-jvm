package cucumber.runtime;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;

import gherkin.formatter.ansi.AnsiEscapes;
import gherkin.formatter.model.Result;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Locale;

import org.junit.Test;

public class StatsTest {
    public static final long ONE_MILLI_SECOND = 1000000;
    private static final long ONE_HOUR = 60 * Stats.ONE_MINUTE;

    @Test
    public void should_print_zero_scenarios_zero_steps_if_nothing_has_executed() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.printStats(new PrintStream(baos), isStrict(false));

        assertThat(baos.toString(), startsWith(String.format(
                "0 Scenarios%n" +
                "0 Steps%n")));
    }

    @Test
    public void should_only_print_sub_counts_if_not_zero() {
        Stats counter = createMonochromeSummaryCounter();
        Result passedResult = createResultWithStatus(Result.PASSED);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.addStep(passedResult);
        counter.addStep(passedResult);
        counter.addStep(passedResult);
        counter.addScenario(Result.PASSED);
        counter.printStats(new PrintStream(baos), isStrict(false));

        assertThat(baos.toString(), startsWith(String.format(
                "1 Scenarios (1 passed)%n" +
                "3 Steps (3 passed)%n")));
    }

    @Test
    public void should_print_sub_counts_in_order_failed_skipped_pending_undefined_passed() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        addOneStepScenario(counter, Result.PASSED);
        addOneStepScenario(counter, Result.FAILED);
        addOneStepScenario(counter, Stats.PENDING);
        addOneStepScenario(counter, Result.UNDEFINED.getStatus());
        addOneStepScenario(counter, Result.SKIPPED.getStatus());
        counter.printStats(new PrintStream(baos), isStrict(false));

        assertThat(baos.toString(), containsString(String.format("" +
                "5 Scenarios (1 failed, 1 skipped, 1 pending, 1 undefined, 1 passed)%n" +
                "5 Steps (1 failed, 1 skipped, 1 pending, 1 undefined, 1 passed)%n")));
    }

    @Test
    public void should_print_sub_counts_in_order_failed_skipped_undefined_passed_in_color() {
        Stats counter = createColorSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        addOneStepScenario(counter, Result.PASSED);
        addOneStepScenario(counter, Result.FAILED);
        addOneStepScenario(counter, Stats.PENDING);
        addOneStepScenario(counter, Result.UNDEFINED.getStatus());
        addOneStepScenario(counter, Result.SKIPPED.getStatus());
        counter.printStats(new PrintStream(baos), isStrict(false));

        String colorSubCounts =
                AnsiEscapes.RED + "1 failed" + AnsiEscapes.RESET + ", " +
                AnsiEscapes.CYAN + "1 skipped" + AnsiEscapes.RESET + ", " +
                AnsiEscapes.YELLOW + "1 pending" + AnsiEscapes.RESET + ", " +
                AnsiEscapes.YELLOW + "1 undefined" + AnsiEscapes.RESET + ", " +
                AnsiEscapes.GREEN + "1 passed" + AnsiEscapes.RESET;
        assertThat(baos.toString(), containsString(String.format("" +
                "5 Scenarios (" + colorSubCounts + ")%n" +
                "5 Steps (" + colorSubCounts + ")%n")));
    }

    @Test
    public void should_print_zero_m_zero_s_if_nothing_has_executed() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.printStats(new PrintStream(baos), isStrict(false));

        assertThat(baos.toString(), endsWith(String.format(
                "0m0.000s%n")));
    }

    @Test
    public void should_include_hook_time_and_step_time_has_executed() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.addHookTime(ONE_MILLI_SECOND);
        counter.addStep(new Result(Result.PASSED, ONE_MILLI_SECOND, null));
        counter.addStep(new Result(Result.PASSED, ONE_MILLI_SECOND, null));
        counter.addHookTime(ONE_MILLI_SECOND);
        counter.printStats(new PrintStream(baos), isStrict(false));

        assertThat(baos.toString(), endsWith(String.format(
                "0m0.004s%n")));
    }

    @Test
    public void should_print_minutes_seconds_and_milliseconds() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.addStep(new Result(Result.PASSED, Stats.ONE_MINUTE, null));
        counter.addStep(new Result(Result.PASSED, Stats.ONE_SECOND, null));
        counter.addStep(new Result(Result.PASSED, ONE_MILLI_SECOND, null));
        counter.printStats(new PrintStream(baos), isStrict(false));

        assertThat(baos.toString(), endsWith(String.format(
                "1m1.001s%n")));
    }

    @Test
    public void should_print_minutes_instead_of_hours() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.addStep(new Result(Result.PASSED, ONE_HOUR, null));
        counter.addStep(new Result(Result.PASSED, Stats.ONE_MINUTE, null));
        counter.printStats(new PrintStream(baos), isStrict(false));

        assertThat(baos.toString(), endsWith(String.format(
                "61m0.000s%n")));
    }

    @Test
    public void should_use_locale_for_decimal_separator() {
        Stats counter = new Stats(true, Locale.GERMANY);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.addStep(new Result(Result.PASSED, Stats.ONE_MINUTE, null));
        counter.addStep(new Result(Result.PASSED, Stats.ONE_SECOND, null));
        counter.addStep(new Result(Result.PASSED, ONE_MILLI_SECOND, null));
        counter.printStats(new PrintStream(baos), isStrict(false));

        assertThat(baos.toString(), endsWith(String.format(
                "1m1,001s%n")));
    }

    @Test
    public void should_print_failed_scenarios() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.addStep(createResultWithStatus(Result.FAILED));
        counter.addScenario(Result.FAILED, "path/file.feature:3 # Scenario: scenario_name");
        counter.addStep(createResultWithStatus(Result.UNDEFINED.getStatus()));
        counter.addScenario(Result.UNDEFINED.getStatus(), "path/file.feature:3 # Scenario: scenario_name");
        counter.addStep(createResultWithStatus(Stats.PENDING));
        counter.addScenario(Stats.PENDING, "path/file.feature:3 # Scenario: scenario_name");
        counter.printStats(new PrintStream(baos), isStrict(false));

        assertThat(baos.toString(), startsWith(String.format("" +
                "Failed scenarios:%n" +
                "path/file.feature:3 # Scenario: scenario_name%n" +
                "%n" +
                "3 Scenarios")));
    }

    @Test
    public void should_print_failed_pending_undefined_scenarios_if_strict() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.addStep(createResultWithStatus(Result.FAILED));
        counter.addScenario(Result.FAILED, "path/file.feature:3 # Scenario: scenario_name");
        counter.addStep(createResultWithStatus(Result.UNDEFINED.getStatus()));
        counter.addScenario(Result.UNDEFINED.getStatus(), "path/file.feature:3 # Scenario: scenario_name");
        counter.addStep(createResultWithStatus(Stats.PENDING));
        counter.addScenario(Stats.PENDING, "path/file.feature:3 # Scenario: scenario_name");
        counter.printStats(new PrintStream(baos), isStrict(true));

        assertThat(baos.toString(), startsWith(String.format("" +
                "Failed scenarios:%n" +
                "path/file.feature:3 # Scenario: scenario_name%n" +
                "%n" +
                "Pending scenarios:%n" +
                "path/file.feature:3 # Scenario: scenario_name%n" +
                "%n" +
                "Undefined scenarios:%n" +
                "path/file.feature:3 # Scenario: scenario_name%n" +
                "%n" +
                "3 Scenarios")));
    }

    private void addOneStepScenario(Stats counter, String status) {
        counter.addStep(createResultWithStatus(status));
        counter.addScenario(status, "scenario designation");
    }

    private Result createResultWithStatus(String status) {
        return new Result(status, 0l, null);
    }

    private Stats createMonochromeSummaryCounter() {
        return new Stats(true, Locale.US);
    }

    private Stats createColorSummaryCounter() {
        return new Stats(false, Locale.US);
    }

    private boolean isStrict(boolean isStrict) {
        return isStrict;
    }
}
