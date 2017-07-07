package cucumber.runtime;

import cucumber.api.Result;
import cucumber.runtime.formatter.AnsiFormats;
import cucumber.runtime.formatter.Format;
import cucumber.runtime.formatter.Formats;
import cucumber.runtime.formatter.MonochromeFormats;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class Stats {
    public static final long ONE_SECOND = 1000000000;
    public static final long ONE_MINUTE = 60 * ONE_SECOND;
    private SubCounts scenarioSubCounts = new SubCounts();
    private SubCounts stepSubCounts = new SubCounts();
    private long totalDuration = 0;
    private Formats formats;
    private Locale locale;
    private List<String> failedScenarios = new ArrayList<String>();
    private List<String> ambiguousScenarios = new ArrayList<String>();
    private List<String> pendingScenarios = new ArrayList<String>();
    private List<String> undefinedScenarios = new ArrayList<String>();

    public Stats(boolean monochrome) {
        this(monochrome, Locale.getDefault());
    }

    public Stats(boolean monochrome, Locale locale) {
        this.locale = locale;
        if (monochrome) {
            formats = new MonochromeFormats();
        } else {
            formats = new AnsiFormats();
        }
    }

    public void printStats(PrintStream out, boolean isStrict) {
        printNonZeroResultScenarios(out, isStrict);
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
        out.print(String.format("%dm", (totalDuration / ONE_MINUTE)));
        DecimalFormat format = new DecimalFormat("0.000", new DecimalFormatSymbols(locale));
        out.println(format.format(((double) (totalDuration % ONE_MINUTE)) / ONE_SECOND) + "s");
    }

    private void printNonZeroResultScenarios(PrintStream out, boolean isStrict) {
        printScenarios(out, failedScenarios, Result.Type.FAILED);
        printScenarios(out, ambiguousScenarios, Result.Type.AMBIGUOUS);
        if (isStrict) {
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

    public void addStep(Result result) {
        addResultToSubCount(stepSubCounts, result.getStatus());
        addTime(result.getDuration());
    }

    public void addScenario(Result.Type resultStatus) {
        addResultToSubCount(scenarioSubCounts, resultStatus);
    }

    public void addHookTime(Long duration) {
        addTime(duration);
    }

    private void addTime(Long duration) {
        totalDuration += duration != null ? duration : 0;
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

    public void addScenario(Result.Type resultStatus, String scenarioDesignation) {
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

    class SubCounts {
        public int passed = 0;
        public int failed = 0;
        public int ambiguous = 0;
        public int skipped = 0;
        public int pending = 0;
        public int undefined = 0;

        public int getTotal() {
            return passed + failed + ambiguous + skipped + pending + undefined;
        }
    }
}
