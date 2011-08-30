package cucumber.cli;

import cucumber.runtime.Runtime;
import cucumber.runtime.SnippetPrinter;
import gherkin.formatter.PrettyFormatter;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class Main {
    private static final String USAGE = "HELP";
    private static final String VERSION = "1.0.0"; // TODO: get this from a file

    public static void main(String[] argv) {
        Runtime runtime = null;

        List<String> filesOrDirs = new ArrayList<String>();
        List<Object> filters = new ArrayList<Object>();

        List<String> args = new ArrayList<String>(asList(argv));
        while (!args.isEmpty()) {
            String arg = args.remove(0);

            if (arg.equals("--help") || arg.equals("-h")) {
                System.out.println(USAGE);
                System.exit(0);
            } else if (arg.equals("--version") || arg.equals("-v")) {
                System.out.println(VERSION);
                System.exit(0);
            } else if (arg.equals("--glue") || arg.equals("-g")) {
                String packageNameOrScriptPrefix = args.remove(0);
                runtime = new Runtime(packageNameOrScriptPrefix);
            } else if (arg.equals("--tags") || arg.equals("-t")) {
                filters.add(args.remove(0));
            } else {
                filesOrDirs.add(arg);
            }
        }
        if (runtime == null) {
            System.out.println("Missing option: --glue");
            System.exit(1);
        }

        Runner runner = new Runner(runtime);

        PrettyFormatter prettyFormatter = new PrettyFormatter(System.out, false, true);
        runner.run(filesOrDirs, filters, prettyFormatter, prettyFormatter);

        new SnippetPrinter(System.out).printSnippets(runtime);

    }
}
