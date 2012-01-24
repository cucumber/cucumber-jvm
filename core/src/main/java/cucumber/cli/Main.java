package cucumber.cli;

import cucumber.formatter.FormatterFactory;
import cucumber.formatter.MultiFormatter;
import cucumber.io.FileResourceLoader;
import cucumber.runtime.Runtime;
import cucumber.runtime.snippets.SummaryPrinter;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Stack;

import static java.util.Arrays.asList;

public class Main {
    private static final String USAGE = "TODO - Write the help";
    static final String VERSION = ResourceBundle.getBundle("cucumber.version").getString("cucumber-jvm.version");

    public static void main(String[] argv) throws Throwable {
        run(argv, Thread.currentThread().getContextClassLoader(), new DefaultRuntimeFactory());
    }

    public static void run(String[] argv, ClassLoader classLoader, RuntimeFactory runtimeFactory) throws IOException {
        List<String> featurePaths = new ArrayList<String>();
        List<String> gluePaths = new ArrayList<String>();
        List<Object> filters = new ArrayList<Object>();
        Stack<String> format = new Stack<String>();
        List<String> args = new ArrayList<String>(asList(argv));
        String dotCucumber = null;
        boolean isDryRun = false;

        FormatterFactory formatterFactory = new FormatterFactory();
        MultiFormatter multiFormatter = new MultiFormatter(classLoader);

        while (!args.isEmpty()) {
            String arg = args.remove(0);

            if (arg.equals("--help") || arg.equals("-h")) {
                System.out.println(USAGE);
                System.exit(0);
            } else if (arg.equals("--version") || arg.equals("-v")) {
                System.out.println(VERSION);
                System.exit(0);
            } else if (arg.equals("--glue") || arg.equals("-g")) {
                String gluePath = args.remove(0);
                gluePaths.add(gluePath);
            } else if (arg.equals("--tags") || arg.equals("-t")) {
                filters.add(args.remove(0));
            } else if (arg.equals("--format") || arg.equals("-f")) {
                format.push(args.remove(0));
            } else if (arg.equals("--out") || arg.equals("-o")) {
                File out = new File(args.remove(0));
                Formatter formatter = formatterFactory.createFormatter(format.pop(), out);
                multiFormatter.add(formatter);
            } else if (arg.equals("--dotcucumber")) {
                dotCucumber = args.remove(0);
            } else if (arg.equals("--dry-run") || arg.equals("-d")) {
                isDryRun = true;
            } else {
                // TODO: Use PathWithLines and add line filter if any
                featurePaths.add(arg);
            }
        }

        //Grab any formatters left on the stack and create a multiformatter for them to stdout
        // yes this will be ugly, but maybe people are crazy
        if (!format.isEmpty()) {
            multiFormatter.add(formatterFactory.createFormatter(format.pop(), System.out));
        } else {
            //Default formatter is progress unless otherwise specified or if they have piped all their other formatters
            // to an output thing
            multiFormatter.add(formatterFactory.createFormatter("progress", System.out));
        }

        if (gluePaths.isEmpty()) {
            System.out.println("Missing option: --glue");
            System.exit(1);
        }

        Runtime runtime = runtimeFactory.createRuntime(new FileResourceLoader(), gluePaths, classLoader, isDryRun);

        if (dotCucumber != null) {
            writeDotCucumber(featurePaths, dotCucumber, runtime);
        }
        run(featurePaths, filters, multiFormatter, runtime);
        printSummary(runtime);
        System.exit(runtime.exitStatus());
    }

    private static void writeDotCucumber(List<String> featurePaths, String dotCucumberPath, Runtime runtime) throws IOException {
        File dotCucumber = new File(dotCucumberPath);
        dotCucumber.mkdirs();
        runtime.writeStepdefsJson(featurePaths, dotCucumber);
    }

    private static void run(List<String> featurePaths, List<Object> filters, MultiFormatter multiFormatter, Runtime runtime) throws IOException {
        Formatter formatter = multiFormatter.formatterProxy();
        Reporter reporter = multiFormatter.reporterProxy();
        runtime.run(featurePaths, filters, formatter, reporter);
        formatter.done();
    }

    private static void printSummary(Runtime runtime) {
        new SummaryPrinter(System.out).print(runtime);
    }
}
