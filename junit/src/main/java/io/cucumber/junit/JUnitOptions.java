package io.cucumber.junit;

import cucumber.runtime.CucumberException;
import cucumber.util.FixJava;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

class JUnitOptions {
    private static final String OPTIONS_RESOURCE = "/io/cucumber/junit/api/OPTIONS.txt";
    private static String optionsText;
    private final boolean strict;

    private boolean filenameCompatibleNames = false;
    private boolean stepNotifications = false;

    /**
     * Create a new instance from a list of options, for example:
     * <p/>
     * <pre<{@code Arrays.asList("--filename-compatible-names", "--step-notifications");}</pre>
     *
     * @param strict
     * @param argv the arguments
     */
    JUnitOptions(boolean strict, List<String> argv) {
        this.strict = strict;
        argv = new ArrayList<>(argv); // in case the one passed in is unmodifiable.
        parse(argv);
    }

    private void parse(List<String> args) {
        while (!args.isEmpty()) {
            String arg = args.remove(0).trim();

            if (arg.equals("--help") || arg.equals("-h")) {
                printOptions();
                System.exit(0);
            } else if (arg.equals("--no-filename-compatible-names") || arg.equals("--filename-compatible-names")) {
                filenameCompatibleNames = !arg.startsWith("--no-");
            } else if (arg.equals("--no-step-notifications") || arg.equals("--step-notifications")) {
                stepNotifications = !arg.startsWith("--no-");
            } else{
                printOptions();
                throw new CucumberException("Unknown option: " + arg);
            }
        }
    }

    boolean filenameCompatibleNames() {
        return filenameCompatibleNames;
    }
    boolean stepNotifications(){
        return stepNotifications;
    }
    boolean isStrict() {
        return strict;
    }

    private void printOptions() {
        loadUsageTextIfNeeded();
        System.out.println(optionsText);
    }

    private static void loadUsageTextIfNeeded() {
        if (optionsText == null) {
            try {
                Reader reader = new InputStreamReader(FixJava.class.getResourceAsStream(OPTIONS_RESOURCE), "UTF-8");
                optionsText = FixJava.readReader(reader);
            } catch (Exception e) {
                optionsText = "Could not load usage text: " + e.toString();
            }
        }
    }
}
