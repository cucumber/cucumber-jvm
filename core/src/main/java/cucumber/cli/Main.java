package cucumber.cli;

import cucumber.formatter.FormatterFactory;
import cucumber.runtime.Runtime;
import cucumber.runtime.snippets.SnippetPrinter;
import gherkin.formatter.Formatter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class Main {
    private static final String USAGE = "HELP";
    private static final String VERSION = "1.0.0"; // TODO: get this from a file

    public static void main(String[] argv) throws IOException {
        List<String> filesOrDirs = new ArrayList<String>();
        List<String> gluePaths = new ArrayList<String>();
        List<Object> filters = new ArrayList<Object>();
        String format = "progress";
        List<String> args = new ArrayList<String>(asList(argv));
        String meta = null;
        boolean isDryRun = false;

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
                format = args.remove(0);
            } else if (arg.equals("--meta") || arg.equals("-m")) {
                meta = args.remove(0);
            } else if (arg.equals("--dry-run") || arg.equals("-d")) {
                isDryRun = true;
            } else {
                filesOrDirs.add(arg);
            }
        }
        if (gluePaths.isEmpty()) {
            System.out.println("Missing option: --glue");
            System.exit(1);
        }

        Runtime runtime = new Runtime(gluePaths, isDryRun);

        if (meta != null) {
            File out = new File(meta);
            out.getParentFile().mkdirs();
            FileWriter fileWriter = new FileWriter(out);
            runtime.writeMeta(filesOrDirs, fileWriter);
            fileWriter.close();
        } else {
            FormatterFactory formatterReporterFactory = new FormatterFactory();
            Formatter formatter = formatterReporterFactory.createFormatter(format, System.out);
            runtime.run(filesOrDirs, filters, formatter, formatterReporterFactory.reporter(formatter));

            new SnippetPrinter(System.out).printSnippets(runtime);
        }
    }
}
