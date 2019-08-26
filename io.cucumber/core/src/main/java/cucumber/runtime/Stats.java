package cucumber.runtime;

import gherkin.formatter.AnsiFormats;
import gherkin.formatter.Format;
import gherkin.formatter.Formats;
import gherkin.formatter.MonochromeFormats;
import gherkin.formatter.model.Result;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class Stats {
    public static final long ONE_SECOND = 1000000000;
    public static final long ONE_MINUTE = 60 * ONE_SECOND;
    public static final String PENDING = "pending";
    private SubCounts scenarioSubCounts = new SubCounts();
    private SubCounts stepSubCounts = new SubCounts();
    private long totalDuration = 0;
    private Formats formats;
    private Locale locale;
    private List<String> failedScenarios = new ArrayList<String>();
    private List<String> pendingScenarios = new ArrayList<String>();
    private List<String> undefinedScenarios = new ArrayList<String>();
    private List<String> passedScenarios = new ArrayList<String>();

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
        out.print(String.format("%dm", (totalDuration / ONE_MINUTE)));
        DecimalFormat format = new DecimalFormat("0.000", new DecimalFormatSymbols(locale));
        out.println(format.format(((double) (totalDuration % ONE_MINUTE)) / ONE_SECOND) + "s");
    }

    private void printNonZeroResultScenarios(PrintStream out, boolean isStrict) {
        printScenarios(out, failedScenarios, Result.FAILED);
        if (isStrict) {
            printScenarios(out, pendingScenarios, PENDING);
            printScenarios(out, undefinedScenarios, Result.UNDEFINED.getStatus());
        }
    }

    private void printScenarios(PrintStream out, List<String> scenarios, String type) {
        Format format = formats.get(type);
        if (!scenarios.isEmpty()) {
            out.println(format.text(capitalizeFirstLetter(type) + " scenarios:"));
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

    private String capitalizeFirstLetter(String type) {
        return type.substring(0, 1).toUpperCase(locale) + type.substring(1);
    }

    public void addStep(Result result) {
        addResultToSubCount(stepSubCounts, result.getStatus());
        addTime(result.getDuration());
    }

    public void addScenario(String resultStatus) {
        addResultToSubCount(scenarioSubCounts, resultStatus);
    }

    public void addHookTime(Long duration) {
        addTime(duration);
    }

    private void addTime(Long duration) {
        totalDuration += duration != null ? duration : 0;
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

    public void addScenario(String resultStatus, String scenarioDesignation) {
        addResultToSubCount(scenarioSubCounts, resultStatus);
        if (resultStatus.equals(Result.FAILED)) {
            failedScenarios.add(scenarioDesignation);
        } else if (resultStatus.equals(PENDING)) {
            pendingScenarios.add(scenarioDesignation);
        } else if (resultStatus.equals(Result.UNDEFINED.getStatus())) {
            undefinedScenarios.add(scenarioDesignation);
        } else if (resultStatus.equals(Result.PASSED)) {
            passedScenarios.add(scenarioDesignation);
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
}
