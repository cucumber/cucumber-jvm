package cucumber.runtime.formatter;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;

import cucumber.api.Result;
import cucumber.api.formatter.AnsiEscapes;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Locale;

import cucumber.runtime.formatter.Stats;
import org.junit.Test;

public class StatsTest {
    private static final long ANY_TIME = 1234567890;
    private static final long ONE_MILLI_SECOND = 1000000;
    private static final long ONE_HOUR = 60 * Stats.ONE_MINUTE;

    @Test
    public void should_print_zero_scenarios_zero_steps_if_nothing_has_executed() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        counter.printStats(new PrintStream(baos));

        assertThat(baos.toString(), startsWith(String.format(
                "0 Scenarios%n" +
                "0 Steps%n")));
    }

    @Test
    public void should_only_print_sub_counts_if_not_zero() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.addStep(Result.Type.PASSED);
        counter.addStep(Result.Type.PASSED);
        counter.addStep(Result.Type.PASSED);
        counter.addScenario(Result.Type.PASSED, "scenario designation");
        counter.printStats(new PrintStream(baos));

        assertThat(baos.toString(), startsWith(String.format(
                "1 Scenarios (1 passed)%n" +
                "3 Steps (3 passed)%n")));
    }

    @Test
    public void should_print_sub_counts_in_order_failed_ambiguous_skipped_pending_undefined_passed() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        addOneStepScenario(counter, Result.Type.PASSED);
        addOneStepScenario(counter, Result.Type.FAILED);
        addOneStepScenario(counter, Result.Type.AMBIGUOUS);
        addOneStepScenario(counter, Result.Type.PENDING);
        addOneStepScenario(counter, Result.Type.UNDEFINED);
        addOneStepScenario(counter, Result.Type.SKIPPED);
        counter.printStats(new PrintStream(baos));

        assertThat(baos.toString(), containsString(String.format("" +
                "6 Scenarios (1 failed, 1 ambiguous, 1 skipped, 1 pending, 1 undefined, 1 passed)%n" +
                "6 Steps (1 failed, 1 ambiguous, 1 skipped, 1 pending, 1 undefined, 1 passed)%n")));
    }

    @Test
    public void should_print_sub_counts_in_order_failed_ambiguous_skipped_undefined_passed_in_color() {
        Stats counter = createColorSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        addOneStepScenario(counter, Result.Type.PASSED);
        addOneStepScenario(counter, Result.Type.FAILED);
        addOneStepScenario(counter, Result.Type.AMBIGUOUS);
        addOneStepScenario(counter, Result.Type.PENDING);
        addOneStepScenario(counter, Result.Type.UNDEFINED);
        addOneStepScenario(counter, Result.Type.SKIPPED);
        counter.printStats(new PrintStream(baos));

        String colorSubCounts = "" +
                AnsiEscapes.RED + "1 failed" + AnsiEscapes.RESET + ", " +
                AnsiEscapes.RED + "1 ambiguous" + AnsiEscapes.RESET + ", " +
                AnsiEscapes.CYAN + "1 skipped" + AnsiEscapes.RESET + ", " +
                AnsiEscapes.YELLOW + "1 pending" + AnsiEscapes.RESET + ", " +
                AnsiEscapes.YELLOW + "1 undefined" + AnsiEscapes.RESET + ", " +
                AnsiEscapes.GREEN + "1 passed" + AnsiEscapes.RESET;
        assertThat(baos.toString(), containsString(String.format("" +
                "6 Scenarios (" + colorSubCounts + ")%n" +
                "6 Steps (" + colorSubCounts + ")%n")));
    }

    @Test
    public void should_print_zero_m_zero_s_if_nothing_has_executed() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.printStats(new PrintStream(baos));

        assertThat(baos.toString(), endsWith(String.format(
                "0m0.000s%n")));
    }

    @Test
    public void should_report_the_difference_between_finish_time_and_start_time() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.setStartTime(ANY_TIME);
        counter.setFinishTime(ANY_TIME + 4*ONE_MILLI_SECOND);
        counter.printStats(new PrintStream(baos));

        assertThat(baos.toString(), endsWith(String.format(
                "0m0.004s%n")));
    }

    @Test
    public void should_print_minutes_seconds_and_milliseconds() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.setStartTime(ANY_TIME);
        counter.setFinishTime(ANY_TIME + Stats.ONE_MINUTE + Stats.ONE_SECOND + ONE_MILLI_SECOND);
        counter.printStats(new PrintStream(baos));

        assertThat(baos.toString(), endsWith(String.format(
                "1m1.001s%n")));
    }

    @Test
    public void should_print_minutes_instead_of_hours() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.setStartTime(ANY_TIME);
        counter.setFinishTime(ANY_TIME + ONE_HOUR + Stats.ONE_MINUTE);
        counter.printStats(new PrintStream(baos));

        assertThat(baos.toString(), endsWith(String.format(
                "61m0.000s%n")));
    }

    @Test
    public void should_use_locale_for_decimal_separator() {
        Stats counter = new Stats(Locale.GERMANY);
        counter.setStrict(true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.setStartTime(ANY_TIME);
        counter.setFinishTime(ANY_TIME + Stats.ONE_MINUTE + Stats.ONE_SECOND + ONE_MILLI_SECOND);
        counter.printStats(new PrintStream(baos));

        assertThat(baos.toString(), endsWith(String.format(
                "1m1,001s%n")));
    }

    @Test
    public void should_print_failed_ambiguous_scenarios() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.addStep(Result.Type.FAILED);
        counter.addScenario(Result.Type.FAILED, "path/file.feature:3 # Scenario: scenario_name");
        counter.addStep(Result.Type.AMBIGUOUS);
        counter.addScenario(Result.Type.AMBIGUOUS, "path/file.feature:3 # Scenario: scenario_name");
        counter.addStep(Result.Type.UNDEFINED);
        counter.addScenario(Result.Type.UNDEFINED, "path/file.feature:3 # Scenario: scenario_name");
        counter.addStep(Result.Type.PENDING);
        counter.addScenario(Result.Type.PENDING, "path/file.feature:3 # Scenario: scenario_name");
        counter.printStats(new PrintStream(baos));

        assertThat(baos.toString(), startsWith(String.format("" +
                "Failed scenarios:%n" +
                "path/file.feature:3 # Scenario: scenario_name%n" +
                "%n" +
                "Ambiguous scenarios:%n" +
                "path/file.feature:3 # Scenario: scenario_name%n" +
                "%n" +
                "4 Scenarios")));
    }

    @Test
    public void should_print_failed_ambiguous_pending_undefined_scenarios_if_strict() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.addStep(Result.Type.FAILED);
        counter.addScenario(Result.Type.FAILED, "path/file.feature:3 # Scenario: scenario_name");
        counter.addStep(Result.Type.AMBIGUOUS);
        counter.addScenario(Result.Type.AMBIGUOUS, "path/file.feature:3 # Scenario: scenario_name");
        counter.addStep(Result.Type.UNDEFINED);
        counter.addScenario(Result.Type.UNDEFINED, "path/file.feature:3 # Scenario: scenario_name");
        counter.addStep(Result.Type.PENDING);
        counter.addScenario(Result.Type.PENDING, "path/file.feature:3 # Scenario: scenario_name");
        counter.setStrict(true);
        counter.printStats(new PrintStream(baos));

        assertThat(baos.toString(), startsWith(String.format("" +
                "Failed scenarios:%n" +
                "path/file.feature:3 # Scenario: scenario_name%n" +
                "%n" +
                "Ambiguous scenarios:%n" +
                "path/file.feature:3 # Scenario: scenario_name%n" +
                "%n" +
                "Pending scenarios:%n" +
                "path/file.feature:3 # Scenario: scenario_name%n" +
                "%n" +
                "Undefined scenarios:%n" +
                "path/file.feature:3 # Scenario: scenario_name%n" +
                "%n" +
                "4 Scenarios")));
    }

    private void addOneStepScenario(Stats counter, Result.Type status) {
        counter.addStep(status);
        counter.addScenario(status, "scenario designation");
    }

    private Stats createMonochromeSummaryCounter() {
        Stats stats = new Stats(Locale.US);
        stats.setMonochrome(true);
        return stats;
    }

    private Stats createColorSummaryCounter() {
        return new Stats(Locale.US);
    }

}
