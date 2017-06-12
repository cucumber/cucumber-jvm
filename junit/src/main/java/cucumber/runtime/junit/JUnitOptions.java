package cucumber.runtime.junit;

import cucumber.runtime.CucumberException;
import cucumber.util.FixJava;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class JUnitOptions {
    private static final String OPTIONS_RESOURCE = "/cucumber/api/junit/OPTIONS.txt";
    private static String optionsText;

    private boolean filenameCompatibleNames = false;
    private boolean stepNotifications = true;

    /**
     * Create a new instance from a list of options, for example:
     * <p/>
     * <pre<{@code Arrays.asList("--allow-started-ignored", "--filename-compatible-names");}</pre>
     *
     * @param argv the arguments
     */
    public JUnitOptions(List<String> argv) {
        argv = new ArrayList<String>(argv); // in case the one passed in is unmodifiable.
        parse(argv);
    }

    private void parse(List<String> args) {
        while (!args.isEmpty()) {
            String arg = args.remove(0).trim();

            if (arg.equals("--help") || arg.equals("-h")) {
                printOptions();
                System.exit(0);
            } else if (arg.equals("--no-allow-started-ignored") || arg.equals("--allow-started-ignored")) {
                System.err.println("WARNING: Found tags option '" + arg + "'. " +
                        "--allow-started-ignored has no effect, testStarted is always fired before a test is started." +
                        "--allow-started-ignored will be removed from the next release of Cucumber-JVM");
            } else if (arg.equals("--no-filename-compatible-names") || arg.equals("--filename-compatible-names")) {
                filenameCompatibleNames = !arg.startsWith("--no-");
            } else if (arg.equals("--no-step-notifications") || arg.equals("--step-notifications")) {
                stepNotifications = !arg.startsWith("--no-");
            } else{
                throw new CucumberException("Unknown option: " + arg);
            }
        }
    }

    boolean filenameCompatibleNames() {
        return filenameCompatibleNames;
    }
    public boolean stepNotifications(){
        return stepNotifications;
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
