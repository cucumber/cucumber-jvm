package io.cucumber.core.plugin;

import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestStep;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static java.time.Duration.ofHours;
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

class StatsTest {

    private static final Instant ANY_TIME = Instant.ofEpochMilli(1234567890);

    @Test
    void should_print_zero_scenarios_zero_steps_if_nothing_has_executed() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        counter.printStats(new PrintStream(baos));

        assertThat(baos.toString(), startsWith(String.format(
            "0 Scenarios%n" +
                    "0 Steps%n")));
    }

    private Stats createMonochromeSummaryCounter() {
        Stats stats = new Stats(Locale.US);
        stats.setMonochrome(true);
        return stats;
    }

    @Test
    void should_only_print_sub_counts_if_not_zero() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.addStep(Status.PASSED);
        counter.addStep(Status.PASSED);
        counter.addStep(Status.PASSED);
        counter.addScenario(Status.PASSED, createTestCase("classpath:com/example", 42, "scenario designation"));
        counter.printStats(new PrintStream(baos));

        assertThat(baos.toString(), startsWith(String.format(
            "1 Scenarios (1 passed)%n" +
                    "3 Steps (3 passed)%n")));
    }

    @Test
    void should_print_sub_counts_in_order_failed_ambiguous_skipped_pending_undefined_passed() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        addOneStepScenario(counter, Status.PASSED);
        addOneStepScenario(counter, Status.FAILED);
        addOneStepScenario(counter, Status.AMBIGUOUS);
        addOneStepScenario(counter, Status.PENDING);
        addOneStepScenario(counter, Status.UNDEFINED);
        addOneStepScenario(counter, Status.SKIPPED);
        counter.printStats(new PrintStream(baos));

        assertThat(baos.toString(), containsString(String.format("" +
                "6 Scenarios (1 failed, 1 ambiguous, 1 skipped, 1 pending, 1 undefined, 1 passed)%n" +
                "6 Steps (1 failed, 1 ambiguous, 1 skipped, 1 pending, 1 undefined, 1 passed)%n")));
    }

    private void addOneStepScenario(Stats counter, Status status) {
        counter.addStep(status);
        counter.addScenario(status, createTestCase("classpath:com/example", 14, "scenario designation"));
    }

    @Test
    void should_print_sub_counts_in_order_failed_ambiguous_skipped_undefined_passed_in_color() {
        Stats counter = createColorSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        addOneStepScenario(counter, Status.PASSED);
        addOneStepScenario(counter, Status.FAILED);
        addOneStepScenario(counter, Status.AMBIGUOUS);
        addOneStepScenario(counter, Status.PENDING);
        addOneStepScenario(counter, Status.UNDEFINED);
        addOneStepScenario(counter, Status.SKIPPED);
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

    private Stats createColorSummaryCounter() {
        return new Stats(Locale.US);
    }

    @Test
    void should_print_zero_m_zero_s_if_nothing_has_executed() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.printStats(new PrintStream(baos));

        assertThat(baos.toString(), endsWith(String.format(
            "0m0.000s%n")));
    }

    @Test
    void should_report_the_difference_between_finish_time_and_start_time() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.setStartTime(ANY_TIME);
        counter.setFinishTime(ANY_TIME.plus(ofMillis(4)));
        counter.printStats(new PrintStream(baos));

        assertThat(baos.toString(), endsWith(String.format(
            "0m0.004s%n")));
    }

    @Test
    void should_print_minutes_seconds_and_milliseconds() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.setStartTime(ANY_TIME);
        counter.setFinishTime(ANY_TIME.plus(ofMinutes(1)).plus(ofSeconds(1)).plus(ofMillis(1)));
        counter.printStats(new PrintStream(baos));

        assertThat(baos.toString(), endsWith(String.format(
            "1m1.001s%n")));
    }

    @Test
    void should_print_minutes_instead_of_hours() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.setStartTime(ANY_TIME);
        counter.setFinishTime(ANY_TIME.plus(ofHours(1)).plus(ofMinutes(1)));
        counter.printStats(new PrintStream(baos));

        assertThat(baos.toString(), endsWith(String.format(
            "61m0.000s%n")));
    }

    @Test
    void should_use_locale_for_decimal_separator() {
        Stats counter = new Stats(Locale.GERMANY);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.setStartTime(ANY_TIME);
        counter.setFinishTime(ANY_TIME.plus(ofMinutes(1)).plus(ofSeconds(1)).plus(ofMillis(1)));
        counter.printStats(new PrintStream(baos));

        assertThat(baos.toString(), endsWith(String.format("1m1,001s%n")));
    }

    @Test
    void should_print_failed_ambiguous_scenarios() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.addStep(Status.FAILED);
        counter.addScenario(Status.FAILED, createTestCase("path/file.feature", 3, "Scenario: scenario_name"));
        counter.addStep(Status.AMBIGUOUS);
        counter.addScenario(Status.AMBIGUOUS, createTestCase("path/file.feature", 3, "Scenario: scenario_name"));
        counter.addStep(Status.UNDEFINED);
        counter.addScenario(Status.UNDEFINED, createTestCase("path/file.feature", 3, "Scenario: scenario_name"));
        counter.addStep(Status.PENDING);
        counter.addScenario(Status.PENDING, createTestCase("path/file.feature", 3, "Scenario: scenario_name"));
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

    @Test
    void should_print_failed_ambiguous_pending_undefined_scenarios_if_strict() {
        Stats counter = createMonochromeSummaryCounter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        counter.addStep(Status.FAILED);
        counter.addScenario(Status.FAILED, createTestCase("path/file.feature", 3, "Scenario: scenario_name"));
        counter.addStep(Status.AMBIGUOUS);
        counter.addScenario(Status.AMBIGUOUS, createTestCase("path/file.feature", 3, "Scenario: scenario_name"));
        counter.addStep(Status.UNDEFINED);
        counter.addScenario(Status.UNDEFINED, createTestCase("path/file.feature", 3, "Scenario: scenario_name"));
        counter.addStep(Status.PENDING);
        counter.addScenario(Status.PENDING, createTestCase("path/file.feature", 3, "Scenario: scenario_name"));
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

    private static TestCase createTestCase(String uri, int line, String name) {
        return new TestCase() {
            @Override
            public Integer getLine() {
                return getLocation().getLine();
            }

            @Override
            public Location getLocation() {
                return new Location(line, -1);
            }

            @Override
            public String getKeyword() {
                return "Scenario";
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getScenarioDesignation() {
                return null;
            }

            @Override
            public List<String> getTags() {
                return Collections.emptyList();
            }

            @Override
            public List<TestStep> getTestSteps() {
                return Collections.emptyList();
            }

            @Override
            public URI getUri() {
                return URI.create(uri);
            }

            @Override
            public UUID getId() {
                return UUID.randomUUID();
            }
        };
    }

}
