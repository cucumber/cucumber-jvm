package cucumber.runtime;

import gherkin.formatter.AnsiFormats;
import gherkin.formatter.Format;
import gherkin.formatter.Formats;
import gherkin.formatter.MonochromeFormats;
import gherkin.formatter.model.Result;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class SummaryCounter {
    public static final long ONE_SECOND = 1000000000;
    public static final long ONE_MINUTE = 60 * ONE_SECOND;
    public static final String PENDING = "pending";
    private SubCounts scenarioSubCounts = new SubCounts();
    private SubCounts stepSubCounts = new SubCounts();
    private long totalDuration = 0;
    private Formats formats;
    private Locale locale;

    public SummaryCounter(boolean monochrome) {
        this(monochrome, Locale.getDefault());
    }

    public SummaryCounter(boolean monochrome, Locale locale) {
        this.locale = locale;
        if (monochrome) {
            formats = new MonochromeFormats();
        } else {
            formats = new AnsiFormats();
        }
    }

    public void printSummary(PrintStream out) {
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
        addComma = printSubCount(out, subCounts.failed, Result.FAILED, addComma);
        addComma = printSubCount(out, subCounts.skipped, Result.SKIPPED.getStatus(), addComma);
        addComma = printSubCount(out, subCounts.pending, PENDING, addComma);
        addComma = printSubCount(out, subCounts.undefined, Result.UNDEFINED.getStatus(), addComma);
        addComma = printSubCount(out, subCounts.passed, Result.PASSED, addComma);
    }

    private boolean printSubCount(PrintStream out, int count, String type, boolean addComma) {
        if (count != 0) {
            if (addComma) {
                out.print(", ");
            }
            Format format = formats.get(type);
            out.print(format.text(count + " " + type));
            addComma = true;
        }
        return addComma;
    }

    private void printDuration(PrintStream out) {
        out.print(String.format("%dm", (totalDuration/ONE_MINUTE)));
        DecimalFormat format = new DecimalFormat("0.000", new DecimalFormatSymbols(locale));
        out.println(format.format(((double)(totalDuration%ONE_MINUTE))/ONE_SECOND) + "s");
    }

    public void addStep(Result result) {
        addResultToSubCount(stepSubCounts, result.getStatus());
        // the following constant defined in the Gherkin libaray have duration == null, so calls to getDuration()
        // will result in a NullPointerException
        if (!result.equals(Result.SKIPPED) && !result.equals(Result.UNDEFINED)) {
            addTime(result.getDuration());
        }
    }

    public void addScenario(String resultStatus) {
        addResultToSubCount(scenarioSubCounts, resultStatus);
    }

    public void addHookTime(long duration) {
        addTime(duration);
    }

    private void addTime(long duration) {
        totalDuration += duration;
    }

    private void addResultToSubCount(SubCounts subCounts, String resultStatus) {
        if (resultStatus.equals(Result.FAILED)) {
            subCounts.failed++;
        } else if (resultStatus.equals(PENDING)) {
            subCounts.pending++;
        } else if (resultStatus.equals(Result.UNDEFINED.getStatus())) {
            subCounts.undefined++;
        } else if (resultStatus.equals(Result.SKIPPED.getStatus())) {
            subCounts.skipped++;
        } else if (resultStatus.equals(Result.PASSED)) {
            subCounts.passed++;
        }
    }
}

class SubCounts {
    public int passed = 0;
    public int failed = 0;
    public int skipped = 0;
    public int pending = 0;
    public int undefined = 0;

    public int getTotal() {
        return passed + failed + skipped + pending + undefined;
    }
}
