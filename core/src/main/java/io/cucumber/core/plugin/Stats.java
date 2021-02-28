package io.cucumber.core.plugin;

import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestStepFinished;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static io.cucumber.core.plugin.Formats.ansi;
import static io.cucumber.core.plugin.Formats.monochrome;
import static java.util.Locale.ROOT;
import static java.util.concurrent.TimeUnit.SECONDS;

class Stats implements ConcurrentEventListener, ColorAware {

    private static final long ONE_SECOND = SECONDS.toNanos(1);
    private static final long ONE_MINUTE = 60 * ONE_SECOND;
    private final SubCounts scenarioSubCounts = new SubCounts();
    private final SubCounts stepSubCounts = new SubCounts();
    private final Locale locale;
    private final List<TestCase> failedScenarios = new ArrayList<>();
    private final List<TestCase> ambiguousScenarios = new ArrayList<>();
    private final List<TestCase> pendingScenarios = new ArrayList<>();
    private final List<TestCase> undefinedScenarios = new ArrayList<>();
    private final List<Throwable> errors = new ArrayList<>();
    private Instant startTime = Instant.EPOCH;
    private Duration totalDuration = Duration.ZERO;
    private Formats formats = ansi();

    Stats(Locale locale) {
        this.locale = locale;
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        formats = monochrome ? monochrome() : ansi();
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestRunStarted.class, this::setStartTime);
        publisher.registerHandlerFor(TestStepFinished.class, this::addStepResult);
        publisher.registerHandlerFor(TestCaseFinished.class, this::addScenario);
        publisher.registerHandlerFor(TestRunFinished.class, this::setFinishTime);
    }

    private void setStartTime(TestRunStarted event) {
        setStartTime(event.getInstant());
    }

    private void addStepResult(TestStepFinished event) {
        Result result = event.getResult();
        if (result.getError() != null) {
            addError(result.getError());
        }
        if (event.getTestStep() instanceof PickleStepTestStep) {
            addStep(result.getStatus());
        }
    }

    private void addScenario(TestCaseFinished event) {
        TestCase testCase = event.getTestCase();
        addScenario(event.getResult().getStatus(), testCase);
    }

    private void setFinishTime(TestRunFinished event) {
        setFinishTime(event.getInstant());
    }

    void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    private void addError(Throwable error) {
        errors.add(error);
    }

    void addStep(Status resultStatus) {
        addResultToSubCount(stepSubCounts, resultStatus);
    }

    void addScenario(Status resultStatus, TestCase testCase) {
        addResultToSubCount(scenarioSubCounts, resultStatus);
        switch (resultStatus) {
            case FAILED:
                failedScenarios.add(testCase);
                break;
            case AMBIGUOUS:
                ambiguousScenarios.add(testCase);
                break;
            case PENDING:
                pendingScenarios.add(testCase);
                break;
            case UNDEFINED:
                undefinedScenarios.add(testCase);
                break;
            default:
                // intentionally left blank
        }
    }

    void setFinishTime(Instant finishTime) {
        this.totalDuration = Duration.between(startTime, finishTime);
    }

    private void addResultToSubCount(SubCounts subCounts, Status resultStatus) {
        switch (resultStatus) {
            case FAILED:
                subCounts.failed++;
                break;
            case AMBIGUOUS:
                subCounts.ambiguous++;
                break;
            case PENDING:
                subCounts.pending++;
                break;
            case UNDEFINED:
                subCounts.undefined++;
                break;
            case SKIPPED:
                subCounts.skipped++;
                break;
            default:
                subCounts.passed++;
        }
    }

    public List<Throwable> getErrors() {
        return errors;
    }

    void printStats(PrintStream out) {
        printNonZeroResultScenarios(out);
        if (stepSubCounts.getTotal() == 0) {
            out.println("0 Scenarios");
            out.println("0 Steps");
        } else {
            printScenarioCounts(out);
            printStepCounts(out);
        }
        printDuration(out);
    }

    private void printStepCounts(PrintStream out) {
        out.print(stepSubCounts.getTotal());
        out.print(" Steps (");
        printSubCounts(out, stepSubCounts);
        out.println(")");
    }

    private void printScenarioCounts(PrintStream out) {
        out.print(scenarioSubCounts.getTotal());
        out.print(" Scenarios (");
        printSubCounts(out, scenarioSubCounts);
        out.println(")");
    }

    private void printSubCounts(PrintStream out, SubCounts subCounts) {
        boolean addComma = false;
        addComma = printSubCount(out, subCounts.failed, Status.FAILED, addComma);
        addComma = printSubCount(out, subCounts.ambiguous, Status.AMBIGUOUS, addComma);
        addComma = printSubCount(out, subCounts.skipped, Status.SKIPPED, addComma);
        addComma = printSubCount(out, subCounts.pending, Status.PENDING, addComma);
        addComma = printSubCount(out, subCounts.undefined, Status.UNDEFINED, addComma);
        addComma = printSubCount(out, subCounts.passed, Status.PASSED, addComma);
    }

    private boolean printSubCount(PrintStream out, int count, Status type, boolean addComma) {
        if (count != 0) {
            if (addComma) {
                out.print(", ");
            }
            Format format = formats.get(type.name().toLowerCase(ROOT));
            out.print(format.text(count + " " + type.name().toLowerCase(ROOT)));
            addComma = true;
        }
        return addComma;
    }

    private void printDuration(PrintStream out) {
        out.print(String.format("%dm", (totalDuration.toNanos() / ONE_MINUTE)));
        DecimalFormat format = new DecimalFormat("0.000", new DecimalFormatSymbols(locale));
        out.println(format.format(((double) (totalDuration.toNanos() % ONE_MINUTE) / ONE_SECOND)) + "s");
    }

    private void printNonZeroResultScenarios(PrintStream out) {
        printScenarios(out, failedScenarios, Status.FAILED);
        printScenarios(out, ambiguousScenarios, Status.AMBIGUOUS);
        printScenarios(out, pendingScenarios, Status.PENDING);
        printScenarios(out, undefinedScenarios, Status.UNDEFINED);
    }

    private void printScenarios(PrintStream out, List<TestCase> scenarios, Status type) {
        Format format = formats.get(type.name().toLowerCase(ROOT));
        if (!scenarios.isEmpty()) {
            out.println(format.text(firstLetterCapitalizedName(type) + " scenarios:"));
        }
        for (TestCase scenario : scenarios) {
            String location = scenario.getUri() + ":" + scenario.getLocation().getLine();
            out.println(location + " # " + scenario.getName());
        }
        if (!scenarios.isEmpty()) {
            out.println();
        }
    }

    private String firstLetterCapitalizedName(Status status) {
        String name = status.name();
        return name.substring(0, 1) + name.substring(1).toLowerCase(ROOT);
    }

    static class SubCounts {

        public int passed = 0;
        public int failed = 0;
        public int ambiguous = 0;
        public int skipped = 0;
        public int pending = 0;
        public int undefined = 0;

        int getTotal() {
            return passed + failed + ambiguous + skipped + pending + undefined;
        }

    }

}
