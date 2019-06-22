package io.cucumber.junit;

import cucumber.runtime.CucumberException;
import cucumber.util.FixJava;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

final class JUnitOptionsParser {

    private static final String OPTIONS_RESOURCE = "/io/cucumber/junit/api/OPTIONS.txt";
    private static String optionsText;

    /**
     * Create a new instance from a list of options, for example:
     * <p/>
     * <pre<{@code Arrays.asList("--filename-compatible-names", "--step-notifications");}</pre>
     *
     * @param args
     * @return
     */
    JUnitOptionsBuilder parse(List<String> args) {
        args = new ArrayList<>(args);
        JUnitOptionsBuilder builder = new JUnitOptionsBuilder();

        while (!args.isEmpty()) {
            String arg = args.remove(0).trim();

            if (arg.equals("--help") || arg.equals("-h")) {
                printOptions();
                System.exit(0);
            } else if (arg.equals("--no-filename-compatible-names") || arg.equals("--filename-compatible-names")) {
                builder.setFilenameCompatibleNames(!arg.startsWith("--no-"));
            } else if (arg.equals("--no-step-notifications") || arg.equals("--step-notifications")) {
                builder.setStepNotifications(!arg.startsWith("--no-"));
            } else {
                printOptions();
                throw new CucumberException("Unknown option: " + arg);
            }
        }
        return builder;
    }

    JUnitOptionsBuilder parse(Class<?> clazz) {
        JUnitOptionsBuilder args = new JUnitOptionsBuilder();

        for (Class<?> classWithOptions = clazz; classWithOptions != Object.class; classWithOptions = classWithOptions.getSuperclass()) {
            final CucumberOptions options = classWithOptions.getAnnotation(CucumberOptions.class);

            if (options == null) {
                continue;
            }

            if (options.stepNotifications()) {
                args.setStepNotifications(true);
            }
            if (options.useFileNameCompatibleName()) {
                args.setFilenameCompatibleNames(true);
            }

        }
        return args;
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
