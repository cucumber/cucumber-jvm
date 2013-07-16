package cucumber.runtime;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;

import gherkin.formatter.ansi.AnsiEscapes;
import gherkin.formatter.model.Result;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Locale;

import org.junit.Test;

public class SummaryCounterTest {
    public static final long ONE_MILLI_SECOND = 1000000;
    private static final long ONE_HOUR = 60 * SummaryCounter.ONE_MINUTE;

    @Test
    public void should_print_zero_scenarios_zero_steps_if_nothing_has_executed() {
        SummaryCounter counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.printSummary(new PrintStream(baos));

        assertThat(baos.toString(), startsWith(String.format(
                "0 Scenarios%n" +
                "0 Steps%n")));
    }

    @Test
    public void should_only_print_sub_counts_if_not_zero() {
        SummaryCounter counter = createMonochromeSummaryCounter();
        Result passedResult = createResultWithStatus(Result.PASSED);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.addStep(passedResult);
        counter.addStep(passedResult);
        counter.addStep(passedResult);
        counter.addScenario(Result.PASSED);
        counter.printSummary(new PrintStream(baos));

        assertThat(baos.toString(), startsWith(String.format(
                "1 Scenarios (1 passed)%n" +
                "3 Steps (3 passed)%n")));
    }

    @Test
    public void should_print_sub_counts_in_order_failed_skipped_pending_undefined_passed() {
        SummaryCounter counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        addOneStepScenario(counter, Result.PASSED);
        addOneStepScenario(counter, Result.FAILED);
        addOneStepScenario(counter, SummaryCounter.PENDING);
        addOneStepScenario(counter, Result.UNDEFINED.getStatus());
        addOneStepScenario(counter, Result.SKIPPED.getStatus());
        counter.printSummary(new PrintStream(baos));

        assertThat(baos.toString(), startsWith(String.format(
                "5 Scenarios (1 failed, 1 skipped, 1 pending, 1 undefined, 1 passed)%n" +
                "5 Steps (1 failed, 1 skipped, 1 pending, 1 undefined, 1 passed)%n")));
    }

    @Test
    public void should_print_sub_counts_in_order_failed_skipped_undefined_passed_in_color() {
        SummaryCounter counter = createColorSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        addOneStepScenario(counter, Result.PASSED);
        addOneStepScenario(counter, Result.FAILED);
        addOneStepScenario(counter, SummaryCounter.PENDING);
        addOneStepScenario(counter, Result.UNDEFINED.getStatus());
        addOneStepScenario(counter, Result.SKIPPED.getStatus());
        counter.printSummary(new PrintStream(baos));

        String colorSubCounts =
                AnsiEscapes.RED + "1 failed" + AnsiEscapes.RESET + ", " +
                AnsiEscapes.CYAN + "1 skipped" + AnsiEscapes.RESET + ", " +
                AnsiEscapes.YELLOW + "1 pending" + AnsiEscapes.RESET + ", " +
                AnsiEscapes.YELLOW + "1 undefined" + AnsiEscapes.RESET + ", " +
                AnsiEscapes.GREEN + "1 passed" + AnsiEscapes.RESET;
        assertThat(baos.toString(), startsWith(String.format(
                "5 Scenarios (" + colorSubCounts + ")%n" +
                "5 Steps (" + colorSubCounts + ")%n")));
    }

    @Test
    public void should_print_zero_m_zero_s_if_nothing_has_executed() {
        SummaryCounter counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.printSummary(new PrintStream(baos));

        assertThat(baos.toString(), endsWith(String.format(
                "0m0.000s%n")));
    }

    @Test
    public void should_include_hook_time_and_step_time_has_executed() {
        SummaryCounter counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.addHookTime(ONE_MILLI_SECOND);
        counter.addStep(new Result(Result.PASSED, ONE_MILLI_SECOND, null));
        counter.addStep(new Result(Result.PASSED, ONE_MILLI_SECOND, null));
        counter.addHookTime(ONE_MILLI_SECOND);
        counter.printSummary(new PrintStream(baos));

        assertThat(baos.toString(), endsWith(String.format(
                "0m0.004s%n")));
    }

    @Test
    public void should_print_minutes_seconds_and_milliseconds() {
        SummaryCounter counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.addStep(new Result(Result.PASSED, SummaryCounter.ONE_MINUTE, null));
        counter.addStep(new Result(Result.PASSED, SummaryCounter.ONE_SECOND, null));
        counter.addStep(new Result(Result.PASSED, ONE_MILLI_SECOND, null));
        counter.printSummary(new PrintStream(baos));

        assertThat(baos.toString(), endsWith(String.format(
                "1m1.001s%n")));
    }

    @Test
    public void should_print_minutes_instead_of_hours() {
        SummaryCounter counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.addStep(new Result(Result.PASSED, ONE_HOUR, null));
        counter.addStep(new Result(Result.PASSED, SummaryCounter.ONE_MINUTE, null));
        counter.printSummary(new PrintStream(baos));

        assertThat(baos.toString(), endsWith(String.format(
                "61m0.000s%n")));
    }

    @Test
    public void should_use_locale_for_decimal_separator() {
        SummaryCounter counter = new SummaryCounter(true, Locale.GERMANY);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.addStep(new Result(Result.PASSED, SummaryCounter.ONE_MINUTE, null));
        counter.addStep(new Result(Result.PASSED, SummaryCounter.ONE_SECOND, null));
        counter.addStep(new Result(Result.PASSED, ONE_MILLI_SECOND, null));
        counter.printSummary(new PrintStream(baos));

        assertThat(baos.toString(), endsWith(String.format(
                "1m1,001s%n")));
    }

    private void addOneStepScenario(SummaryCounter counter, String status) {
        counter.addStep(createResultWithStatus(status));
        counter.addScenario(status);
    }

    private Result createResultWithStatus(String status) {
        return new Result(status, 0l, null);
    }

    private SummaryCounter createMonochromeSummaryCounter() {
        return new SummaryCounter(true, Locale.US);
    }

    private SummaryCounter createColorSummaryCounter() {
        return new SummaryCounter(false, Locale.US);
    }
}
