package io.cucumber.core.plugin;

import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.TestStepFinished;
import io.cucumber.messages.types.TestStepResult;
import io.cucumber.messages.types.TestStepResultStatus;
import io.cucumber.plugin.ColorAware;
import io.cucumber.query.Query;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.cucumber.core.plugin.Formats.ansi;
import static io.cucumber.core.plugin.Formats.monochrome;
import static java.util.Locale.ROOT;
import static java.util.concurrent.TimeUnit.SECONDS;

class Stats implements ColorAware {

    private static final long ONE_SECOND = SECONDS.toNanos(1);
    private static final long ONE_MINUTE = 60 * ONE_SECOND;
     final Query query;
    private final Locale locale;
    private final List<Throwable> errors = new ArrayList<>();
    private Formats formats = ansi();

    Stats(Query query, Locale locale) {
        this.query = query;
        this.locale = locale;
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        formats = monochrome ? monochrome() : ansi();
    }

    public List<Throwable> getErrors() {
        return errors;
    }

    void printStats(PrintStream out) {
        printNonZeroResultScenarios(out);
        List<TestStepFinished> testStepFinished = query.findAllTestStepFinished();
        if (testStepFinished.isEmpty()) {
            out.println("0 Scenarios");
            out.println("0 Steps");
        } else {
            printScenarioCounts(out);
            printStepCounts(out);
        }
        printDuration(out);
    }

    private void printStepCounts(PrintStream out) {
        List<io.cucumber.messages.types.TestStepFinished> testStepsFinished = query.findAllTestStepFinished();
        Map<TestStepResultStatus, Long> testStepResultStatus = query.countMostSevereTestStepResultStatus();

        out.print(testStepsFinished.size());
        out.print(" Steps (");
        printSubCounts(out, testStepResultStatus);
        out.println(")");
    }

    private void printScenarioCounts(PrintStream out) {
        Map<TestStepResultStatus, Long> scenarioSubCounts = query.findAllTestCaseFinished()
                .stream()
                .map(query::findMostSevereTestStepResultBy)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(TestStepResult::getStatus)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        out.print(query.findAllTestCaseFinished().size());
        out.print(" Scenarios (");
        printSubCounts(out, scenarioSubCounts);
        out.println(")");
    }

    private void printSubCounts(PrintStream out, Map<TestStepResultStatus, Long> subCounts) {
        boolean addComma = false;
        addComma = printSubCount(out, subCounts, TestStepResultStatus.FAILED, addComma);
        addComma = printSubCount(out, subCounts, TestStepResultStatus.AMBIGUOUS, addComma);
        addComma = printSubCount(out, subCounts, TestStepResultStatus.SKIPPED, addComma);
        addComma = printSubCount(out, subCounts, TestStepResultStatus.PENDING, addComma);
        addComma = printSubCount(out, subCounts, TestStepResultStatus.UNDEFINED, addComma);
        addComma = printSubCount(out, subCounts, TestStepResultStatus.PASSED, addComma);
    }

    private boolean printSubCount(PrintStream out, Map<TestStepResultStatus, Long> subCounts, TestStepResultStatus type, boolean addComma) {
        long count = subCounts.getOrDefault(type, 0L);
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
        query.findTestRunDuration().ifPresent(duration -> {
            out.printf("%dm", (duration.toNanos() / ONE_MINUTE));
            DecimalFormat format = new DecimalFormat("0.000", new DecimalFormatSymbols(locale));
            out.println(format.format(((double) (duration.toNanos() % ONE_MINUTE) / ONE_SECOND)) + "s");            
        });
    }

    private void printNonZeroResultScenarios(PrintStream out) {
        Map<TestStepResultStatus, List<io.cucumber.messages.types.TestCaseFinished>> testCaseFinishedByStatus = query.findAllTestCaseFinished()
                .stream()
                .collect(Collectors.groupingBy(testCaseFinished -> query.findMostSevereTestStepResultBy(testCaseFinished).map(TestStepResult::getStatus).orElse(TestStepResultStatus.UNKNOWN)));

        printScenarios(out, testCaseFinishedByStatus, TestStepResultStatus.FAILED);
        printScenarios(out, testCaseFinishedByStatus, TestStepResultStatus.AMBIGUOUS);
        printScenarios(out, testCaseFinishedByStatus, TestStepResultStatus.PENDING);
        printScenarios(out, testCaseFinishedByStatus, TestStepResultStatus.UNDEFINED);
    }

    private void printScenarios(PrintStream out, Map<TestStepResultStatus, List<io.cucumber.messages.types.TestCaseFinished>> testCaseFinishedByStatus, TestStepResultStatus type) {
        List<io.cucumber.messages.types.TestCaseFinished> scenarios = testCaseFinishedByStatus.getOrDefault(type, Collections.emptyList());
        Format format = formats.get(type.name().toLowerCase(ROOT));
        if (!scenarios.isEmpty()) {
            out.println(format.text(firstLetterCapitalizedName(type) + " scenarios:"));
        }
        for (io.cucumber.messages.types.TestCaseFinished testCaseFinished : scenarios) {
            query.findPickleBy(testCaseFinished).ifPresent(pickle -> {
                String location = pickle.getUri() + query.findLocationOf(pickle).map(Location::getLine).map(line -> ":" + line).orElse("");
                out.println(location + " # " + pickle.getName());                
            });
        }
        if (!scenarios.isEmpty()) {
            out.println();
        }
    }

    private String firstLetterCapitalizedName(TestStepResultStatus status) {
        String name = status.name();
        return name.charAt(0) + name.substring(1).toLowerCase(ROOT);
    }

}
