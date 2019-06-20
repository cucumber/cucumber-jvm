package io.cucumber.core.plugin;

import io.cucumber.core.api.event.Result;
import io.cucumber.core.api.event.PickleStepTestStep;
import io.cucumber.core.api.event.EventHandler;
import io.cucumber.core.api.event.EventListener;
import io.cucumber.core.api.event.EventPublisher;
import io.cucumber.core.api.event.TestCaseFinished;
import io.cucumber.core.api.event.TestRunFinished;
import io.cucumber.core.api.event.TestRunStarted;
import io.cucumber.core.api.event.TestStepFinished;
import io.cucumber.core.api.plugin.ColorAware;
import io.cucumber.core.api.plugin.StrictAware;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.concurrent.TimeUnit.SECONDS;

class Stats implements EventListener, ColorAware, StrictAware {
    private static final long ONE_SECOND = SECONDS.toNanos(1);
    private static final long ONE_MINUTE = 60 * ONE_SECOND;
    private SubCounts scenarioSubCounts = new SubCounts();
    private SubCounts stepSubCounts = new SubCounts();
    private Instant startTime = Instant.EPOCH;
    private Duration totalDuration = Duration.ZERO;
    private Formats formats = new AnsiFormats();
    private Locale locale;
    private final List<String> failedScenarios = new ArrayList<String>();
    private List<String> ambiguousScenarios = new ArrayList<String>();
    private final List<String> pendingScenarios = new ArrayList<String>();
    private final List<String> undefinedScenarios = new ArrayList<String>();
    private final List<Throwable> errors = new ArrayList<Throwable>();
    private final EventHandler<TestRunStarted> testRunStartedHandler = new EventHandler<TestRunStarted>() {
        @Override
        public void receive(TestRunStarted event) {
            setStartTime(event.getInstant());
        }
    };
    private final EventHandler<TestStepFinished> stepFinishedHandler = new EventHandler<TestStepFinished>() {
        @Override
        public void receive(TestStepFinished event) {
            Result result = event.result;
            if (result.getError() != null) {
                addError(result.getError());
            }
            if (event.testStep instanceof PickleStepTestStep) {
                addStep(result.getStatus());
            }
        }
    };
    private final EventHandler<TestCaseFinished> testCaseFinishedHandler = new EventHandler<TestCaseFinished>() {
        @Override
        public void receive(TestCaseFinished event) {
            addScenario(event.result.getStatus(), event.testCase.getScenarioDesignation());
        }
    };
    private final EventHandler<TestRunFinished> testRunFinishedHandler = new EventHandler<TestRunFinished>() {
        @Override
        public void receive(TestRunFinished event) {
            setFinishTime(event.getInstant());
        }
    };
    private boolean strict;

    Stats() {
        this(Locale.getDefault());
    }

    Stats(Locale locale) {
        this.locale = locale;
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        if (monochrome) {
            formats = new MonochromeFormats();
        } else {
            formats = new AnsiFormats();
        }
    }

    @Override
    public void setStrict(boolean strict) {
        this.strict = true;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestRunStarted.class, testRunStartedHandler);
        publisher.registerHandlerFor(TestStepFinished.class, stepFinishedHandler);
        publisher.registerHandlerFor(TestCaseFinished.class, testCaseFinishedHandler);
        publisher.registerHandlerFor(TestRunFinished.class, testRunFinishedHandler);
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
        addComma = printSubCount(out, subCounts.failed, Result.Type.FAILED, addComma);
        addComma = printSubCount(out, subCounts.ambiguous, Result.Type.AMBIGUOUS, addComma);
        addComma = printSubCount(out, subCounts.skipped, Result.Type.SKIPPED, addComma);
        addComma = printSubCount(out, subCounts.pending, Result.Type.PENDING, addComma);
        addComma = printSubCount(out, subCounts.undefined, Result.Type.UNDEFINED, addComma);
        addComma = printSubCount(out, subCounts.passed, Result.Type.PASSED, addComma);
    }

    private boolean printSubCount(PrintStream out, int count, Result.Type type, boolean addComma) {
        if (count != 0) {
            if (addComma) {
                out.print(", ");
            }
            Format format = formats.get(type.lowerCaseName());
            out.print(format.text(count + " " + type.lowerCaseName()));
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
        printScenarios(out, failedScenarios, Result.Type.FAILED);
        printScenarios(out, ambiguousScenarios, Result.Type.AMBIGUOUS);
        if (strict) {
            printScenarios(out, pendingScenarios, Result.Type.PENDING);
            printScenarios(out, undefinedScenarios, Result.Type.UNDEFINED);
        }
    }

    private void printScenarios(PrintStream out, List<String> scenarios, Result.Type type) {
        Format format = formats.get(type.lowerCaseName());
        if (!scenarios.isEmpty()) {
            out.println(format.text(type.firstLetterCapitalizedName() + " scenarios:"));
        }
        for (String scenario : scenarios) {
            String[] parts = scenario.split("#");
            out.print(format.text(parts[0]));
            for (int i = 1; i < parts.length; ++i) {
                out.println("#" + parts[i]);
            }
        }
        if (!scenarios.isEmpty()) {
            out.println();
        }
    }

    void addStep(Result.Type resultStatus) {
        addResultToSubCount(stepSubCounts, resultStatus);
    }

    private void addError(Throwable error) {
        errors.add(error);
    }

    void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    void setFinishTime(Instant finishTime) {
        this.totalDuration = Duration.between(startTime, finishTime);
    }

    private void addResultToSubCount(SubCounts subCounts, Result.Type resultStatus) {
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

    void addScenario(Result.Type resultStatus, String scenarioDesignation) {
        addResultToSubCount(scenarioSubCounts, resultStatus);
        switch (resultStatus) {
        case FAILED:
            failedScenarios.add(scenarioDesignation);
            break;
        case AMBIGUOUS:
            ambiguousScenarios.add(scenarioDesignation);
            break;
        case PENDING:
            pendingScenarios.add(scenarioDesignation);
            break;
        case UNDEFINED:
            undefinedScenarios.add(scenarioDesignation);
            break;
        default:
            // intentionally left blank
        }
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
